package factory;

import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonFactoryTest {

    @Test
    void createPerson_guest_returnsGuestInstance() {
        assertInstanceOf(Guest.class, PersonFactory.createPerson(PersonType.GUEST, 1, 0, 0));
    }

    @Test
    void createPerson_guest_setsCorrectId() {
        Person p = PersonFactory.createPerson(PersonType.GUEST, 42, 0, 0);
        assertEquals(42, p.id);
    }

    @Test
    void createPerson_guest_setsInitialX() {
        Person p = PersonFactory.createPerson(PersonType.GUEST, 1, 100, 200);
        assertEquals(100.0, p.x, 0.001);
    }

    @Test
    void createPerson_guest_setsInitialY() {
        Person p = PersonFactory.createPerson(PersonType.GUEST, 1, 100, 200);
        assertEquals(200.0, p.y, 0.001);
    }

    @Test
    void createPerson_guest_initialStateIsWalking() {
        Guest g = (Guest) PersonFactory.createPerson(PersonType.GUEST, 1, 0, 0);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void createPerson_cleaner_returnsCleanerInstance() {
        assertInstanceOf(Cleaner.class, PersonFactory.createPerson(PersonType.CLEANER, 1, 0, 0));
    }

    @Test
    void createPerson_cleaner_initialStateIsIdle() {
        Cleaner c = (Cleaner) PersonFactory.createPerson(PersonType.CLEANER, 1, 0, 0);
        assertEquals(CleanerState.IDLE, c.state);
    }

    @Test
    void createPerson_multipleCallsReturnDistinctInstances() {
        Person p1 = PersonFactory.createPerson(PersonType.GUEST, 1, 0, 0);
        Person p2 = PersonFactory.createPerson(PersonType.GUEST, 2, 0, 0);
        assertNotSame(p1, p2);
    }

    @Test
    void createPerson_nullType_throwsException() {
        assertThrows(RuntimeException.class,
                () -> PersonFactory.createPerson(null, 1, 0, 0));
    }
}

class RoomFactoryTest {

    @Test
    void createRuimte_room_setsAreaTypeRoom() {
        Area a = RoomFactory.createRuimte(RoomType.ROOM, "1,2", "2,1", 1);
        assertEquals("ROOM", a.AreaType);
    }

    @Test
    void createRuimte_cinema_setsAreaTypeCinema() {
        assertEquals("CINEMA", RoomFactory.createRuimte(RoomType.CINEMA, "1,2", "2,1", 2).AreaType);
    }

    @Test
    void createRuimte_fitness_setsAreaTypeFitness() {
        assertEquals("FITNESS", RoomFactory.createRuimte(RoomType.FITNESS, "1,2", "2,1", 3).AreaType);
    }

    @Test
    void createRuimte_restaurant_setsAreaTypeRestaurant() {
        assertEquals("RESTAURANT", RoomFactory.createRuimte(RoomType.RESTAURANT, "1,2", "2,1", 4).AreaType);
    }

    @Test
    void createRuimte_liftschacht_setsAreaTypeLiftschacht() {
        assertEquals("LIFTSCHACHT", RoomFactory.createRuimte(RoomType.LIFTSCHACHT, "1,2", "2,1", 5).AreaType);
    }

    @Test
    void createRuimte_trap_setsAreaTypeTrap() {
        assertEquals("TRAP", RoomFactory.createRuimte(RoomType.TRAP, "1,2", "2,1", 6).AreaType);
    }

    @Test
    void createRuimte_lobby_setsAreaTypeLobby() {
        assertEquals("LOBBY", RoomFactory.createRuimte(RoomType.LOBBY, "1,2", "2,1", 7).AreaType);
    }

    @Test
    void createRuimte_reception_setsAreaTypeReception() {
        assertEquals("RECEPTION", RoomFactory.createRuimte(RoomType.RECEPTION, "1,2", "2,1", 8).AreaType);
    }

    @Test
    void createRuimte_setsPositionCorrectly() {
        Area a = RoomFactory.createRuimte(RoomType.ROOM, "3,4", "2,1", 1);
        assertArrayEquals(new int[]{3, 4}, a.getPos());
    }

    @Test
    void createRuimte_setsDimensionCorrectly() {
        Area a = RoomFactory.createRuimte(RoomType.ROOM, "1,1", "5,2", 1);
        assertArrayEquals(new int[]{5, 2}, a.getDim());
    }

    @Test
    void createRuimte_setsId() {
        Area a = RoomFactory.createRuimte(RoomType.ROOM, "1,1", "2,1", 99);
        assertEquals(99, a.id);
    }

    @Test
    void createRuimte_returnsNewInstanceEachCall() {
        Area a1 = RoomFactory.createRuimte(RoomType.ROOM, "1,1", "2,1", 1);
        Area a2 = RoomFactory.createRuimte(RoomType.ROOM, "1,1", "2,1", 1);
        assertNotSame(a1, a2);
    }
}