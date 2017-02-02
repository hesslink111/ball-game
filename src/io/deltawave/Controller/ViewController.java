package io.deltawave.Controller;

import io.deltawave.Service.Render.RenderService;
import io.deltawave.View.Window;

/**
 * Created by will on 6/2/16.
 */
public class ViewController {

    private RenderService renderService;

    public ViewController(RenderService renderService) {

        this.renderService = renderService;
        renderService.start();

    }
}
