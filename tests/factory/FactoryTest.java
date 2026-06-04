package factory;

import model.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PersonFactoryTest {

    @Test
    void createGuest_returnsGuestInstance() {
        assertInstanceOf(Guest.class, PersonFactory.createGuest(PersonType.GUEST, 1, 0, 0));
    }

    @Test
    void createGuest_setsCorrectId() {
        Guest g = PersonFactory.createGuest(PersonType.GUEST, 42, 0, 0);
        assertEquals(42, g.id);
    }

    @Test
    void createGuest_setsInitialX() {
        Guest g = PersonFactory.createGuest(PersonType.GUEST, 1, 100, 200);
        assertEquals(100.0, g.x, 0.001);
    }

    @Test
    void createGuest_setsInitialY() {
        Guest g = PersonFactory.createGuest(PersonType.GUEST, 1, 100, 200);
        assertEquals(200.0, g.y, 0.001);
    }

    @Test
    void createGuest_initialStateIsWalking() {
        Guest g = PersonFactory.createGuest(PersonType.GUEST, 1, 0, 0);
        assertEquals(GuestState.WALKING, g.state);
    }

    @Test
    void createGuest_multipleCallsReturnDistinctInstances() {
        Guest g1 = PersonFactory.createGuest(PersonType.GUEST, 1, 0, 0);
        Guest g2 = PersonFactory.createGuest(PersonType.GUEST, 2, 0, 0);
        assertNotSame(g1, g2);
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
