package com.example.panalsuite.repository

import com.example.panalsuite.model.User

object Session {
    var currentUser: User? = null
        set(value) {
            field = value
            Persistence.saveSession(value?.idUser)
        }
}