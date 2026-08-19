package com.example.data.service

import com.example.data.model.CrmUser
import com.example.data.model.UserRole
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.tasks.await

/**
 * UserProfileSyncService
 *
 * Synchronizes user profile data and access roles (Sales Manager, Team Leader,
 * Sales Representative, Moderator) between Cloud Firestore ("users" collection)
 * and the local CRM application.
 */
class UserProfileSyncService {

    private val firestore: FirebaseFirestore?
        get() = try {
            FirebaseFirestore.getInstance()
        } catch (_: Exception) {
            null
        }

    companion object {
        const val USERS_COLLECTION = "users"
        const val EXCLUSIVE_MANAGER_EMAIL = "dreamcarautoloan@gmail.com"

        /**
         * Validates if the given email is the designated Sales Manager
         */
        fun isManagerEmail(email: String): Boolean {
            return email.trim().equals(EXCLUSIVE_MANAGER_EMAIL, ignoreCase = true)
        }

        /**
         * Enforces strict role policy: Only dreamcarautoloan@gmail.com can hold SALES_MANAGER.
         * All other unauthorized accounts default to TEAM_LEADER, SALES_REP, or MODERATOR.
         */
        fun resolveAuthorizedRole(email: String, requestedRole: UserRole): UserRole {
            return if (isManagerEmail(email)) {
                UserRole.SALES_MANAGER
            } else if (requestedRole == UserRole.SALES_MANAGER) {
                UserRole.TEAM_LEADER
            } else {
                requestedRole
            }
        }
    }

