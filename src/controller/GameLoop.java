package controller;

import javax.swing.*;

/**
 * De motor van de simulatie. Deze klasse zorgt voor een constante tik (tick)
 * waarmee de logica wordt geüpdatet en het scherm wordt ververst.
 */
public class GameLoop {

    private final SimulationController controller;
    private final HotelTimeEngine hte;
    private final Runnable onTick;   // Callback die het tekenpaneel vertelt om te hertekenen (repaint)
    private final Timer timer;

    public GameLoop(SimulationController controller, HotelTimeEngine hte, Runnable onTick)
    {
        this.controller = controller;
        this.hte = hte;
        this.onTick = onTick;

        // Schiet elke 16 milliseconden een actie af (~60 frames per seconde) en roep de tick() methode aan
        this.timer = new Timer(16, e -> tick());
    }

    /**
     * Start de gameloop.
     */
    public void start()
    {
        timer.start();
    }

    /**
     * Zet de gameloop stil.
     */
    public void stop()
    {
        timer.stop();
    }

    /**
     * Wordt elke 16ms uitgevoerd. Updatet de logica en ververst de graphics.
     */
    private void tick()
    {
        // Voer de logica alleen uit als de simulatie niet op pauze staat
        if (!hte.isPaused())
        {
            // Draai de update-lus vaker per tick als de simulatiesnelheid (bijv. 2x of 4x) is verhoogd
            for (int i = 0; i < hte.getSpeed(); i++)
            {
                controller.updateTick();
            }
        }
        // Voer de repaint-actie uit, zodat het scherm up-to-date blijft met de nieuwe posities
        onTick.run();
    }
}