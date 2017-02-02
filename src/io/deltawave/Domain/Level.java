package io.deltawave.Domain;

import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import finnstr.libgdx.liquidfun.ParticleGroup;
import finnstr.libgdx.liquidfun.ParticleSystem;

import java.util.ArrayList;

/**
 * Created by will on 6/3/16.
 */
public class Level {

    //Level
    private World world;
    private ParticleSystem particleSystem;
    private GameObject ball;
    private GameObject finish;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<RopeJoint> ropeJoints;
    private ArrayList<ParticleGroup> particleGroups;

    public Level(World world,
                 ParticleSystem particleSystem,
                 GameObject ball,
                 GameObject finish,
                 ArrayList<GameObject> gameObjects,
                 ArrayList<RopeJoint> ropeJoints,
                 ArrayList<ParticleGroup> particleGroups) {
        this.world = world;
        this.particleSystem = particleSystem;
        this.ball = ball;
        this.finish = finish;
        this.gameObjects = gameObjects;
        this.ropeJoints = ropeJoints;
        this.particleGroups = particleGroups;
    }

    public World getWorld() {
        return world;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }

    public ArrayList<GameObject> getGameObjects() {
        return gameObjects;
    }

    public ArrayList<RopeJoint> getRopeJoints() {
        return ropeJoints;
    }

    public ArrayList<ParticleGroup> getParticleGroups() {
        return particleGroups;
    }

    public GameObject getBall() {
        return ball;
    }

    public GameObject getFinish() {
        return finish;
    }
}
