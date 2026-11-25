package com.example.doan.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.doan.model.User
import com.example.doan.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class AuthMode {
    LOGIN, REGISTER, FORGOT_PASSWORD
}

data class AuthUiState(
    val mode: AuthMode = AuthMode.LOGIN,
    val displayName: String = "",
    val phone: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val rememberMe: Boolean = false,
    val isPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val loggedInUser: User? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun switchMode(newMode: AuthMode) {
        _uiState.update {
            it.copy(
                mode = newMode,
                errorMessage = null,
                successMessage = null,
                password = "",
                confirmPassword = ""
            )
        }
    }

    fun onDisplayNameChange(value: String) =
        _uiState.update { it.copy(displayName = value, errorMessage = null) }

    fun onPhoneChange(value: String) =
        _uiState.update { it.copy(phone = value, errorMessage = null) }

    fun onPasswordChange(value: String) =
        _uiState.update { it.copy(password = value, errorMessage = null) }

    fun onConfirmPasswordChange(value: String) =
        _uiState.update { it.copy(confirmPassword = value, errorMessage = null) }

    fun onRememberMeChange(checked: Boolean) =
        _uiState.update { it.copy(rememberMe = checked) }

    fun togglePasswordVisibility() =
        _uiState.update { it.copy(isPasswordVisible = !it.isPasswordVisible) }

    fun toggleConfirmPasswordVisibility() =
        _uiState.update { it.copy(isConfirmPasswordVisible = !it.isConfirmPasswordVisible) }

    fun submit() {
        val state = _uiState.value
        if (state.isLoading) return

        when (state.mode) {
            AuthMode.LOGIN -> handleLogin(state)
            AuthMode.REGISTER -> handleRegister(state)
            AuthMode.FORGOT_PASSWORD -> handleForgotPassword(state)
        }
    }

    private fun handleLogin(state: AuthUiState) {
        if (state.phone.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số điện thoại và mật khẩu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.login(state.phone.trim(), state.password).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    private fun handleRegister(state: AuthUiState) {
        when {
            state.displayName.trim().length < 2 -> {
                _uiState.update { it.copy(errorMessage = "Tên người dùng phải có ít nhất 2 ký tự.") }
                return
            }
            state.phone.isBlank() -> {
                _uiState.update { it.copy(errorMessage = "Vui lòng nhập số điện thoại.") }
                return
            }
            state.password.length < 6 -> {
                _uiState.update { it.copy(errorMessage = "Mật khẩu phải từ 6 ký tự trở lên.") }
                return
            }
            state.password != state.confirmPassword -> {
                _uiState.update { it.copy(errorMessage = "Mật khẩu xác nhận không khớp.") }
                return
            }
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.register(
                displayName = state.displayName.trim(),
                phone = state.phone.trim(),
                password = state.password
            ).fold(
                onSuccess = { msg ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            mode = AuthMode.LOGIN,
                            displayName = "",
                            password = "",
                            confirmPassword = "",
                            successMessage = msg
                        )
                    }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    private fun handleForgotPassword(state: AuthUiState) {
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Vui lòng nhập số điện thoại đã đăng ký.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            repository.sendResetOtp(state.phone.trim()).fold(
                onSuccess = { msg ->
                    _uiState.update { it.copy(isLoading = false, successMessage = msg) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }

    fun loginSocial(provider: String) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            repository.loginWithSocial(provider).fold(
                onSuccess = { user ->
                    _uiState.update { it.copy(isLoading = false, loggedInUser = user) }
                },
                onFailure = { err ->
                    _uiState.update { it.copy(isLoading = false, errorMessage = err.message) }
                }
            )
        }
    }
}