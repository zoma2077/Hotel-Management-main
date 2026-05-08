package com.cse241.hotel.model.transaction;

import com.cse241.hotel.enums.PaymentMethod;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public final class Invoice {
    private final String invoiceId;
    private final Reservation reservation;
    private final BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private LocalDateTime paymentDate;

    public Invoice(Reservation reservation, BigDecimal totalAmount) {
        this(UUID.randomUUID().toString(), reservation, totalAmount);
    }

    public Invoice(String invoiceId, Reservation reservation, BigDecimal totalAmount) {
        this.invoiceId = requireNonBlank(invoiceId, "Invoice id is required.");
        this.reservation = Objects.requireNonNull(reservation, "Reservation is required.");
        this.totalAmount = requireNonNegative(totalAmount, "Total amount must be >= 0.");
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public Reservation getReservation() {
        return reservation;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }

    public boolean isPaid() {
        return paymentDate != null;
    }

    public void markPaid(PaymentMethod paymentMethod, LocalDateTime paymentDate) {
        this.paymentMethod = Objects.requireNonNull(paymentMethod, "Payment method is required.");
        this.paymentDate = Objects.requireNonNull(paymentDate, "Payment date is required.");
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
}

