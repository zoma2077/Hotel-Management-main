package com.cse241.hotel.model.property;

import java.math.BigDecimal;
import java.util.Objects;

public final class RoomType {
    private final String name;
    private final BigDecimal basePricePerNight;
    private final int capacity;

    public RoomType(String name, BigDecimal basePricePerNight, int capacity) {
        this.name = requireNonBlank(name, "Room type name is required.");
        this.basePricePerNight = requireNonNegative(basePricePerNight, "Base price per night must be >= 0.");
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be > 0.");
        }
        this.capacity = capacity;
    }

    public String getName() {
        return name;
    }

    public BigDecimal getBasePricePerNight() {
        return basePricePerNight;
    }

    public int getCapacity() {
        return capacity;
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

