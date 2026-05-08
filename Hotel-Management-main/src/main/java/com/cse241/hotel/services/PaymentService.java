package com.cse241.hotel.services;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.enums.PaymentMethod;
import com.cse241.hotel.enums.ReservationStatus;
import com.cse241.hotel.exceptions.InvalidPaymentException;
import com.cse241.hotel.interfaces.Payable;
import com.cse241.hotel.model.transaction.Invoice;
import com.cse241.hotel.model.transaction.Reservation;
import com.cse241.hotel.model.user.Guest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

public final class PaymentService implements Payable {
    @Override
    public void pay(Invoice invoice, PaymentMethod method) throws InvalidPaymentException {
        processPayment(invoice, method);
    }

    public static Invoice checkout(Reservation reservation, PaymentMethod method) throws InvalidPaymentException {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(reservation, "Reservation is required.");
        Objects.requireNonNull(method, "Payment method is required.");

        BigDecimal total = calculateTotal(reservation);
        Invoice invoice = new Invoice(reservation, total);
        HotelDatabase.INVOICES.add(invoice);

        processPayment(invoice, method);

        reservation.setStatus(ReservationStatus.COMPLETED);
        return invoice;
    }

    public static void processPayment(Invoice invoice, PaymentMethod method) throws InvalidPaymentException {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(invoice, "Invoice is required.");
        if (invoice.isPaid()) {
            throw new InvalidPaymentException("Invoice is already paid: " + invoice.getInvoiceId());
        }
        Objects.requireNonNull(method, "Payment method is required.");

        Reservation reservation = invoice.getReservation();
        Guest guest = reservation.getGuest();

        switch (method) {
            case CASH, CREDIT_CARD -> invoice.markPaid(method, LocalDateTime.now());
            case ONLINE -> {
                BigDecimal amount = invoice.getTotalAmount();
                if (guest.getBalance().compareTo(amount) < 0) {
                    throw new InvalidPaymentException("Insufficient guest balance for ONLINE payment.");
                }
                guest.debitBalance(amount);
                invoice.markPaid(method, LocalDateTime.now());
            }
            default -> throw new InvalidPaymentException("Unsupported payment method: " + method);
        }
    }

    public static BigDecimal calculateTotal(Reservation reservation) {
        Objects.requireNonNull(reservation, "Reservation is required.");
        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        if (nights <= 0) {
            return BigDecimal.ZERO;
        }
        return reservation.getRoom().nightlyRate().multiply(BigDecimal.valueOf(nights));
    }
}

