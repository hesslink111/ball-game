package io.deltawave.Controller;

import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.GameObjectListener;
import io.deltawave.Domain.Level;
import io.deltawave.Service.Input.InputEventListener;
import io.deltawave.Service.Input.SwingInputService;
import io.deltawave.Service.Level.LevelService;
import io.deltawave.Service.Render.RenderService;
import io.deltawave.Service.World.LevelSetListener;
import io.deltawave.Service.World.WorldService;

import java.awt.event.KeyEvent;

/**
 * Created by will on 6/3/16.
 */
public class GameController implements InputEventListener, LevelSetListener, GameObjectListener {

    //Services
    private RenderService renderService;
    private WorldService worldService;
    private LevelService levelService;
    private SwingInputService swingInputService;

    //Keys
    private final int reloadKey = KeyEvent.VK_R;

    //Tracked objects
    private GameObject ball;
    private GameObject finish;

    //State
    private boolean won;

    public GameController(RenderService renderService, WorldService worldService, LevelService levelService, SwingInputService swingInputService) {
        this.renderService = renderService;
        this.worldService = worldService;
        this.levelService = levelService;
        this.swingInputService = swingInputService;

        //Set up listeners
        renderService.addRenderFrameListener(worldService);
        levelService.addLevelSetListener(renderService);
        levelService.addLevelSetListener(worldService);
        levelService.addLevelSetListener(this);
        swingInputService.addInputEventListener(this);

        //This is really how it should be done
        levelService.loadLevelFile("testmap2.json");

        //Set tracked objects
        ball = levelService.getLevel().getBall();
        finish = levelService.getLevel().getFinish();
        finish.addGameObjectListener(this);

        //Set default state
        won = false;

    }

    @Override
    public void onKeyDownEvent(KeyEvent event) {
        switch(event.getKeyCode()) {
            case reloadKey:
                levelService.loadLevelFile("testmap2.json");
                break;
        }
    }

    @Override
    public void onKeyUpEvent(KeyEvent event) {

    }

    @Override
    public void onLevelSet(Level level) {
        ball = level.getBall();
        finish = level.getFinish();
        finish.addGameObjectListener(this);
    }

    @Override
    public void onBeginGameObjectContact(GameObject go1, GameObject go2) {
        System.out.println("Winner!");
    }

    @Override
    public void onEndGameObjectContact(GameObject go1, GameObject go2) {

    }
}
