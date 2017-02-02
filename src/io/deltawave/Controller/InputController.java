package io.deltawave.Controller;

import io.deltawave.Service.Input.SwingInputService;
import io.deltawave.Service.Render.RenderService;

import java.awt.*;
import java.awt.event.KeyEvent;

/**
 * Created by will on 6/3/16.
 */
public class InputController {

    private SwingInputService swingInputService;
    private RenderService renderService;

    public InputController(SwingInputService swingInputService, RenderService renderService) {
        this.swingInputService = swingInputService;
        this.renderService = renderService;

        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(swingInputService);
        //renderService.getWindow().addKeyListener(swingInputService);
    }

}
