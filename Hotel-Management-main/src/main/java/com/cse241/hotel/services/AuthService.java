package com.cse241.hotel.services;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.Gender;
import com.cse241.hotel.enums.Role;
import com.cse241.hotel.model.user.Admin;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.model.user.Receptionist;
import com.cse241.hotel.model.user.Staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

public final class AuthService {
    private AuthService() {
    }

    public static Guest registerGuest(
            String username,
            String password,
            LocalDate dateOfBirth,
            BigDecimal balance,
            String address,
            Gender gender,
            List<String> roomPreferences
    ) {
        HotelDatabase.seedDummyData();

        Objects.requireNonNull(username, "Username is required.");
        HotelDatabase.requireUniqueGuestUsername(username);

        Guest guest = new Guest(username, password, dateOfBirth, balance, address, gender, roomPreferences);
        HotelDatabase.GUESTS.add(guest);
        return guest;
    }

    public static Guest loginGuest(String username, String password) {
        HotelDatabase.seedDummyData();
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }
        return HotelDatabase.findGuestByUsername(username)
                .filter(g -> g.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));
    }

    public static Staff loginStaff(Role role, String username, String password) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(role, "Role is required.");
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (password == null) {
            throw new IllegalArgumentException("Password is required.");
        }

        Staff staff = HotelDatabase.findStaffByUsername(username)
                .filter(s -> s.getPassword().equals(password))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password."));

        if (staff.getRole() != role) {
            throw new IllegalArgumentException("Account role mismatch.");
        }
        return staff;
    }

    public static Admin loginAdmin(String username, String password) {
        Staff staff = loginStaff(Role.ADMIN, username, password);
        if (staff instanceof Admin admin) {
            return admin;
        }
        throw new IllegalArgumentException("Account is not an admin.");
    }

    public static Receptionist loginReceptionist(String username, String password) {
        Staff staff = loginStaff(Role.RECEPTIONIST, username, password);
        if (staff instanceof Receptionist receptionist) {
            return receptionist;
        }
        throw new IllegalArgumentException("Account is not a receptionist.");
    }
}

