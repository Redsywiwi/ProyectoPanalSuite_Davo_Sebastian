package com.panalsuite.tickets.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "locations")
public class Location {
    @Id
    private Long idLocation;
    private String name;

    public Location() {}

    public Location(Long idLocation, String name) {
        this.idLocation = idLocation;
        this.name = name;
    }

    public Long getIdLocation() { return idLocation; }
    public void setIdLocation(Long idLocation) { this.idLocation = idLocation; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
