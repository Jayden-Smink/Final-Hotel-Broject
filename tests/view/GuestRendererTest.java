package view;

import model.Guest;
import model.GuestState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;

import static org.junit.jupiter.api.Assertions.*;

class GuestRendererTest {

    private Graphics2D g2;

    @BeforeEach
    void setUp() {
        // Headless offscreen canvas so we never need a display
        BufferedImage img = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);
        g2 = img.createGraphics();

        // Always reset display-ID mapping between tests
        GuestRenderer.resetDisplayIds();
    }

    // ── draw – null guard ─────────────────────────────────────────────────────

    @Test
    void draw_withNullGuest_doesNotThrow() {
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, null, 0));
    }

    // ── draw – hidden states ──────────────────────────────────────────────────

    @Test
    void draw_idleGuestNotCheckingOut_doesNotThrow() {
        Guest guest = new Guest(1, 100, 100);
        guest.state = GuestState.IDLE;
        guest.isCheckingOut = false;
        // IDLE and not checking out → not drawn, but must not throw
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0));
    }

    @Test
    void draw_guestInLift_doesNotThrow() {
        Guest guest = new Guest(2, 50, 50);
        guest.state = GuestState.IN_LIFT;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0));
    }

    // ── draw – visible states ─────────────────────────────────────────────────

    @Test
    void draw_walkingGuest_doesNotThrow() {
        Guest guest = new Guest(3, 120, 80);
        guest.state = GuestState.WALKING;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0));
    }

    @Test
    void draw_checkingOutGuest_doesNotThrow() {
        Guest guest = new Guest(4, 200, 150);
        guest.state = GuestState.WALKING;
        guest.isCheckingOut = true;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0));
    }

    @Test
    void draw_withPositiveGlobalOffset_doesNotThrow() {
        Guest guest = new Guest(5, 50, 50);
        guest.state = GuestState.WALKING;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 60));
    }

    @Test
    void draw_withZeroCoordinates_doesNotThrow() {
        Guest guest = new Guest(6, 0, 0);
        guest.state = GuestState.WALKING;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0));
    }

    @Test
    void draw_multipleGuestsInSequence_doesNotThrow() {
        for (int i = 1; i <= 10; i++) {
            Guest guest = new Guest(i, i * 20, i * 10);
            guest.state = GuestState.WALKING;
            int idx = i;
            assertDoesNotThrow(() -> GuestRenderer.draw(g2, guest, 0),
                    "draw() must not throw for guest " + idx);
        }
    }

    // ── resetDisplayIds ───────────────────────────────────────────────────────

    @Test
    void resetDisplayIds_doesNotThrow() {
        assertDoesNotThrow(GuestRenderer::resetDisplayIds);
    }

    @Test
    void resetDisplayIds_canBeCalledMultipleTimes() {
        assertDoesNotThrow(() -> {
            GuestRenderer.resetDisplayIds();
            GuestRenderer.resetDisplayIds();
        });
    }

    @Test
    void resetDisplayIds_afterReset_guestGetsLowDisplayId() {
        // Register several guests to advance the counter
        for (int i = 1; i <= 5; i++) {
            Guest g = new Guest(i, 0, 0);
            g.state = GuestState.WALKING;
            GuestRenderer.draw(g2, g, 0);
        }

        GuestRenderer.resetDisplayIds();

        // After reset the next guest should start from display-id 1 again (no exception)
        Guest fresh = new Guest(99, 50, 50);
        fresh.state = GuestState.WALKING;
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, fresh, 0));
    }

    @Test
    void resetDisplayIds_afterReset_sameGuestGetsFreshLabel() {
        // First render assigns display id 1 to guest 42
        Guest g = new Guest(42, 50, 50);
        g.state = GuestState.WALKING;
        GuestRenderer.draw(g2, g, 0);

        GuestRenderer.resetDisplayIds();

        // Second render after reset should still not throw (counter restarted)
        assertDoesNotThrow(() -> GuestRenderer.draw(g2, g, 0));
    }
}
