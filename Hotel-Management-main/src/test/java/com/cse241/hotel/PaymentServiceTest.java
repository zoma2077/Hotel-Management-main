package com.cse241.hotel;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.PaymentMethod;
import com.cse241.hotel.exceptions.InvalidPaymentException;
import com.cse241.hotel.model.transaction.Invoice;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.services.PaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class PaymentServiceTest {

    @BeforeEach
    void setUp() {
        HotelDatabase.resetForTests();
    }

    @Test
    void onlinePaymentShouldThrowWhenBalanceInsufficient() {
        Reservation reservation = HotelDatabase.RESERVATIONS.get(0);
        Guest guest = reservation.getGuest();
        guest.debitBalance(guest.getBalance()); // set to zero

        Invoice invoice = new Invoice(reservation, new BigDecimal("10.00"));

        assertThrows(InvalidPaymentException.class, () -> PaymentService.processPayment(invoice, PaymentMethod.ONLINE));
    }

    @Test
    void payingAlreadyPaidInvoiceShouldThrow() {
        Reservation reservation = HotelDatabase.RESERVATIONS.get(0);
        Invoice invoice = new Invoice(reservation, new BigDecimal("10.00"));
        invoice.markPaid(PaymentMethod.CASH, LocalDateTime.now());

        assertThrows(InvalidPaymentException.class, () -> PaymentService.processPayment(invoice, PaymentMethod.CASH));
    }
}

