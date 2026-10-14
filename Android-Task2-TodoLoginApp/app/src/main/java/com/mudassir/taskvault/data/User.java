package com.mudassir.taskvault.data;

/**
 * Model class representing a registered user in TaskVault.
 */
public class User {
    private final long id;
    private final String name;
    private final String email;
    private final String passwordHash;
    private final String salt;
    private final long createdAt;

    public User(long id, String name, String email, String passwordHash, String salt, long createdAt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getSalt() {
        return salt;
    }

    public long getCreatedAt() {
        return createdAt;
    }
}
