package com.example.panalsuite.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Team
import com.example.panalsuite.model.User

// viven en memoria mientras la app esta abierta
object TeamRepository {
    private val teams = mutableStateListOf<Team>()
    private var nextTeamId = 1L

    fun getTeams(): List<Team> = teams.sortedBy { it.name.lowercase() }

    fun getTeam(idTeam: Long): Team? = teams.firstOrNull { it.idTeam == idTeam }

    // Crea un equipo. Requiere ManageTeams.
    @Synchronized
    fun createTeam(actor: User, name: String, memberIds: List<Long>): Team? {
        if (Permissions.ManageTeams !in actor.role.permits) return null
        val clean = name.trim()
        if (clean.isEmpty() || isNameTaken(clean, exceptId = null)) return null
        val team = Team(
            idTeam = nextTeamId++,
            name = clean,
            memberIds = memberIds.distinct()
        )
        teams.add(team)
        return team
    }

    // Edita nombre y miembros de un equipo. Requiere ManageTeams.
    @Synchronized
    fun updateTeam(actor: User, idTeam: Long, name: String, memberIds: List<Long>): Boolean {
        if (Permissions.ManageTeams !in actor.role.permits) return false
        val index = teams.indexOfFirst { it.idTeam == idTeam }
        if (index < 0) return false
        val clean = name.trim()
        if (clean.isEmpty() || isNameTaken(clean, exceptId = idTeam)) return false
        teams[index] = teams[index].copy(name = clean, memberIds = memberIds.distinct())
        return true
    }

    // Elimina un equipo. Requiere ManageTeams.
    @Synchronized
    fun deleteTeam(actor: User, idTeam: Long): Boolean {
        if (Permissions.ManageTeams !in actor.role.permits) return false
        return teams.removeAll { it.idTeam == idTeam }
    }

    private fun isNameTaken(name: String, exceptId: Long?): Boolean =
        teams.any { it.idTeam != exceptId && it.name.equals(name, ignoreCase = true) }
}
