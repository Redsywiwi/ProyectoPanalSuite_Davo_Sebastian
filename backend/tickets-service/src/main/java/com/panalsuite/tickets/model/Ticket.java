package com.panalsuite.tickets.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "tickets")
public class Ticket {
    @Id
    private Long idTicket;
    private Long idUser;
    private String title;
    private String description;
    private String category;
    private String type;
    private Urgency priority;
    private String impact;
    private TicketStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdate;
    private Branch branch;
    private String team;
    private Long peopleInCharge;
    private boolean pinned;
    private List<Commentary> commentary = new ArrayList<>();
    private List<DevelopmentEntry> developmentHistory = new ArrayList<>();
    private List<Evidence> evidences = new ArrayList<>();

    public Ticket() {}

    public Long getIdTicket() { return idTicket; }
    public void setIdTicket(Long idTicket) { this.idTicket = idTicket; }

    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Urgency getPriority() { return priority; }
    public void setPriority(Urgency priority) { this.priority = priority; }

    public String getImpact() { return impact; }
    public void setImpact(String impact) { this.impact = impact; }

    public TicketStatus getStatus() { return status; }
    public void setStatus(TicketStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getLastUpdate() { return lastUpdate; }
    public void setLastUpdate(LocalDateTime lastUpdate) { this.lastUpdate = lastUpdate; }

    public Branch getBranch() { return branch; }
    public void setBranch(Branch branch) { this.branch = branch; }

    public String getTeam() { return team; }
    public void setTeam(String team) { this.team = team; }

    public Long getPeopleInCharge() { return peopleInCharge; }
    public void setPeopleInCharge(Long peopleInCharge) { this.peopleInCharge = peopleInCharge; }

    public boolean isPinned() { return pinned; }
    public void setPinned(boolean pinned) { this.pinned = pinned; }

    public List<Commentary> getCommentary() { return commentary; }
    public void setCommentary(List<Commentary> commentary) { this.commentary = commentary; }

    public List<DevelopmentEntry> getDevelopmentHistory() { return developmentHistory; }
    public void setDevelopmentHistory(List<DevelopmentEntry> developmentHistory) { this.developmentHistory = developmentHistory; }

    public List<Evidence> getEvidences() { return evidences; }
    public void setEvidences(List<Evidence> evidences) { this.evidences = evidences; }
}
