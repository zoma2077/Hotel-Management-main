package com.cse241.hotel.model.property;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Room {
    private final String roomNumber;
    private final RoomType roomType;
    private final List<Amenity> amenities;

    public Room(String roomNumber, RoomType roomType, List<Amenity> amenities) {
        this.roomNumber = requireNonBlank(roomNumber, "Room number is required.");
        this.roomType = Objects.requireNonNull(roomType, "Room type is required.");
        this.amenities = Collections.unmodifiableList(new ArrayList<>(amenities == null ? List.of() : amenities));
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public List<Amenity> getAmenities() {
        return amenities;
    }

    public BigDecimal nightlyRate() {
        BigDecimal total = roomType.getBasePricePerNight();
        for (Amenity a : amenities) {
            total = total.add(a.getNightlyCost());
        }
        return total;
    }

    private static String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value;
    }
}

