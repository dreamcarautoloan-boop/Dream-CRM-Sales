package com.example.data.repository

import android.content.Context
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.example.data.model.CrmUser
import com.example.data.model.UserPermissions
import com.example.data.model.UserRole
import com.example.data.service.UserProfileSyncService
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

/**
 * AuthenticationRepository
 *
 * Encapsulates Firebase Auth methods for user sign-in, account creation,
 * Google Sign-In, and syncs user roles with Firebase Firestore.
 */
class AuthenticationRepository(
    val syncService: UserProfileSyncService = UserProfileSyncService()
) {

    private val auth: FirebaseAuth?
        get() = try {
            FirebaseAuth.getInstance()
        } catch (_: Exception) {
            null
        }

    companion object {
        const val EXCLUSIVE_MANAGER_EMAIL = UserProfileSyncService.EXCLUSIVE_MANAGER_EMAIL
        const val USERS_COLLECTION = UserProfileSyncService.USERS_COLLECTION

        fun isManagerEmail(email: String): Boolean = UserProfileSyncService.isManagerEmail(email)

        fun resolveAuthorizedRole(email: String, requestedRole: UserRole): UserRole =
            UserProfileSyncService.resolveAuthorizedRole(email, requestedRole)
    }

    val currentFirebaseUser: FirebaseUser?
        get() = auth?.currentUser

    fun authStateFlow(): Flow<FirebaseUser?> {
        val fbAuth = auth ?: return flowOf(null)
        return callbackFlow {
            val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
                trySend(firebaseAuth.currentUser)
            }
            fbAuth.addAuthStateListener(listener)
            awaitClose { fbAuth.removeAuthStateListener(listener) }
        }
    }

    /**
     * Observes user role from Firestore collection "users"
     */
    fun observeUserRoleFromFirestore(uid: String): Flow<UserRole?> =
        syncService.observeUserProfile(uid).map { it?.role }

    /**
     * Syncs user profile with Firestore
     */
    suspend fun syncUserProfileToFirestore(user: CrmUser) {
        syncService.syncUserProfile(user)
    }

    /**
     * Fetches user profile from Firestore or returns default
     */
    suspend fun fetchUserProfileFromFirestore(uid: String, fallbackEmail: String): CrmUser? {
        return syncService.fetchUserProfile(uid, fallbackEmail).getOrNull()
    }

    fun getCurrentUser(savedRepName: String = "Nada"): CrmUser? {
        val fbUser = auth?.currentUser ?: return null
        val userEmail = fbUser.email.orEmpty()
        val role = if (isManagerEmail(userEmail)) {
            UserRole.SALES_MANAGER
        } else {
            UserRole.SALES_REP
        }
        return CrmUser(
            uid = fbUser.uid,
            email = userEmail,
            displayName = fbUser.displayName ?: userEmail.substringBefore("@"),
            role = role,
            assignedRepName = savedRepName,
            photoUrl = fbUser.photoUrl?.toString().orEmpty()
        )
    }

    suspend fun signInWithEmail(
        email: String,
        pass: String,
        requestedRole: UserRole,
        assignedRepName: String
    ): Result<CrmUser> {
        return try {
            val cleanEmail = email.trim()
            val fbAuth = auth
            if (fbAuth == null) {
                // Fallback for offline/demo mode when Firebase is unavailable
                val role = resolveAuthorizedRole(cleanEmail, requestedRole)
                val demoUser = CrmUser(
                    uid = "local_${cleanEmail.hashCode()}",
                    email = cleanEmail,
                    displayName = cleanEmail.substringBefore("@"),
                    role = role,
                    assignedRepName = if (role == UserRole.SALES_REP) assignedRepName else "Nada"
                )
                return Result.success(demoUser)
            }

            val result = fbAuth.signInWithEmailAndPassword(cleanEmail, pass.trim()).await()
            val user = result.user ?: return Result.failure(Exception("Authentication returned empty user session."))

            val finalRole = resolveAuthorizedRole(cleanEmail, requestedRole)
            val crmUser = CrmUser(
                uid = user.uid,
                email = user.email ?: cleanEmail,
                displayName = user.displayName ?: cleanEmail.substringBefore("@"),
                role = finalRole,
                assignedRepName = if (finalRole == UserRole.SALES_REP) assignedRepName else "Nada",
                photoUrl = user.photoUrl?.toString().orEmpty()
            )

            syncUserProfileToFirestore(crmUser)
            Result.success(crmUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signUpWithEmail(
        email: String,
        pass: String,
        displayName: String,
        requestedRole: UserRole,
        assignedRepName: String
    ): Result<CrmUser> {
        return try {
            val cleanEmail = email.trim()

            if (requestedRole == UserRole.SALES_MANAGER && !isManagerEmail(cleanEmail)) {
                return Result.failure(
                    SecurityException("Sales Manager (المدير) role is exclusively reserved for $EXCLUSIVE_MANAGER_EMAIL.")
                )
            }

            val fbAuth = auth
            if (fbAuth == null) {
                val finalRole = resolveAuthorizedRole(cleanEmail, requestedRole)
                val demoUser = CrmUser(
                    uid = "local_${cleanEmail.hashCode()}",
                    email = cleanEmail,
                    displayName = if (displayName.isNotBlank()) displayName else cleanEmail.substringBefore("@"),
                    role = finalRole,
                    assignedRepName = if (finalRole == UserRole.SALES_REP) assignedRepName else "Nada"
                )
                return Result.success(demoUser)
            }

            val result = fbAuth.createUserWithEmailAndPassword(cleanEmail, pass.trim()).await()
            val user = result.user ?: return Result.failure(Exception("User registration failed."))

            if (displayName.isNotBlank()) {
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName.trim())
                    .build()
                user.updateProfile(profileUpdates).await()
            }

            val finalRole = resolveAuthorizedRole(cleanEmail, requestedRole)
            val crmUser = CrmUser(
                uid = user.uid,
                email = user.email ?: cleanEmail,
                displayName = if (displayName.isNotBlank()) displayName else cleanEmail.substringBefore("@"),
                role = finalRole,
                assignedRepName = if (finalRole == UserRole.SALES_REP) assignedRepName else "Nada",
                photoUrl = ""
            )

            syncUserProfileToFirestore(crmUser)
            Result.success(crmUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signInWithGoogle(
        context: Context,
        requestedRole: UserRole,
        assignedRepName: String
    ): Result<CrmUser> {
        return try {
            val credentialManager = CredentialManager.create(context)
            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setAutoSelectEnabled(false)
                .setServerClientId("dummy-client-id.apps.googleusercontent.com")
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response: GetCredentialResponse = credentialManager.getCredential(
                request = request,
                context = context
            )

            val credential = response.credential
            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)

                val fbAuth = auth
                if (fbAuth == null) {
                    val gEmail = googleIdTokenCredential.id
                    val finalRole = resolveAuthorizedRole(gEmail, requestedRole)
                    val crmUser = CrmUser(
                        uid = "google_${gEmail.hashCode()}",
                        email = gEmail,
                        displayName = googleIdTokenCredential.displayName ?: gEmail.substringBefore("@"),
                        role = finalRole,
                        assignedRepName = if (finalRole == UserRole.SALES_REP) assignedRepName else "Nada",
                        photoUrl = googleIdTokenCredential.profilePictureUri?.toString().orEmpty()
                    )
                    return Result.success(crmUser)
                }

                val authResult = fbAuth.signInWithCredential(authCredential).await()
                val user = authResult.user ?: return Result.failure(Exception("Google sign-in user session empty."))

                val finalRole = resolveAuthorizedRole(user.email ?: "", requestedRole)
                val crmUser = CrmUser(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: user.email?.substringBefore("@") ?: "User",
                    role = finalRole,
                    assignedRepName = if (finalRole == UserRole.SALES_REP) assignedRepName else "Nada",
                    photoUrl = user.photoUrl?.toString().orEmpty()
                )

                syncUserProfileToFirestore(crmUser)
                Result.success(crmUser)
            } else {
                Result.failure(Exception("Unsupported credential type from Google Sign-In."))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            auth?.sendPasswordResetEmail(email.trim())?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Clears local user session, credentials, and signs out from Firebase Auth.
     */
    suspend fun logout(context: Context? = null): Result<Unit> {
        return signOut(context)
    }

    /**
     * Signs out the user and clears authentication state.
     */
    suspend fun signOut(context: Context? = null): Result<Unit> {
        return try {
            if (context != null) {
                try {
                    val credentialManager = CredentialManager.create(context)
                    credentialManager.clearCredentialState(ClearCredentialStateRequest())
                } catch (_: Exception) { }
            }
            auth?.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
