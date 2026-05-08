package com.cse241.hotel;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.exceptions.RoomNotAvailableException;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.services.ReservationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class ReservationOverlapTest {

    @BeforeEach
    void setUp() {
        HotelDatabase.resetForTests();
    }

    @Test
    void overlappingDatesShouldThrowRoomNotAvailable() {
        Reservation existing = HotelDatabase.RESERVATIONS.get(0);
        Guest guest = HotelDatabase.GUESTS.get(0);
        Room room = existing.getRoom();

        LocalDate requestedCheckIn = existing.getCheckInDate().plusDays(1);
        LocalDate requestedCheckOut = existing.getCheckOutDate().minusDays(1);

        assertThrows(RoomNotAvailableException.class, () ->
                ReservationService.createReservation(guest, room, requestedCheckIn, requestedCheckOut)
        );
    }

    @Test
    void backToBackDatesShouldBeAllowed() {
        Reservation existing = HotelDatabase.RESERVATIONS.get(0);
        Guest guest = HotelDatabase.GUESTS.get(0);
        Room room = existing.getRoom();

        LocalDate requestedCheckIn = existing.getCheckOutDate();
        LocalDate requestedCheckOut = existing.getCheckOutDate().plusDays(2);

        assertDoesNotThrow(() -> ReservationService.createReservation(guest, room, requestedCheckIn, requestedCheckOut));
    }
}

