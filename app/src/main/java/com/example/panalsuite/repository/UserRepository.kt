package com.example.panalsuite.repository

import android.content.Context
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.User
import org.json.JSONArray

class UserRepository(private val context: Context) {

    fun getUsers(): List<User> {
        val json = context.assets.open("users.json")
            .bufferedReader()
            .use { it.readText() }

        val array = JSONArray(json)
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            User(
                idUser = o.getLong("idUser"),
                name = o.getString("name"),
                lastName = o.getString("lastName"),
                email = o.getString("email"),
                password = o.getString("password"),
                role = Role.valueOf(o.getString("role")),
                active = o.getBoolean("active")
            )
        }
    }

    // Devuelve el usuario si las credenciales son válidas, o null si no
    fun authenticate(email: String, password: String): User? =
        getUsers().firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) &&
                    it.password == password &&
                    it.active
        }
}