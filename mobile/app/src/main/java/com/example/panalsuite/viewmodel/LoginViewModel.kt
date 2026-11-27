package com.example.panalsuite.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.panalsuite.model.User
import com.example.panalsuite.remote.ApiResult
import com.example.panalsuite.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LoginUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val user: User? = null
)

// Login validado por users-service. La navegacion la hace la pantalla cuando "user" deja de ser null.
class LoginViewModel(
    private val auth: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(LoginUiState())
    val state: StateFlow<LoginUiState> = _state.asStateFlow()

    fun login(identifier: String, password: String) {
        if (_state.value.loading) return
        if (identifier.isBlank() || password.isEmpty()) {
            _state.value = LoginUiState(error = "Ingresa tu correo y contraseña")
            return
        }
        _state.value = LoginUiState(loading = true)
        viewModelScope.launch {
            _state.value = when (val result = auth.login(identifier, password)) {
                is ApiResult.Success -> LoginUiState(user = result.data)
                is ApiResult.Failure -> LoginUiState(error = result.message)
            }
        }
    }

    // Se llama despues de navegar para no reaccionar dos veces al mismo login
    fun consumeLogin() {
        _state.value = LoginUiState()
    }
}
