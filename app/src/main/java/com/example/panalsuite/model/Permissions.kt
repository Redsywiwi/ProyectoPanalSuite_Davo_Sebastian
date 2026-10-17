package com.example.panalsuite.model

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
    ManageUsers
}

enum class Role(val permits: Set<Permissions>){
    CommonUser(
        setOf(Permissions.OpenTicket, Permissions.ViewTickets, Permissions.ViewHistory)
    ),
    Operator(
        CommonUser.permits + setOf(
            Permissions.ViewSystemTray, Permissions.ChangeStatus, Permissions.Comment, Permissions.AttachEvidence
        )
    ),
    Supervisor(
        //antes de cualquier cosa aquí se pondría setOf<permisos>() pero como hay un solo tipo de dato
        //el programa infiere que es solamente Permiso
      Operator.permits + setOf(
          Permissions.SeeWorkload, Permissions.Reassign
      )
    ),
    Management(
        setOf(Permissions.ViewTickets , Permissions.ViewSummary, Permissions.ViewHistory)
    ),
    Administrator(
        setOf(Permissions.ManageUsers)
    )
}