package com.cse241.hotel.services;

import com.cse241.hotel.db.HotelDatabase;
import com.cse241.hotel.model.property.Room;

import java.util.List;
import java.util.Objects;

public final class RoomService {
    private RoomService() {
    }

    public static List<Room> searchRooms(String query) {
        HotelDatabase.seedDummyData();
        String q = query == null ? "" : query.trim().toLowerCase();
        if (q.isEmpty()) {
            return List.copyOf(HotelDatabase.ROOMS);
        }
        return HotelDatabase.ROOMS.stream()
                .filter(r -> r.getRoomNumber().toLowerCase().contains(q)
                        || r.getRoomType().getName().toLowerCase().contains(q))
                .toList();
    }

    public static Room addRoom(Room room) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(room, "Room is required.");
        HotelDatabase.requireUniqueRoomNumber(room.getRoomNumber());
        HotelDatabase.ROOMS.add(room);
        return room;
    }

    public static Room updateRoom(Room room) {
        HotelDatabase.seedDummyData();
        Objects.requireNonNull(room, "Room is required.");
        Room existing = HotelDatabase.findRoomByNumber(room.getRoomNumber())
                .orElseThrow(() -> new IllegalArgumentException("Room not found: " + room.getRoomNumber()));
        int idx = HotelDatabase.ROOMS.indexOf(existing);
        HotelDatabase.ROOMS.set(idx, room);
        return room;
    }

    public static boolean deleteRoomByNumber(String roomNumber) {
        HotelDatabase.seedDummyData();
        if (roomNumber == null || roomNumber.isBlank()) {
            throw new IllegalArgumentException("Room number is required.");
        }
        return HotelDatabase.ROOMS.removeIf(r -> r.getRoomNumber().equalsIgnoreCase(roomNumber));
    }
}

