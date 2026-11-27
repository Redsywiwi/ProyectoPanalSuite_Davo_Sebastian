package com.panalsuite.users.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "teams")
public class Team {
    @Id
    private Long idTeam;
    private String name;
    private List<Long> memberIds = new ArrayList<>();

    public Team() {}

    public Team(Long idTeam, String name, List<Long> memberIds) {
        this.idTeam = idTeam;
        this.name = name;
        this.memberIds = memberIds;
    }

    public Long getIdTeam() { return idTeam; }
    public void setIdTeam(Long idTeam) { this.idTeam = idTeam; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}
