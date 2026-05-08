package com.cse241.hotel.ui;

import com.cse241.hotel.model.user.Guest;
import com.cse241.hotel.model.user.Staff;


public final class Session {
    private static Guest currentGuest;
    private static Staff currentStaff;

    private Session() {
    }

    public static Guest getCurrentGuest() {
        return currentGuest;
    }

    public static void setCurrentGuest(Guest guest) {
        currentGuest = guest;
        if (guest != null) {
            currentStaff = null;
        }
    }

    public static Staff getCurrentStaff() {
        return currentStaff;
    }

    public static void setCurrentStaff(Staff staff) {
        currentStaff = staff;
        if (staff != null) {
            currentGuest = null;
        }
    }

    public static boolean isLoggedIn() {
        return currentGuest != null || currentStaff != null;
    }

    public static void clear() {
        currentGuest = null;
        currentStaff = null;
    }
}
