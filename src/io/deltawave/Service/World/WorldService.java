package io.deltawave.Service.World;

import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import finnstr.libgdx.liquidfun.ParticleBodyContact;
import finnstr.libgdx.liquidfun.ParticleContact;
import finnstr.libgdx.liquidfun.ParticleSystem;
import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.Level;
import io.deltawave.Service.Render.RenderFrameListener;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by will on 6/3/16.
 */
public class WorldService implements RenderFrameListener, LevelSetListener, ContactListener {

    //Static variables
    private static final float NANO_TO_BASE = 1.0e9f;       //Conversion from nano to base

    //World
    private Level level;
    private World world;

    //Time variables
    private long last;

    //Listeners
    private ArrayList<WorldUpdateListener> worldUpdateListeners;
    private ArrayList<ContactListener> contactListeners;

    public WorldService(Level level) {
        this.level = level;
        this.world = level.getWorld();

        //Set up listeners
        worldUpdateListeners = new ArrayList<>();
        contactListeners = new ArrayList<>();
        world.setContactListener(this);

    }

    public void update() {
        // get the current time
        long time = System.nanoTime();
        // get the elapsed time from the last iteration
        long diff = time - last;
        // set the last time
        last = time;
        // convert from nanoseconds to seconds
        float elapsedTime = diff / NANO_TO_BASE;

        //Inform frame listeners
        notifyWorldUpdateListeners(elapsedTime);

        // update the world with the elapsed time
        //world.step(elapsedTime, 6, 2);
        world.step(elapsedTime, 1, 1, 3);//level.getParticleSystem().calculateReasonableParticleIterations(elapsedTime));
        //world.step(elapsedTime, 100, 50);

        //Perform join operations
        Iterator<RopeJoint> rjIterator = level.getRopeJoints().iterator();
        while(rjIterator.hasNext()) {
            RopeJoint rj = rjIterator.next();
            float force = rj.getReactionForce(elapsedTime).len2();
            if(force >= 0.03f) {
                world.destroyJoint(rj);
                rjIterator.remove();
            }
        }

    }

    public void addContactListener(ContactListener contactListener) {
        contactListeners.add(contactListener);
    }

    public void removeContactListener(ContactListener contactListener) {
        contactListeners.remove(contactListener);
    }

    public void addWorldUpdateListener(WorldUpdateListener worldUpdateListener) {
        worldUpdateListeners.add(worldUpdateListener);
    }

    public void removeWorldUpdateListener(WorldUpdateListener worldUpdateListener) {
        worldUpdateListeners.remove(worldUpdateListener);
    }

    private void notifyWorldUpdateListeners(float deltaTime) {
        worldUpdateListeners.stream().forEach(rl -> rl.onWorldUpdate(deltaTime));
    }

    @Override
    public void onRenderStart() {
        // initialize the last update time
        last = System.nanoTime();
    }

    @Override
    public void onRenderFrame() {
        update();
    }

    @Override
    public void onLevelSet(Level level) {
        this.level = level;
        world.dispose();
        this.world = level.getWorld();
        world.setContactListener(this);
    }

    @Override
    public void beginContact(Contact contact) {
        //contactListeners.forEach(cl -> cl.beginContact(contact));
        GameObject go1 = (GameObject)contact.getFixtureA().getBody().getUserData();
        GameObject go2 = (GameObject)contact.getFixtureB().getBody().getUserData();
        go1.onBeginContact(go2);
        go2.onBeginContact(go1);
    }

    @Override
    public void endContact(Contact contact) {
        //contactListeners.forEach(cl -> cl.endContact(contact));
        GameObject go1 = (GameObject)contact.getFixtureA().getBody().getUserData();
        GameObject go2 = (GameObject)contact.getFixtureB().getBody().getUserData();
        go1.onEndContact(go2);
        go2.onEndContact(go1);
    }

    @Override
    public void beginParticleBodyContact(ParticleSystem system, ParticleBodyContact contact) {

    }

    @Override
    public void endParticleBodyContact(Fixture fixture, ParticleSystem system, int index) {

    }

    @Override
    public void beginParticleContact(ParticleSystem system, ParticleContact contact) {

    }

    @Override
    public void endParticleContact(ParticleSystem system, int indexA, int indexB) {

    }

    @Override
    public void preSolve(Contact contact, Manifold manifold) {

    }

    @Override
    public void postSolve(Contact contact, ContactImpulse contactImpulse) {

    }
}
