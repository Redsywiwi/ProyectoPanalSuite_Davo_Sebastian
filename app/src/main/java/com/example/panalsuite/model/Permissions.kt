package com.example.panalsuite.model

import androidx.compose.runtime.mutableStateMapOf

enum class Permissions {
    OpenTicket,
    ViewTickets,
    ViewHistory,
    ViewSystemTray,
    ChangeStatus,
    Comment,
    AttachEvidence,
    SeeWorkload,
    Reassign,
    ViewSummary,
    ManageUsers,
    ManageTeams,
    ViewDashboard,
    ManagePermissions,
    ManageParameters
}

// defaultPermits son los permisos "de fabrica" de cada rol.
// permits son los permisos vigentes: los de fabrica, salvo que el Administrator los haya editado.
// Todo el codigo existente sigue usando role.permits sin cambios.
enum class Role(val defaultPermits: Set<Permissions>) {
    CommonUser(
        setOf(Permissions.OpenTicket, Permissions.ViewTickets, Permissions.ViewHistory)
    ),
    Operator(
        CommonUser.defaultPermits + setOf(
            Permissions.ViewSystemTray, Permissions.ChangeStatus, Permissions.Comment, Permissions.AttachEvidence
        )
    ),
    Supervisor(
        Operator.defaultPermits + setOf(
            Permissions.SeeWorkload, Permissions.Reassign, Permissions.ManageTeams
        )
    ),
    Management(
        setOf(Permissions.ViewDashboard, Permissions.ViewSummary)
    ),
    Administrator(
        setOf(Permissions.ManageUsers, Permissions.ManagePermissions, Permissions.ManageParameters)
    );

    val permits: Set<Permissions>
        get() = RolePermissions.of(this)
}

// Permisos editables por rol. Viven en memoria mientras la app esta abierta.
object RolePermissions {
    private val overrides = mutableStateMapOf<Role, Set<Permissions>>()

    // El Administrator siempre conserva estos permisos para no quedarse sin acceso al panel.
    val lockedForAdministrator: Set<Permissions> =
        setOf(Permissions.ManageUsers, Permissions.ManagePermissions, Permissions.ManageParameters)

    fun of(role: Role): Set<Permissions> = overrides[role] ?: role.defaultPermits

    fun isCustomized(role: Role): Boolean = of(role) != role.defaultPermits

    fun isLocked(role: Role, permission: Permissions): Boolean =
        role == Role.Administrator && permission in lockedForAdministrator

    @Synchronized
    fun update(actor: User, role: Role, permits: Set<Permissions>): Boolean {
        if (Permissions.ManagePermissions !in actor.role.permits) return false
        val result = if (role == Role.Administrator) permits + lockedForAdministrator else permits
        if (result == role.defaultPermits) overrides.remove(role) else overrides[role] = result
        return true
    }

    @Synchronized
    fun reset(actor: User, role: Role): Boolean {
        if (Permissions.ManagePermissions !in actor.role.permits) return false
        overrides.remove(role)
        return true
    }
}
