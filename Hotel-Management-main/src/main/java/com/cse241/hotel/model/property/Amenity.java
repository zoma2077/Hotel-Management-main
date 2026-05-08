package com.cse241.hotel.model.property;

import java.math.BigDecimal;
import java.util.Objects;

public final class Amenity {
    private final String name;
    private final BigDecimal nightlyCost;

    public Amenity(String name, BigDecimal nightlyCost) {
        this.name = requireNonBlank(name, "Amenity name is required.");
        this.nightlyCost = requireNonNegative(nightlyCost, "Amenity nightly cost must be >= 0.");
    }

    public String getName() {
        return name;
    }

    public BigDecimal getNightlyCost() {
        return nightlyCost;
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

