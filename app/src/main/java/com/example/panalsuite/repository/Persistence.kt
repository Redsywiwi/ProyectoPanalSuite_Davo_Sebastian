package com.example.panalsuite.repository

import android.content.Context
import androidx.compose.runtime.snapshotFlow
import com.example.panalsuite.model.Commentary
import com.example.panalsuite.model.DevelopmentHistory
import com.example.panalsuite.model.Evidences
import com.example.panalsuite.model.Location
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Role
import com.example.panalsuite.model.RolePermissions
import com.example.panalsuite.model.Team
import com.example.panalsuite.model.Ticket
import com.example.panalsuite.model.TicketStatus
import com.example.panalsuite.model.Urgency
import com.example.panalsuite.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.time.LocalDateTime

// Guarda el estado de la app como archivos JSON en el almacenamiento interno (filesDir)
// - restoreAll(): se llama una vez al abrir la app
// - startAutoSave(): observa los repositorios y reescribe el archivo cada vez que algo cambia
object Persistence {
    private var dir: File? = null

    fun init(context: Context) {
        if (dir == null) dir = File(context.applicationContext.filesDir, "panalsuite").apply { mkdirs() }
    }


    @Synchronized
    private fun write(name: String, text: String) {
        val d = dir ?: return
        val tmp = File(d, "$name.tmp")
        tmp.writeText(text)
        val target = File(d, name)
        if (!tmp.renameTo(target)) {
            target.writeText(text)
            tmp.delete()
        }
    }

    @Synchronized
    private fun read(name: String): String? {
        val f = File(dir ?: return null, name)
        return if (f.exists()) f.readText() else null
    }


    fun restoreAll(context: Context) {
        ParametersRepository.getCategories()
        TicketRepository.snapshot()
        TeamRepository.snapshot()
        RolePermissions.snapshot()
        UserRepository(context).getUsers()
        init(context)
        loadTickets()?.let { TicketRepository.restore(it) }
        loadTeams()?.let { TeamRepository.restore(it) }
        loadParameters()?.let { (cats, locs) -> ParametersRepository.restore(cats, locs) }
        loadPermissions()?.let { RolePermissions.restore(it) }
        DemoData.loaded = read("meta.json")?.let { JSONObject(it).optBoolean("demoLoaded", false) } ?: false
        // La sesion se restaura al final porque necesita los usuarios ya cargados
        val savedId = loadSessionUserId()
        if (savedId != null) {
            Session.currentUser = UserRepository(context).getUsers()
                .firstOrNull { it.idUser == savedId && it.active }
        }
    }

    fun startAutoSave(scope: CoroutineScope, context: Context) {
        val users = UserRepository(context)
        scope.launch(Dispatchers.IO) {
            snapshotFlow { TicketRepository.snapshot() }.collect { saveTickets(it) }
        }
        scope.launch(Dispatchers.IO) {
            snapshotFlow { TeamRepository.snapshot() }.collect { saveTeams(it) }
        }
        scope.launch(Dispatchers.IO) {
            snapshotFlow { users.getUsers() }.collect { saveUsers(it) }
        }
        scope.launch(Dispatchers.IO) {
            snapshotFlow { ParametersRepository.getCategories() to ParametersRepository.getLocations() }
                .collect { (c, l) -> saveParameters(c, l) }
        }
        scope.launch(Dispatchers.IO) {
            snapshotFlow { RolePermissions.snapshot() }.collect { savePermissions(it) }
        }
    }
    fun saveSession(userId: Long?) {
        write("session.json", JSONObject().apply { if (userId != null) put("userId", userId) }.toString())
    }

    private fun loadSessionUserId(): Long? =
        read("session.json")?.let { JSONObject(it).let { o -> if (o.has("userId")) o.getLong("userId") else null } }

    fun saveDemoLoaded(value: Boolean) {
        write("meta.json", JSONObject().put("demoLoaded", value).toString())
    }



    private fun saveUsers(users: List<User>) {
        val arr = JSONArray()
        users.forEach {
            arr.put(
                JSONObject()
                    .put("idUser", it.idUser).put("name", it.name).put("lastName", it.lastName)
                    .put("email", it.email).put("password", it.password)
                    .put("role", it.role.name).put("active", it.active)
            )
        }
        write("users.json", arr.toString())
    }

