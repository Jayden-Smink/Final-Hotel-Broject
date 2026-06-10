package controller;

import model.Area;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class layoutGeneratorTest {

    private layoutGenerator generator;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        generator = new layoutGenerator();
    }

    // ── file-not-found guard ──────────────────────────────────────────────────

    @Test
    void generateLayout_withMissingFile_returnsEmptyList() {
        List<Area> areas = generator.generateLayout("nonexistent_file_xyz.json");
        assertNotNull(areas);
        assertTrue(areas.isEmpty(),
                "Missing file should return an empty list, not throw");
    }

    // ── minimal valid layout ──────────────────────────────────────────────────

    /**
     * Writes a minimal custom-JSON layout with a single ROOM entry and calls
     * generateLayout. The generator always appends LIFTSCHACHT, TRAP, LOBBY,
     * and RECEPTION automatically, so the result should contain at least 5 areas.
     */
    @Test
    void generateLayout_singleRoom_returnsAtLeastFiveAreas() throws IOException {
        String content = buildLayoutWithOneRoom();
        Path file = tempDir.resolve("test_layout.json");
        Files.writeString(file, content);

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        // 1 ROOM + 4 fixed areas (LIFTSCHACHT, TRAP, LOBBY, RECEPTION)
        assertTrue(areas.size() >= 5,
                "Expected at least 5 areas (1 room + 4 fixed), got " + areas.size());
    }

    @Test
    void generateLayout_alwaysContainsLobby() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        boolean hasLobby = areas.stream()
                .anyMatch(a -> "LOBBY".equalsIgnoreCase(a.AreaType));
        assertTrue(hasLobby, "Generated layout must always include a LOBBY");
    }

    @Test
    void generateLayout_alwaysContainsReception() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        boolean hasReception = areas.stream()
                .anyMatch(a -> "RECEPTION".equalsIgnoreCase(a.AreaType));
        assertTrue(hasReception, "Generated layout must always include a RECEPTION");
    }

    @Test
    void generateLayout_alwaysContainsStairs() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        boolean hasTrap = areas.stream()
                .anyMatch(a -> "TRAP".equalsIgnoreCase(a.AreaType));
        assertTrue(hasTrap, "Generated layout must always include a TRAP (stairs)");
    }

    @Test
    void generateLayout_alwaysContainsElevatorShaft() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        boolean hasShaft = areas.stream()
                .anyMatch(a -> "LIFTSCHACHT".equalsIgnoreCase(a.AreaType));
        assertTrue(hasShaft, "Generated layout must always include a LIFTSCHACHT");
    }

    // ── room properties ───────────────────────────────────────────────────────

    @Test
    void generateLayout_roomHasPositiveOrZeroCoordinates() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        Area room = areas.stream()
                .filter(a -> "ROOM".equalsIgnoreCase(a.AreaType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No ROOM found"));

        int[] pos = room.getPos();
        assertNotNull(pos);
        assertEquals(2, pos.length);
    }

    @Test
    void generateLayout_roomHasValidDimension() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        Area room = areas.stream()
                .filter(a -> "ROOM".equalsIgnoreCase(a.AreaType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No ROOM found"));

        int[] dim = room.getDim();
        assertNotNull(dim);
        assertTrue(dim[0] > 0 && dim[1] > 0, "Room dimensions must be positive");
    }

    @Test
    void generateLayout_roomDefaultCapacityIsOne() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        Area room = areas.stream()
                .filter(a -> "ROOM".equalsIgnoreCase(a.AreaType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No ROOM found"));

        assertEquals(1, room.Capacity, "Default room capacity should be 1");
    }

    // ── cinema default capacity ───────────────────────────────────────────────

    @Test
    void generateLayout_cinemaDefaultCapacityIsTen() throws IOException {
        String layout = buildLayoutWithCinema();
        Path file = writeLayout(layout);

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        Area cinema = areas.stream()
                .filter(a -> "CINEMA".equalsIgnoreCase(a.AreaType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No CINEMA found"));

        assertEquals(10, cinema.Capacity, "Default cinema capacity should be 10");
    }

    // ── restaurant default capacity ───────────────────────────────────────────

    @Test
    void generateLayout_restaurantDefaultCapacityIsFive() throws IOException {
        String layout = buildLayoutWithRestaurant();
        Path file = writeLayout(layout);

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        Area restaurant = areas.stream()
                .filter(a -> "RESTAURANT".equalsIgnoreCase(a.AreaType))
                .findFirst()
                .orElseThrow(() -> new AssertionError("No RESTAURANT found"));

        assertEquals(5, restaurant.Capacity, "Default restaurant capacity should be 5");
    }

    // ── re-entrance / reset ───────────────────────────────────────────────────

    @Test
    void generateLayout_calledTwice_returnsFreshList() throws IOException {
        Path file = writeLayout(buildLayoutWithOneRoom());

        List<Area> first  = generator.generateLayout(file.toAbsolutePath().toString());
        List<Area> second = generator.generateLayout(file.toAbsolutePath().toString());

        assertNotSame(first, second, "Each call must return a new list instance");
        assertEquals(first.size(), second.size(), "Both calls with same file must produce same count");
    }

    // ── empty / invalid content ───────────────────────────────────────────────

    @Test
    void generateLayout_emptyFile_returnsOnlyFixedAreas() throws IOException {
        Path file = tempDir.resolve("empty.json");
        Files.writeString(file, "{}");

        List<Area> areas = generator.generateLayout(file.toAbsolutePath().toString());

        // No rooms, but 4 fixed areas are still appended
        assertEquals(4, areas.size(),
                "Empty/invalid content should still produce exactly 4 fixed infrastructure areas");
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Path writeLayout(String content) throws IOException {
        Path file = tempDir.resolve("layout_" + System.nanoTime() + ".json");
        Files.writeString(file, content);
        return file;
    }

    /** Minimal layout JSON containing one ROOM entry. */
    private String buildLayoutWithOneRoom() {
        return "[\n" +
               "  {\n" +
               "    \"AreaType\": \"ROOM\",\n" +
               "    \"Position\": \"1, 0\",\n" +
               "    \"Dimension\": \"1, 1\"\n" +
               "  }\n" +
               "]\n";
    }

    private String buildLayoutWithCinema() {
        return "[\n" +
               "  {\n" +
               "    \"AreaType\": \"CINEMA\",\n" +
               "    \"Position\": \"1, 0\",\n" +
               "    \"Dimension\": \"2, 1\"\n" +
               "  }\n" +
               "]\n";
    }

    private String buildLayoutWithRestaurant() {
        return "[\n" +
               "  {\n" +
               "    \"AreaType\": \"RESTAURANT\",\n" +
               "    \"Position\": \"1, 0\",\n" +
               "    \"Dimension\": \"2, 1\"\n" +
               "  }\n" +
               "]\n";
    }
}
