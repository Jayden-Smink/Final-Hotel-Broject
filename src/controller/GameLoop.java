package controller;

import javax.swing.*;

public class GameLoop {

    private final SimulationController controller;
    private final HotelTimeEngine hte;
    private final Runnable onTick;   // tells the panel to repaint
    private final Timer timer;

    public GameLoop(SimulationController controller, HotelTimeEngine hte, Runnable onTick)
    {
        this.controller = controller;
        this.hte = hte;
        this.onTick = onTick;

        this.timer = new Timer(16, e -> tick());
    }

    public void start()
    {
        timer.start();
    }
    public void stop()
    {
        timer.stop();
    }

    private void tick()
    {
        if (!hte.isPaused())
        {
            for (int i = 0; i < hte.getSpeed(); i++)
            {
                controller.updateTick();
            }
        }
        onTick.run();
    }
}