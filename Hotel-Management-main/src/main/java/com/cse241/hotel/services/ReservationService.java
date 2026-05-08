package com.cse241.hotel.services;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.exceptions.RoomNotAvailableException;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;

import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public final class ReservationService {
    private static final Set<ReservationStatus> BLOCKING_STATUSES =
            EnumSet.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED);

    private ReservationService() {
    }

    public static Reservation createReservation(Guest guest, Room room, LocalDate checkIn, LocalDate checkOut) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(guest, "Guest is required.");
        Objects.requireNonNull(room, "Room is required.");
        Objects.requireNonNull(checkIn, "Check-in date is required.");
        Objects.requireNonNull(checkOut, "Check-out date is required.");
        if (!checkIn.isBefore(checkOut)) {
            throw new IllegalArgumentException("Check-in date must be before check-out date.");
        }

        requireAvailable(room, checkIn, checkOut);

        Reservation reservation = new Reservation(guest, room, checkIn, checkOut, ReservationStatus.PENDING);
        HotelDatabase.RESERVATIONS.add(reservation);
        return reservation;
    }

    public static void requireAvailable(Room room, LocalDate requestedCheckIn, LocalDate requestedCheckOut) {
        boolean conflict = HotelDatabase.RESERVATIONS.stream()
                .filter(r -> r.getRoom().getRoomNumber().equalsIgnoreCase(room.getRoomNumber()))
                .filter(r -> BLOCKING_STATUSES.contains(r.getStatus()))
                .anyMatch(r -> rangesOverlap(requestedCheckIn, requestedCheckOut, r.getCheckInDate(), r.getCheckOutDate()));

        if (conflict) {
            throw new RoomNotAvailableException("Room " + room.getRoomNumber() + " is not available for the selected dates.");
        }
    }

    public static boolean rangesOverlap(
            LocalDate requestedCheckIn,
            LocalDate requestedCheckOut,
            LocalDate existingCheckIn,
            LocalDate existingCheckOut
    ) {
        return requestedCheckIn.isBefore(existingCheckOut) && requestedCheckOut.isAfter(existingCheckIn);
    }

    public static Reservation addReservation(Reservation reservation) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(reservation, "Reservation is required.");
        requireAvailable(reservation.getRoom(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        HotelDatabase.RESERVATIONS.add(reservation);
        return reservation;
    }

    public static Reservation updateReservation(Reservation reservation) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(reservation, "Reservation is required.");
        Reservation existing = HotelDatabase.findReservationById(reservation.getReservationId())
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservation.getReservationId()));
        int idx = HotelDatabase.RESERVATIONS.indexOf(existing);
        HotelDatabase.RESERVATIONS.set(idx, reservation);
        return reservation;
    }

    public static boolean cancelReservation(String reservationId) {
        HotelDatabase.seedDummyData();
        if (reservationId == null || reservationId.isBlank()) {
            throw new IllegalArgumentException("Reservation id is required.");
        }
        return HotelDatabase.findReservationById(reservationId)
                .map(r -> {
                    r.setStatus(ReservationStatus.CANCELLED);
                    return true;
                })
                .orElse(false);
    }

    public static Reservation setStatus(String reservationId, ReservationStatus status) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(status, "Status is required.");
        Reservation reservation = HotelDatabase.findReservationById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        reservation.setStatus(status);
        return reservation;
    }
}

