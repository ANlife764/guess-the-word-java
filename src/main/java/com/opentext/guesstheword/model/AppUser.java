package com.opentext.guesstheword.model;

import jakarta.persistence.*;

/**
 * A registered user (admin or player). "user" is a reserved word in several
 * SQL dialects (including H2), so the table is named "app_user".
 */
@Entity
@Table(name = "app_user")
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    /** Lower-cased copy of username, used to enforce case-insensitive uniqueness. */
    @Column(nullable = false, unique = true)
    private String usernameLower;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    protected AppUser() {
        // for JPA
    }

    public AppUser(String username, String passwordHash, Role role) {
        setUsername(username);
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
        this.usernameLower = username == null ? null : username.toLowerCase();
    }

    public String getUsernameLower() {
        return usernameLower;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public Role getRole() {
        return role;
    }
}
