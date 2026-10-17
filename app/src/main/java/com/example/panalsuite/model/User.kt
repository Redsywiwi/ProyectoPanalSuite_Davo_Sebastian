package com.example.panalsuite.model

data class User (
    val idUser: Long,
    val name: String,
    val lastName: String,
    var email: String,
    var role: Role,
    var active: Boolean
    //se asigna uno de los roles y de ahi con un if se decide que puede acceder y que no wijiji
)