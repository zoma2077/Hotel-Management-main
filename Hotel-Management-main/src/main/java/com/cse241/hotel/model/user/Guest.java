package com.cse241.hotel.model.user;

import com.cse241.hotel.enums.Gender;
import com.cse241.hotel.enums.PaymentMethod;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.transaction.Invoice;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.services.AuthService;
import com.cse241.hotel.services.PaymentService;
import com.cse241.hotel.services.ReservationService;
import com.cse241.hotel.services.RoomService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Guest {
    private static final int MIN_AGE_YEARS = 18;

    private final String username;
    private final String password;
    private final LocalDate dateOfBirth;
    private BigDecimal balance;
    private String address;
    private final Gender gender;
    private final List<String> roomPreferences;

    public Guest(
            String username,
            String password,
            LocalDate dateOfBirth,
            BigDecimal balance,
            String address,
            Gender gender,
            List<String> roomPreferences
    ) {
        this.username = requireNonBlank(username, "Username is required.");
        this.password = requireValidPassword(password);
        this.dateOfBirth = requireAdultDob(dateOfBirth);
        this.balance = requireNonNegative(balance, "Balance must be >= 0.");
        this.address = requireNonBlank(address, "Address is required.");
        this.gender = Objects.requireNonNull(gender, "Gender is required.");
        this.roomPreferences = Collections.unmodifiableList(new ArrayList<>(roomPreferences == null ? List.of() : roomPreferences));
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getAddress() {
        return address;
    }

    public Gender getGender() {
        return gender;
    }

    public List<String> getRoomPreferences() {
        return roomPreferences;
    }

    public void updateAddress(String newAddress) {
        this.address = requireNonBlank(newAddress, "Address is required.");
    }

    public void creditBalance(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount is required.");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Credit amount must be >= 0.");
        }
        this.balance = this.balance.add(amount);
    }

    public void debitBalance(BigDecimal amount) {
        Objects.requireNonNull(amount, "Amount is required.");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Debit amount must be >= 0.");
        }
        BigDecimal next = this.balance.subtract(amount);
        if (next.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Insufficient balance.");
        }
        this.balance = next;
    }

    // Convenience intent methods (Phase 2 controllers can call services directly too).
    public static Guest register(
            String username,
            String password,
            LocalDate dateOfBirth,
            BigDecimal balance,
            String address,
            Gender gender,
            List<String> roomPreferences
    ) {
        return AuthService.registerGuest(username, password, dateOfBirth, balance, address, gender, roomPreferences);
    }

    public static Guest login(String username, String password) {
        return AuthService.loginGuest(username, password);
    }

    public List<Room> browseRooms(String query) {
        return RoomService.searchRooms(query);
    }

    public Reservation makeReservation(Room room, LocalDate checkIn, LocalDate checkOut) {
        return ReservationService.createReservation(this, room, checkIn, checkOut);
    }

    public Invoice checkout(Reservation reservation, PaymentMethod paymentMethod) throws Exception {
        return PaymentService.checkout(reservation, paymentMethod);
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static BigDecimal requireNonNegative(BigDecimal value, String message) {
        Objects.requireNonNull(value, "Value is required.");
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }

    private static LocalDate requireAdultDob(LocalDate dob) {
        Objects.requireNonNull(dob, "Date of birth is required.");
        int years = Period.between(dob, LocalDate.now()).getYears();
        if (years < MIN_AGE_YEARS) {
            throw new IllegalArgumentException("Guest must be at least " + MIN_AGE_YEARS + " years old.");
        }
        return dob;
    }

    public static String requireValidPassword(String password) {
        return Staff.requireValidPassword(password);
    }
}

