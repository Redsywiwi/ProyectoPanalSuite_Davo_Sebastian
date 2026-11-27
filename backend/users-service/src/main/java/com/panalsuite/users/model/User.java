package com.panalsuite.users.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private Long idUser;
    private String name;
    private String lastName;
    @Indexed(unique = true)
    private String email;
    private String passwordHash;
    private Role role;
    private boolean active;

    public User() {}

    public User(Long idUser, String name, String lastName, String email, String passwordHash, Role role, boolean active) {
        this.idUser = idUser;
        this.name = name;
        this.lastName = lastName;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
        this.active = active;
    }

    public Long getIdUser() { return idUser; }
    public void setIdUser(Long idUser) { this.idUser = idUser; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
