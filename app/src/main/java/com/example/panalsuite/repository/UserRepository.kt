package com.example.panalsuite.repository

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableIntStateOf
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.User
import org.json.JSONArray

enum class UserError {
    NotAllowed, MissingFields, EmailTaken, NotFound, CannotModifySelf, LastAdministrator
}

// Los usuarios de prueba se cargan de assets/users.json la primera vez.
// Despues viven en memoria mientras la app esta abierta (igual que tickets y equipos):
// los usuarios que cree el Administrator se pierden al cerrar la app.
class UserRepository(private val context: Context) {
    val lastError: String?
        get() = loadError

    // Leer aqui dentro de un composable hace que se redibuje cuando cambian los usuarios
    fun getUsers(): List<User> {
        ensureLoaded(context)
        version.intValue
        return synchronized(lock) { store.toList() }
    }

    // Id que recibira el proximo usuario creado
    fun nextUserId(): Long {
        ensureLoaded(context)
        version.intValue
        return synchronized(lock) { nextId }
    }

    fun getActiveOperators(): List<User> =
        getUsers().filter { it.role == Role.Operator && it.active }

    // Acepta el email o el id numerico del usuario en el primer campo
    fun authenticate(identifier: String, password: String): User? {
        val key = identifier.trim()
        return getUsers().firstOrNull {
            (it.email.equals(key, ignoreCase = true) || it.idUser.toString() == key) &&
                    it.password == password &&
                    it.active
        }
    }

    // Crea un usuario con el siguiente id incremental. Si no se da email se genera uno.
    fun createUser(
        actor: User,
        name: String,
        lastName: String,
        email: String,
        password: String,
        role: Role
    ): UserError? {
        ensureLoaded(context)
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        val cleanName = name.trim()
        val cleanLast = lastName.trim()
        if (cleanName.isEmpty() || cleanLast.isEmpty() || password.isEmpty()) return UserError.MissingFields
        synchronized(lock) {
            val id = nextId
            val cleanEmail = email.trim().ifEmpty { "$id@gmail.com" }
            if (store.any { it.email.equals(cleanEmail, ignoreCase = true) }) return UserError.EmailTaken
            store.add(
                User(
                    idUser = id,
                    name = cleanName,
                    lastName = cleanLast,
                    email = cleanEmail,
                    password = password,
                    role = role,
                    active = true
                )
            )
            nextId = id + 1
        }
        version.intValue++
        return null
    }

    fun updateUser(
        actor: User,
        idUser: Long,
        name: String,
        lastName: String,
        email: String,
        password: String,
        role: Role,
        active: Boolean
    ): UserError? {
        ensureLoaded(context)
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        val cleanName = name.trim()
        val cleanLast = lastName.trim()
        if (cleanName.isEmpty() || cleanLast.isEmpty() || password.isEmpty()) return UserError.MissingFields

        val updated: User
        val previous: User
        synchronized(lock) {
            val index = store.indexOfFirst { it.idUser == idUser }
            if (index < 0) return UserError.NotFound
            previous = store[index]

            val cleanEmail = email.trim().ifEmpty { previous.email }
            if (store.any { it.idUser != idUser && it.email.equals(cleanEmail, ignoreCase = true) }) {
                return UserError.EmailTaken
            }
            // Un admin no puede cambiarse el rol ni desactivarse a si mismo
            if (idUser == actor.idUser && (role != previous.role || !active)) {
                return UserError.CannotModifySelf
            }
            if (previous.role == Role.Administrator && previous.active &&
                (role != Role.Administrator || !active) && activeAdminCount() <= 1
            ) {
                return UserError.LastAdministrator
            }
            updated = previous.copy(
                name = cleanName,
                lastName = cleanLast,
                email = cleanEmail,
                password = password,
                role = role,
                active = active
            )
            store[index] = updated
        }
        // Si deja de ser un operator activo sus tickets quedan sin asignar
        if (previous.role == Role.Operator && (updated.role != Role.Operator || !updated.active)) {
            TicketRepository.releaseTicketsOf(idUser, "${previous.name} ${previous.lastName}", actor)
        }
        if (Session.currentUser?.idUser == idUser) Session.currentUser = updated
        version.intValue++
        return null
    }

    fun deleteUser(actor: User, idUser: Long): UserError? {
        ensureLoaded(context)
        if (Permissions.ManageUsers !in actor.role.permits) return UserError.NotAllowed
        val target: User
        synchronized(lock) {
            target = store.firstOrNull { it.idUser == idUser } ?: return UserError.NotFound
            if (idUser == actor.idUser) return UserError.CannotModifySelf
            if (target.role == Role.Administrator && target.active && activeAdminCount() <= 1) {
                return UserError.LastAdministrator
            }
            store.remove(target)
        }
        TicketRepository.releaseTicketsOf(idUser, "${target.name} ${target.lastName}", actor)
        version.intValue++
        return null
    }

    companion object {
        private val lock = Any()
        private val store = mutableListOf<User>()
        private var loaded = false
        private var nextId = 1L
        private var loadError: String? = null
        private val version = mutableIntStateOf(0)

        private fun activeAdminCount() = store.count { it.role == Role.Administrator && it.active }

        private fun ensureLoaded(context: Context) {
            synchronized(lock) {
                if (loaded) return
                loaded = true
                loadError = null
                try {
                    val json = context.assets.open("users.json")
                        .bufferedReader()
                        .use { it.readText() }
                    val array = JSONArray(json)

                    for (i in 0 until array.length()) {
                        try {
                            val o = array.getJSONObject(i)
                            store.add(
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
                            loadError = "Usuario #$i: ${t::class.java.simpleName}: ${t.message}"
                        }
                    }

                    if (store.isEmpty() && loadError == null) {
                        val assets = context.assets.list("")?.joinToString() ?: "?"
                        loadError = "users.json leído: ${json.length} caracteres, " +
                                "${array.length()} elementos.\nInicio: ${json.take(60).replace("\n", " ")}\n" +
                                "assets: [$assets]"
                    }
                } catch (t: Throwable) {
                    Log.e("UserRepository", "No se pudo leer users.json", t)
                    loadError = "users.json: ${t::class.java.simpleName}: ${t.message}"
                }
                nextId = (store.maxOfOrNull { it.idUser } ?: 0L) + 1
            }
        }
    }
}
