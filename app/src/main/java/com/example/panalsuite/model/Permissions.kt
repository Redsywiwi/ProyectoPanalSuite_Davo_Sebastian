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