    /**
     * Synchronizes a user profile to Firestore upon initial authentication (Sign-in, Sign-up, or Google Auth).
     */
    suspend fun syncUserProfile(user: CrmUser): Result<CrmUser> {
        return try {
            val authorizedRole = resolveAuthorizedRole(user.email, user.role)
            val updatedUser = user.copy(
                role = authorizedRole,
                lastLoginAt = System.currentTimeMillis()
            )

            val fs = firestore
            if (fs == null) {
                // Offline or local development fallback
                return Result.success(updatedUser)
            }

            if (user.uid.isBlank()) {
                return Result.failure(IllegalArgumentException("User UID cannot be blank for Firestore sync."))
            }

            val data = hashMapOf(
                "uid" to updatedUser.uid,
                "email" to updatedUser.email,
                "displayName" to updatedUser.displayName,
                "role" to updatedUser.role.name,
                "assignedRepName" to updatedUser.assignedRepName,
                "photoUrl" to updatedUser.photoUrl,
                "phone" to updatedUser.phone,
                "lastLoginAt" to updatedUser.lastLoginAt,
                "createdAt" to updatedUser.createdAt,
                "updatedAt" to System.currentTimeMillis()
            )

            fs.collection(USERS_COLLECTION)
                .document(updatedUser.uid)
                .set(data, SetOptions.merge())
                .await()

            Result.success(updatedUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Fetches a user's synchronized profile document from Firestore.
     */
    suspend fun fetchUserProfile(uid: String, fallbackEmail: String = ""): Result<CrmUser?> {
        return try {
            val fs = firestore ?: return Result.success(null)
            if (uid.isBlank()) return Result.success(null)

            val doc = fs.collection(USERS_COLLECTION).document(uid).get().await()
            if (doc.exists()) {
                val email = doc.getString("email") ?: fallbackEmail
                val roleStr = doc.getString("role") ?: UserRole.SALES_REP.name
                val rawRole = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.SALES_REP }
                val authorizedRole = resolveAuthorizedRole(email, rawRole)
                val repName = doc.getString("assignedRepName") ?: "Nada"
                val name = doc.getString("displayName") ?: email.substringBefore("@")
                val photo = doc.getString("photoUrl").orEmpty()
                val phone = doc.getString("phone").orEmpty()
                val lastLogin = doc.getLong("lastLoginAt") ?: System.currentTimeMillis()
                val created = doc.getLong("createdAt") ?: System.currentTimeMillis()

                val crmUser = CrmUser(
                    uid = uid,
                    email = email,
                    displayName = name,
                    role = authorizedRole,
                    assignedRepName = repName,
                    photoUrl = photo,
                    phone = phone,
                    lastLoginAt = lastLogin,
                    createdAt = created
                )
                Result.success(crmUser)
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Real-time stream listening to profile changes and role updates for a specific user in Firestore.
     */
    fun observeUserProfile(uid: String): Flow<CrmUser?> {
        val fs = firestore ?: return flowOf(null)
        if (uid.isBlank()) return flowOf(null)

        return callbackFlow {
            val docRef = fs.collection(USERS_COLLECTION).document(uid)
            val listener = docRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(null)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val email = snapshot.getString("email").orEmpty()
                    val roleStr = snapshot.getString("role") ?: UserRole.SALES_REP.name
                    val rawRole = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.SALES_REP }
                    val authorizedRole = resolveAuthorizedRole(email, rawRole)
                    val repName = snapshot.getString("assignedRepName") ?: "Nada"
                    val name = snapshot.getString("displayName") ?: email.substringBefore("@")
                    val photo = snapshot.getString("photoUrl").orEmpty()
                    val phone = snapshot.getString("phone").orEmpty()
                    val lastLogin = snapshot.getLong("lastLoginAt") ?: System.currentTimeMillis()
                    val created = snapshot.getLong("createdAt") ?: System.currentTimeMillis()

                    val user = CrmUser(
                        uid = uid,
                        email = email,
                        displayName = name,
                        role = authorizedRole,
                        assignedRepName = repName,
                        photoUrl = photo,
                        phone = phone,
                        lastLoginAt = lastLogin,
                        createdAt = created
                    )
                    trySend(user)
                } else {
                    trySend(null)
                }
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Streams all team member profiles from Firestore for management dashboards.
     */
    fun observeAllTeamProfiles(): Flow<List<CrmUser>> {
        val fs = firestore ?: return flowOf(emptyList())

        return callbackFlow {
            val collectionRef = fs.collection(USERS_COLLECTION)
            val listener = collectionRef.addSnapshotListener { snapshots, error ->
                if (error != null || snapshots == null) {
                    trySend(emptyList())
                    return@addSnapshotListener
                }

                val users = snapshots.documents.mapNotNull { doc ->
                    val uid = doc.getString("uid") ?: doc.id
                    val email = doc.getString("email").orEmpty()
                    val roleStr = doc.getString("role") ?: UserRole.SALES_REP.name
                    val rawRole = try { UserRole.valueOf(roleStr) } catch (_: Exception) { UserRole.SALES_REP }
                    val authorizedRole = resolveAuthorizedRole(email, rawRole)
                    val repName = doc.getString("assignedRepName") ?: "Nada"
                    val name = doc.getString("displayName") ?: email.substringBefore("@")
                    val photo = doc.getString("photoUrl").orEmpty()
                    val phone = doc.getString("phone").orEmpty()
                    val lastLogin = doc.getLong("lastLoginAt") ?: System.currentTimeMillis()
                    val created = doc.getLong("createdAt") ?: System.currentTimeMillis()

                    CrmUser(
                        uid = uid,
                        email = email,
                        displayName = name,
                        role = authorizedRole,
                        assignedRepName = repName,
                        photoUrl = photo,
                        phone = phone,
                        lastLoginAt = lastLogin,
                        createdAt = created
                    )
                }
                trySend(users)
            }
            awaitClose { listener.remove() }
        }
    }

    /**
     * Seeds or updates Firestore with all predefined team members:
     * Ali (Sales Manager), Marwan (Team Leader), and Sales Reps (Mahmoud, Nahla, Israa, Nada, Alaa).
     */
    suspend fun seedPredefinedTeamUsers(overwrite: Boolean = false): Result<Int> {
        val seedService = FirestoreDatabaseSeedService(this)
        return seedService.seedPredefinedUsers(overwrite)
    }

    /**
     * Updates a user's role in Firestore (e.g. Sales Manager adjusting permissions).
     */
    suspend fun updateUserRole(uid: String, newRole: UserRole, userEmail: String): Result<Unit> {
        return try {
            val fs = firestore ?: return Result.success(Unit)
            val effectiveRole = resolveAuthorizedRole(userEmail, newRole)

            fs.collection(USERS_COLLECTION)
                .document(uid)
                .update(
                    mapOf(
                        "role" to effectiveRole.name,
                        "updatedAt" to System.currentTimeMillis()
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
