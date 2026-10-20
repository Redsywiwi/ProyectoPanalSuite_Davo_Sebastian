package com.example.panalsuite.repository

import android.content.Context
import android.util.Log
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.User
import org.json.JSONArray

class UserRepository(private val context: Context) {
    var lastError: String? = null
        private set

    fun getUsers(): List<User> {
        lastError = null
        val users = mutableListOf<User>()

        try {
            val json = context.assets.open("users.json")
                .bufferedReader()
                .use { it.readText() }
            val array = JSONArray(json)

            for (i in 0 until array.length()) {
                try {
                    val o = array.getJSONObject(i)
                    users.add(
                        User(
                            idUser = o.getLong("idUser"),
                            name = o.getString("name"),
                            lastName = o.getString("lastName"),
                            email = o.getString("email"),
                            password = o.getString("password"),
                            role = Role.valueOf(o.getString("role")),
                            active = o.getBoolean("active")
                        )
                    )
                } catch (t: Throwable) {
                    Log.e("UserRepository", "Usuario #$i inválido en users.json", t)
                    lastError = "Usuario #$i: ${t::class.java.simpleName}: ${t.message}"
                }
            }

            if (users.isEmpty() && lastError == null) {
                val assets = context.assets.list("")?.joinToString() ?: "?"
                lastError = "users.json leído: ${json.length} caracteres, " +
                        "${array.length()} elementos.\nInicio: ${json.take(60).replace("\n", " ")}\n" +
                        "assets: [$assets]"
            }
        } catch (t: Throwable) {
            Log.e("UserRepository", "No se pudo leer users.json", t)
            lastError = "users.json: ${t::class.java.simpleName}: ${t.message}"
        }
        return users
    }

    fun getActiveOperators(): List<User> =
        getUsers().filter { it.role == Role.Operator && it.active }
    fun authenticate(email: String, password: String): User? =
        getUsers().firstOrNull {
            it.email.equals(email.trim(), ignoreCase = true) &&
                    it.password == password &&
                    it.active
        }
}
