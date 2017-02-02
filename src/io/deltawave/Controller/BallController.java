package io.deltawave.Controller;

import com.badlogic.gdx.physics.box2d.*;
import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.GameObjectListener;
import io.deltawave.Domain.Level;

import io.deltawave.Service.Input.SwingInputService;
import io.deltawave.Service.Level.LevelService;
import io.deltawave.Service.World.LevelSetListener;
import io.deltawave.Service.World.WorldService;
import io.deltawave.Service.World.WorldUpdateListener;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by will on 6/3/16.
 */
public class BallController implements WorldUpdateListener, LevelSetListener, GameObjectListener {

    //Services
    private WorldService worldService;
    private SwingInputService swingInputService;

    //World and bodies
    private Level level;
    private GameObject ball;

    //Contact
    private boolean ballInContact;
    private GameObject contactedObject;

    //Bounce
    private boolean applyingBounce;
    private Timer bounceTimer;

    public BallController(WorldService worldService, LevelService levelService, SwingInputService swingInputService) {

        //Services
        this.worldService = worldService;
        this.swingInputService = swingInputService;

        //World
        this.level = levelService.getLevel();
        this.ball = level.getBall();

        //Default state
        ballInContact = false;

        //Listening
        ball.addGameObjectListener(this);
        worldService.addWorldUpdateListener(this);
        levelService.addLevelSetListener(this);

        //Bounce
        applyingBounce = false;
        bounceTimer = new Timer();
    }

    @Override
    public void onWorldUpdate(float deltaTime) {

        if(ballInContact && (swingInputService.getKeyDown(SwingInputService.SPACE_KEY))) {
            applyBounce();
        }

        if(swingInputService.getKeyDown(SwingInputService.UP_KEY)) {
            ball.getBody().applyLinearImpulse(0f, 4f * deltaTime, ball.getBody().getPosition().x, ball.getBody().getPosition().y, true);
        }

        if(swingInputService.getKeyDown(SwingInputService.DOWN_KEY)) {
            ball.getBody().applyLinearImpulse(0f, -8f * deltaTime, ball.getBody().getPosition().x, ball.getBody().getPosition().y, true);
        }

        if(swingInputService.getKeyDown(SwingInputService.LEFT_KEY)) {
            ball.getBody().applyLinearImpulse(-25f * deltaTime, 0f, ball.getBody().getPosition().x, ball.getBody().getPosition().y, true);
        }

        if(swingInputService.getKeyDown(SwingInputService.RIGHT_KEY)) {
            ball.getBody().applyLinearImpulse(25f * deltaTime, 0f, ball.getBody().getPosition().x, ball.getBody().getPosition().y, true);
        }

    }

    @Override
    public void onLevelSet(Level level) {
        this.ball = level.getBall();
        ball.addGameObjectListener(this);
        this.level = level;
    }

    @Override
    public void onBeginGameObjectContact(GameObject go1, GameObject go2) {
        //Ball has made contact
        ballInContact = true;
        this.contactedObject = go2;
    }

    @Override
    public void onEndGameObjectContact(GameObject go1, GameObject go2) {
        //Ball has left contact
        ballInContact = false;
        if(swingInputService.getKeyDown(SwingInputService.SPACE_KEY)) {
            applyBounce();
        }
    }

    private void applyBounce() {
        if(!applyingBounce) {
            if (ball.getBody().getLinearVelocity().y - contactedObject.getBody().getLinearVelocity().y >= 0) {
                applyingBounce = true;

                //This one is way more fun
                ball.getBody().applyLinearImpulse(0f, 25f, ball.getBody().getPosition().x, ball.getBody().getPosition().y, true);

                //Can only bounce every 100 ms;
                bounceTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        applyingBounce = false;
                    }
                }, 100);
            }
        }
    }
}
