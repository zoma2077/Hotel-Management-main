package com.cse241.hotel.model.user;

import com.cse241.hotel.enums.Role;

import java.util.Objects;

public abstract class Staff {
    private final String username;
    private final String password;
    private final Role role;
    private final String workingHours;

    protected Staff(String username, String password, Role role, String workingHours) {
        this.username = requireNonBlank(username, "Staff username is required.");
        this.password = requireValidPassword(password);
        this.role = Objects.requireNonNull(role, "Role is required.");
        this.workingHours = requireNonBlank(workingHours, "Working hours are required.");
    }

    public final String getUsername() {
        return username;
    }

    public final String getPassword() {
        return password;
    }

    public final Role getRole() {
        return role;
    }

    public final String getWorkingHours() {
        return workingHours;
    }

    protected static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    protected static String requireValidPassword(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
        boolean hasLetter = false;
        boolean hasDigit = false;
        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) {
                hasLetter = true;
            } else if (Character.isDigit(c)) {
                hasDigit = true;
            }
        }
        if (!hasLetter || !hasDigit) {
            throw new IllegalArgumentException("Password must contain at least one letter and one digit.");
        }
        return password;
    }
}

