package io.deltawave;

import io.deltawave.Controller.BallController;
import io.deltawave.Controller.GameController;
import io.deltawave.Controller.InputController;
import io.deltawave.Controller.ViewController;
import io.deltawave.Service.Input.SwingInputService;
import io.deltawave.Service.Level.LevelService;
import io.deltawave.Service.Render.RenderService;
import io.deltawave.Service.World.WorldService;


public class App {

    public App(int width, int height) {

        SwingInputService swingInputService = new SwingInputService();
        LevelService levelService = new LevelService();
        WorldService worldService = new WorldService(levelService.getLevel());
        RenderService renderService = new RenderService(width, height, levelService.getLevel());

        GameController gameController = new GameController(renderService, worldService, levelService, swingInputService);
        ViewController viewController = new ViewController(renderService);
        BallController ballController = new BallController(worldService, levelService, swingInputService);
        InputController inputController = new InputController(swingInputService, renderService);

    }

    public static void main(String[] args) {

        int width = 800;
        int height = 500;

        try {
            if (args.length == 4) {
                if (args[0].equals("-w") && args[2].equals("-h")) {
                    width = Integer.parseInt(args[1]);
                    height = Integer.parseInt(args[3]);
                }
            }
        } catch(Exception ex) {
            System.err.println("Ex: -w 800 -h 500");
        }

        App app = new App(width, height);

    }
}
