package model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EnumTest {

    // ── GuestState ────────────────────────────────────────────────────────────

    @Test
    void guestState_allValuesPresent() {
        GuestState[] states = GuestState.values();
        assertNotNull(states);
        assertTrue(states.length >= 8);
    }

    @Test
    void guestState_idleExists() {
        assertEquals(GuestState.IDLE, GuestState.valueOf("IDLE"));
    }

    @Test
    void guestState_walkingExists() {
        assertEquals(GuestState.WALKING, GuestState.valueOf("WALKING"));
    }

    @Test
    void guestState_inLiftExists() {
        assertEquals(GuestState.IN_LIFT, GuestState.valueOf("IN_LIFT"));
    }

    @Test
    void guestState_exitingLiftExists() {
        assertEquals(GuestState.EXITING_LIFT, GuestState.valueOf("EXITING_LIFT"));
    }

    @Test
    void guestState_atDestinationExists() {
        assertEquals(GuestState.AT_DESTINATION, GuestState.valueOf("AT_DESTINATION"));
    }

    // ── CleanerState ──────────────────────────────────────────────────────────

    @Test
    void cleanerState_idleExists() {
        assertEquals(CleanerState.IDLE, CleanerState.valueOf("IDLE"));
    }

    @Test
    void cleanerState_walkingToRoomExists() {
        assertEquals(CleanerState.WALKING_TO_ROOM, CleanerState.valueOf("WALKING_TO_ROOM"));
    }

    @Test
    void cleanerState_cleaningExists() {
        assertEquals(CleanerState.CLEANING, CleanerState.valueOf("CLEANING"));
    }

    @Test
    void cleanerState_walkingBackExists() {
        assertEquals(CleanerState.WALKING_BACK, CleanerState.valueOf("WALKING_BACK"));
    }

    // ── RoomType ──────────────────────────────────────────────────────────────

    @Test
    void roomType_roomExists() {
        assertEquals(RoomType.ROOM, RoomType.valueOf("ROOM"));
    }

    @Test
    void roomType_cinemaExists() {
        assertEquals(RoomType.CINEMA, RoomType.valueOf("CINEMA"));
    }

    @Test
    void roomType_fitnessExists() {
        assertEquals(RoomType.FITNESS, RoomType.valueOf("FITNESS"));
    }

    @Test
    void roomType_restaurantExists() {
        assertEquals(RoomType.RESTAURANT, RoomType.valueOf("RESTAURANT"));
    }

    @Test
    void roomType_lobbyExists() {
        assertEquals(RoomType.LOBBY, RoomType.valueOf("LOBBY"));
    }

    @Test
    void roomType_receptionExists() {
        assertEquals(RoomType.RECEPTION, RoomType.valueOf("RECEPTION"));
    }

    // ── PersonType ────────────────────────────────────────────────────────────

    @Test
    void personType_guestExists() {
        assertEquals(PersonType.GUEST, PersonType.valueOf("GUEST"));
    }
}
