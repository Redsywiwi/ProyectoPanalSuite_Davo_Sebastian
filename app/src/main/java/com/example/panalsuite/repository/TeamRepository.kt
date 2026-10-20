package com.example.panalsuite.repository

import androidx.compose.runtime.mutableStateListOf
import com.example.panalsuite.model.Permissions
import com.example.panalsuite.model.Team
import com.example.panalsuite.model.User

object TeamRepository {
    private val teams = mutableStateListOf<Team>()
    private var nextTeamId = 1L

    fun snapshot(): List<Team> = teams.toList()

    @Synchronized
    fun restore(saved: List<Team>) {
        teams.clear()
        teams.addAll(saved)
        nextTeamId = (saved.maxOfOrNull { it.idTeam } ?: 0L) + 1
    }

    fun getTeams(): List<Team> = teams.sortedBy { it.name.lowercase() }

    fun getTeam(idTeam: Long): Team? = teams.firstOrNull { it.idTeam == idTeam }

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

    @Synchronized
    fun importTeam(name: String, memberIds: List<Long>): Team? {
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

    @Synchronized
    fun deleteTeam(actor: User, idTeam: Long): Boolean {
        if (Permissions.ManageTeams !in actor.role.permits) return false
        return teams.removeAll { it.idTeam == idTeam }
    }

    private fun isNameTaken(name: String, exceptId: Long?): Boolean =
        teams.any { it.idTeam != exceptId && it.name.equals(name, ignoreCase = true) }
}
