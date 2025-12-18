package com.example.personal_finance_tracker.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class AuthUiState(
    val loading: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel : ViewModel() {
    private val repo: AuthRepository = AuthRepository()
    private val _ui = MutableStateFlow(AuthUiState())
    val ui: StateFlow<AuthUiState> = _ui.asStateFlow()

    val status: StateFlow<AuthStatus> =
        repo.authState()
            .map { user ->
                if (user == null) AuthStatus.LoggedOut
                else AuthStatus.LoggedIn(user.uid, user.email)
            }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AuthStatus.Loading)

    fun consumeError() { _ui.update { it.copy(errorMessage = null) } }
    fun signOut() = repo.signOut()

    fun signIn(email: String, password: String) {
        val e = email.trim()
        val p = password

        validateOrError(e, p)?.let { msg ->
            _ui.update { it.copy(errorMessage = msg) }
            return
        }

        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            try {
                repo.firebaseAuth().signInWithEmailAndPassword(e, p).await()
                _ui.value = AuthUiState(loading = false)
            } catch (ex: Exception) {
                _ui.value = AuthUiState(loading = false, errorMessage = mapAuthError(ex))
            }
        }
    }

    fun signUp(email: String, password: String) {
        val e = email.trim()
        val p = password

        validateOrError(e, p)?.let { msg ->
            _ui.update { it.copy(errorMessage = msg) }
            return
        }

        viewModelScope.launch {
            _ui.value = AuthUiState(loading = true)
            try {
                repo.firebaseAuth().createUserWithEmailAndPassword(e, p).await()
                _ui.value = AuthUiState(loading = false)
            } catch (ex: Exception) {
                _ui.value = AuthUiState(loading = false, errorMessage = mapAuthError(ex))
            }
        }
    }

    private fun validateOrError(email: String, password: String): String? {
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) return "Invalid email format"
        if (password.length < 6) return "Password must be at least 6 characters"
        return null
    }

    private fun mapAuthError(e: Exception): String {
        return when (e) {
            is FirebaseNetworkException -> "Network error. Please try again."
            is FirebaseAuthInvalidUserException -> "Account not found."
            is FirebaseAuthInvalidCredentialsException -> "Incorrect email or password."
            is FirebaseAuthUserCollisionException -> "Email is already in use."
            else -> e.message ?: "Authentication failed."
        }
    }
}
