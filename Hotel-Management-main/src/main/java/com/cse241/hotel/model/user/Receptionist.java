package com.cse241.hotel.model.user;

import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.enums.Role;
import com.cse241.hotel.interfaces.Manageable;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.services.ReservationService;

import java.util.Objects;

public final class Receptionist extends Staff implements Manageable<Reservation> {
    public Receptionist(String username, String password, String workingHours) {
        super(username, password, Role.RECEPTIONIST, workingHours);
    }

    @Override
    public Reservation create(Reservation item) {
        Objects.requireNonNull(item, "Reservation is required.");
        return ReservationService.addReservation(item);
    }

    @Override
    public Reservation update(Reservation item) {
        Objects.requireNonNull(item, "Reservation is required.");
        return ReservationService.updateReservation(item);
    }

    @Override
    public boolean deleteById(String id) {
        return ReservationService.cancelReservation(id);
    }

    public Reservation confirmReservation(String reservationId) {
        return ReservationService.setStatus(reservationId, ReservationStatus.CONFIRMED);
    }
}

