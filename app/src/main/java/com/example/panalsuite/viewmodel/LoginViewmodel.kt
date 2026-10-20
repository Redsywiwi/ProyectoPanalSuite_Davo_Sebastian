package com.example.panalsuite.viewmodel

import androidx.lifecycle.ViewModel
import com.example.panalsuite.model.User
import com.example.panalsuite.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow


class LoginViewmodel(
    private val repository: UserRepository = UserRepository(
        context = TODO()
    )
) : ViewModel() {
    private val _activeUser = MutableStateFlow<List<User>>(emptyList())
    val menuItems: StateFlow<List<User>> = _activeUser.asStateFlow()
}