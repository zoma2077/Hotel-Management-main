package com.cse241.hotel;

import com.cse241.hotel.model.user.Guest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PasswordValidationTest {

    @Test
    void passwordMustBeAtLeast8Chars() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> Guest.requireValidPassword("Abc123"));
        assertTrue(ex.getMessage().toLowerCase().contains("at least 8"));
    }

    @Test
    void passwordMustContainLetterAndDigit() {
        assertThrows(IllegalArgumentException.class, () -> Guest.requireValidPassword("12345678"));
        assertThrows(IllegalArgumentException.class, () -> Guest.requireValidPassword("Password"));
        assertDoesNotThrow(() -> Guest.requireValidPassword("Password1"));
    }
}

