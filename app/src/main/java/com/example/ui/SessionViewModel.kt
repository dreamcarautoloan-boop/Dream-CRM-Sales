package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.CrmUser
import com.example.data.model.UserPermissions
import com.example.data.model.UserRole
import com.example.data.repository.AuthenticationRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class SessionUiState(
    val isAuthenticated: Boolean = true,
    val currentUser: CrmUser? = CrmUser(
        displayName = "Sales Manager (المدير)",
        role = UserRole.SALES_MANAGER,
        email = AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
    ),
    val userRole: UserRole = UserRole.SALES_MANAGER,
    val permissions: UserPermissions = UserPermissions.fromRole(UserRole.SALES_MANAGER),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val syncStatus: String = "Active Manager Session"
)

/**
 * SessionManager / SessionViewModel
 *
 * Tracks the current user's authentication status and their assigned role
 * from Firestore to conditionally render UI components based on permissions.
 */
class SessionViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthenticationRepository()

    private val _currentUser = MutableStateFlow<CrmUser?>(
        CrmUser(
            displayName = "Sales Manager",
            role = UserRole.SALES_MANAGER,
            email = AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
        )
    )
    private val _isAuthenticated = MutableStateFlow(true)
    private val _currentRole = MutableStateFlow(UserRole.SALES_MANAGER)
    private val _activeRepName = MutableStateFlow("Nada")
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _syncStatus = MutableStateFlow("Active Manager Session")

    init {
        // Observe Firebase Auth state
        viewModelScope.launch {
            authRepository.authStateFlow().collect { fbUser ->
                if (fbUser != null) {
                    val fallbackEmail = fbUser.email.orEmpty()
                    // Fetch role from Firestore
                    val firestoreUser = authRepository.fetchUserProfileFromFirestore(fbUser.uid, fallbackEmail)
                    if (firestoreUser != null) {
                        _currentUser.value = firestoreUser
                        _currentRole.value = firestoreUser.role
                        _activeRepName.value = firestoreUser.assignedRepName
                        _syncStatus.value = "Synced with Firestore"
                    } else {
                        val localRole = if (AuthenticationRepository.isManagerEmail(fallbackEmail)) {
                            UserRole.SALES_MANAGER
                        } else {
                            _currentRole.value
                        }
                        _currentUser.value = CrmUser(
                            uid = fbUser.uid,
                            email = fallbackEmail,
                            displayName = fbUser.displayName ?: fallbackEmail.substringBefore("@"),
                            role = localRole,
                            assignedRepName = _activeRepName.value
                        )
                        _currentRole.value = localRole
                        _syncStatus.value = "Local Session"
                    }
                    _isAuthenticated.value = true
                }
            }
        }
    }

    val sessionState: StateFlow<SessionUiState> = combine(
        _isAuthenticated,
        _currentUser,
        _currentRole,
        _isLoading,
        _errorMessage,
        _syncStatus
    ) { args: Array<Any?> ->
        val isAuth = args[0] as Boolean
        val user = args[1] as CrmUser?
        val role = args[2] as UserRole
        val loading = args[3] as Boolean
        val error = args[4] as String?
        val sync = args[5] as String

        SessionUiState(
            isAuthenticated = isAuth,
            currentUser = user,
            userRole = role,
            permissions = UserPermissions.fromRole(role),
            isLoading = loading,
            errorMessage = error,
            syncStatus = sync
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SessionUiState()
    )

    fun signInWithEmail(email: String, pass: String, role: UserRole, repName: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please provide email and password"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signInWithEmail(email, pass, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
                _syncStatus.value = "Synced with Firestore"
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Authentication failed"
            }
            _isLoading.value = false
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, role: UserRole, repName: String) {
        if (email.isBlank() || pass.isBlank()) {
            _errorMessage.value = "Please provide email and password"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signUpWithEmail(email, pass, name, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
                _syncStatus.value = "Registered & Synced with Firestore"
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Registration failed"
            }
            _isLoading.value = false
        }
    }

    fun signInWithGoogle(context: Context, role: UserRole, repName: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            val result = authRepository.signInWithGoogle(context, role, repName)
            result.onSuccess { user ->
                _currentUser.value = user
                _currentRole.value = user.role
                _activeRepName.value = user.assignedRepName
                _isAuthenticated.value = true
                _syncStatus.value = "Google Auth Synced with Firestore"
            }.onFailure { error ->
                _errorMessage.value = error.message ?: "Google Sign-In failed"
            }
            _isLoading.value = false
        }
    }

    fun quickDemoLogin(role: UserRole, repName: String = "Nada") {
        val email = if (role == UserRole.SALES_MANAGER) {
            AuthenticationRepository.EXCLUSIVE_MANAGER_EMAIL
        } else {
            "${role.name.lowercase()}@dreamauto.com"
        }
        val effectiveRole = AuthenticationRepository.resolveAuthorizedRole(email, role)

        _currentRole.value = effectiveRole
        _activeRepName.value = repName
        _currentUser.value = CrmUser(
            uid = "demo_${effectiveRole.name.lowercase()}",
            displayName = if (effectiveRole == UserRole.SALES_MANAGER) "Sales Manager (المدير)" else "${effectiveRole.arabicName} ($repName)",
            email = email,
            role = effectiveRole,
            assignedRepName = repName
        )
        _isAuthenticated.value = true
        _errorMessage.value = null
        _syncStatus.value = "Demo ${effectiveRole.displayName} Active"
    }

    fun switchRole(role: UserRole, repName: String = _activeRepName.value) {
        _currentRole.value = role
        _activeRepName.value = repName
        _currentUser.value = _currentUser.value?.copy(
            role = role,
            assignedRepName = repName
        )
    }

    fun logout(context: Context? = null) {
        signOut(context)
    }

    fun signOut(context: Context? = null) {
        viewModelScope.launch {
            authRepository.logout(context)
            _currentUser.value = null
            _isAuthenticated.value = false
            _syncStatus.value = "Signed Out"
        }
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
