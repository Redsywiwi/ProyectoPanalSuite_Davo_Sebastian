package com.example.panalsuite.model

data class usuario (
    val idUsuario: Long,
    val nombre: String,
    val apellidos: String,
    var correo: String,
    var rol: String,
    var activo: Boolean
    //se asigna uno de los roles y de ahi con un if se decide que puede acceder y que no wijiji
)