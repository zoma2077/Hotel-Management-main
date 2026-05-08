package com.cse241.hotel.model.user;

import com.cse241.hotel.enums.Role;
import com.cse241.hotel.interfaces.Manageable;
import com.cse241.hotel.model.property.Room;
import com.cse241.hotel.services.RoomService;

import java.util.Objects;

public final class Admin extends Staff implements Manageable<Room> {
    public Admin(String username, String password, String workingHours) {
        super(username, password, Role.ADMIN, workingHours);
    }

    @Override
    public Room create(Room item) {
        Objects.requireNonNull(item, "Room is required.");
        return RoomService.addRoom(item);
    }

    @Override
    public Room update(Room item) {
        Objects.requireNonNull(item, "Room is required.");
        return RoomService.updateRoom(item);
    }

    @Override
    public boolean deleteById(String id) {
        return RoomService.deleteRoomByNumber(id);
    }
}