    fun loadUsers(): List<User>? = runCatching {
        val arr = JSONArray(read("users.json") ?: return null)
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            User(
                idUser = o.getLong("idUser"), name = o.getString("name"), lastName = o.getString("lastName"),
                email = o.getString("email"), password = o.getString("password"),
                role = Role.valueOf(o.getString("role")), active = o.getBoolean("active")
            )
        }
    }.getOrNull()

    private fun saveTickets(tickets: List<Ticket>) {
        val arr = JSONArray()
        tickets.forEach { t ->
            val o = JSONObject()
                .put("idTicket", t.idTicket).put("idUser", t.idUser)
                .put("title", t.title).put("description", t.description)
                .put("category", t.category).put("type", t.type)
                .put("priority", t.priority.name).put("impact", t.impact)
                .put("status", t.status.name)
                .put("createdAt", t.createdAt.toString()).put("lastUpdate", t.lastUpdate.toString())
                .put("branchId", t.branch.idLocation).put("branchName", t.branch.name)
                .put("pinned", t.pinned)
            t.team?.let { o.put("team", it) }
            t.peopleInCharge?.let { o.put("peopleInCharge", it) }
            o.put("commentary", JSONArray().also { a ->
                t.commentary.forEach {
                    a.put(
                        JSONObject().put("id", it.idCommentary).put("idTicket", it.idTicket)
                            .put("idUser", it.idUser).put("text", it.text)
                            .put("date", it.commentaryDate.toString())
                    )
                }
            })
            o.put("history", JSONArray().also { a ->
                t.developmentHistory.forEach {
                    a.put(
                        JSONObject().put("id", it.idDevelopment).put("field", it.modifiedField)
                            .put("prev", it.previousValue).put("new", it.newValue)
                            .put("idUser", it.idUser).put("date", it.date.toString())
                    )
                }
            })
            o.put("evidences", JSONArray().also { a ->
                t.evidences.forEach {
                    val e = JSONObject().put("id", it.idEvidence).put("idTicket", it.idTicket)
                        .put("idUser", it.idUser).put("url", it.url)
                        .put("date", it.creationDate.toString())
                    it.description?.let { d -> e.put("description", d) }
                    a.put(e)
                }
            })
            arr.put(o)
        }
        write("tickets.json", arr.toString())
    }

    private fun loadTickets(): List<Ticket>? = runCatching {
        val arr = JSONArray(read("tickets.json") ?: return null)
        (0 until arr.length()).map { i ->
            val o = arr.getJSONObject(i)
            Ticket(
                idTicket = o.getLong("idTicket"), idUser = o.getLong("idUser"),
                title = o.getString("title"), description = o.getString("description"),
                category = o.getString("category"), type = o.getString("type"),
                priority = Urgency.valueOf(o.getString("priority")), impact = o.getString("impact"),
                status = TicketStatus.valueOf(o.getString("status")),
                createdAt = LocalDateTime.parse(o.getString("createdAt")),
                lastUpdate = LocalDateTime.parse(o.getString("lastUpdate")),
                branch = Location(o.getLong("branchId"), o.getString("branchName")),
                team = if (o.has("team")) o.getString("team") else null,
                peopleInCharge = if (o.has("peopleInCharge")) o.getLong("peopleInCharge") else null,
                commentary = o.getJSONArray("commentary").let { a ->
                    (0 until a.length()).map {
                        val c = a.getJSONObject(it)
                        Commentary(
                            c.getLong("id"), c.getLong("idTicket"), c.getLong("idUser"),
                            c.getString("text"), LocalDateTime.parse(c.getString("date"))
                        )
                    }.toMutableList()
                },
                developmentHistory = o.getJSONArray("history").let { a ->
                    (0 until a.length()).map {
                        val h = a.getJSONObject(it)
                        DevelopmentHistory(
                            h.getLong("id"), h.getString("field"), h.getString("prev"),
                            h.getString("new"), h.getLong("idUser"), LocalDateTime.parse(h.getString("date"))
                        )
                    }.toMutableList()
                },
                evidences = o.getJSONArray("evidences").let { a ->
                    (0 until a.length()).map {
                        val e = a.getJSONObject(it)
                        Evidences(
                            e.getLong("id"), e.getLong("idTicket"), e.getLong("idUser"), e.getString("url"),
                            if (e.has("description")) e.getString("description") else null,
                            LocalDateTime.parse(e.getString("date"))
                        )
                    }.toMutableList()
                },
                pinned = o.optBoolean("pinned", false)
            )
        }
    }.getOrNull()

    private fun saveTeams(teams: List<Team>) {
        val arr = JSONArray()
        teams.forEach {
            arr.put(
                JSONObject().put("idTeam", it.idTeam).put("name", it.name)
                    .put("memberIds", JSONArray(it.memberIds))
            )
        }
        write("teams.json", arr.toString())
    }

    private fun loadTeams(): List<Team>? = runCatching {
        val arr = JSONArray(read("teams.json") ?: return null)
        (0 until arr.length()).map {
            val o = arr.getJSONObject(it)
            val m = o.getJSONArray("memberIds")
            Team(o.getLong("idTeam"), o.getString("name"), (0 until m.length()).map { j -> m.getLong(j) })
        }
    }.getOrNull()

    private fun saveParameters(categories: List<String>, locations: List<Location>) {
        val o = JSONObject()
            .put("categories", JSONArray(categories))
            .put("locations", JSONArray().also { a ->
                locations.forEach { a.put(JSONObject().put("id", it.idLocation).put("name", it.name)) }
            })
        write("parameters.json", o.toString())
    }

    private fun loadParameters(): Pair<List<String>, List<Location>>? = runCatching {
        val o = JSONObject(read("parameters.json") ?: return null)
        val c = o.getJSONArray("categories")
        val l = o.getJSONArray("locations")
        (0 until c.length()).map { c.getString(it) } to
                (0 until l.length()).map { Location(l.getJSONObject(it).getLong("id"), l.getJSONObject(it).getString("name")) }
    }.getOrNull()

    private fun savePermissions(overrides: Map<Role, Set<Permissions>>) {
        val o = JSONObject()
        overrides.forEach { (role, set) -> o.put(role.name, JSONArray(set.map { it.name })) }
        write("permissions.json", o.toString())
    }

    private fun loadPermissions(): Map<Role, Set<Permissions>>? = runCatching {
        val o = JSONObject(read("permissions.json") ?: return null)
        o.keys().asSequence().associate { key ->
            val a = o.getJSONArray(key)
            Role.valueOf(key) to (0 until a.length()).map { Permissions.valueOf(a.getString(it)) }.toSet()
        }
    }.getOrNull()
}
