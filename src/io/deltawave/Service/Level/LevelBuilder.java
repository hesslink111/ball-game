package io.deltawave.Service.Level;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import finnstr.libgdx.liquidfun.*;
import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.Level;
import io.deltawave.Util.ColorConverter;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by will on 6/6/16.
 */
public class LevelBuilder {

    private World world;
    private ParticleSystem particleSystem;
    private GameObject ball;
    private GameObject finish;

    private ArrayList<ParticleGroup> particleGroups;
    private ArrayList<GameObject> gameObjects;
    private ArrayList<RopeJoint> ropeJoints;

    public LevelBuilder() {
        world = new World(new Vector2(0, -10), false);
        world.getAddress();
        ParticleSystemDef systemDef = new ParticleSystemDef();
        systemDef.radius = 0.05f;
        systemDef.dampingStrength = 0.2f;
        this.particleSystem = new ParticleSystem(world, systemDef);
        this.particleSystem.setParticleDensity(1.3f);

        gameObjects = new ArrayList<>();
        ropeJoints = new ArrayList<>();
        particleGroups = new ArrayList<>();
    }

    public ParticleGroup addParticles(float width, float height, float angle, float x, float y, String color) {
        ParticleGroupDef particleGroupDef = new ParticleGroupDef();
        float[] colorFloat = ColorConverter.StringToFloat(color);
        particleGroupDef.color.set(colorFloat[0], colorFloat[1], colorFloat[2], 0);
        particleGroupDef.flags.add(ParticleDef.ParticleType.b2_waterParticle);
        particleGroupDef.position.set(x, y);
        //No angles

        PolygonShape parShape = new PolygonShape();
        parShape.setAsBox(width, height);

        particleGroupDef.shape = parShape;

        ParticleGroup pg = particleSystem.createParticleGroup(particleGroupDef);
        particleGroups.add(pg);
        return pg;
    }

    private Body addBody(float x, float y, boolean isStatic) {
        BodyDef bd = new BodyDef();
        if(!isStatic) {
            bd.type = BodyDef.BodyType.DynamicBody;
        } else {
            bd.type = BodyDef.BodyType.StaticBody;
        }
        bd.position.set(x, y);


        Body body = world.createBody(bd);
        return body;
    }

    public GameObject addRectangle(float width, float height, float angle, float x, float y, boolean isStatic, String color) {
        Body body = addBody(x, y, isStatic);

        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(width, height);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        body.createFixture(fixtureDef);

        //Set rotation
        applyRotation(body, width, height, angle);

        //Why do I have to do this?
        polygonShape.dispose();

        //Turn it into a game object
        GameObject gameObject = new GameObject(body, ColorConverter.StringToFloat(color));
        gameObjects.add(gameObject);

        return gameObject;
    }

    public GameObject addCircle(float radius, float angle, float x, float y, boolean isStatic, String color) {
        Body body = addBody(x, y, isStatic);

        CircleShape circleShape = new CircleShape();
        circleShape.setRadius(radius);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = circleShape;
        fixtureDef.density = 0.5f;
        fixtureDef.friction = 0.4f;
        fixtureDef.restitution = 0.6f;

        body.createFixture(fixtureDef);

        circleShape.dispose();

        GameObject gameObject = new GameObject(body, ColorConverter.StringToFloat(color));
        gameObjects.add(gameObject);

        return gameObject;
    }

    public Body findBodyAt(float x, float y) {

        Body b = gameObjects.stream()
                .map(go -> go.getBody().getFixtureList())
                .flatMap(fl -> Arrays.asList(fl.items).stream())
                .filter(f -> f != null)
                .filter(f -> f.testPoint(x, y))
                .findAny()
                .get()
                .getBody();
        return b;
    }

    public void addRope(float p1x, float p1y, float p2x, float p2y) {
        RopeJointDef ropeJointDef = new RopeJointDef();
        ropeJointDef.bodyA = findBodyAt(p1x, p1y);
        ropeJointDef.bodyB = findBodyAt(p2x, p2y);
        ropeJointDef.localAnchorA.set(ropeJointDef.bodyA.getLocalPoint(new Vector2(p1x, p1y)));
        ropeJointDef.localAnchorB.set(ropeJointDef.bodyB.getLocalPoint(new Vector2(p2x, p2y)));
        ropeJointDef.maxLength = (float)Math.sqrt((p1x-p2x)*(p1x-p2x) + (p1y-p2y)*(p1y-p2y));
        ropeJointDef.collideConnected = true;

        RopeJoint ropeJoint = (RopeJoint)world.createJoint(ropeJointDef);
        ropeJoints.add(ropeJoint);
    }

    public void applyRotation(Body body, float width, float height, float angle) {
        //Apply rotation
        body.setTransform(body.getPosition(), -(float)(angle * Math.PI / 180));

        Vector2 cornerOffset = new Vector2(width, -height);
        double offsetAngle = Math.atan2(cornerOffset.y, cornerOffset.x);
        double length = Math.sqrt(cornerOffset.x*cornerOffset.x + cornerOffset.y*cornerOffset.y);

        double newAngle = -(angle * Math.PI / 180) + offsetAngle;
        Vector2 newCornerOffset = new Vector2((float)(length*Math.cos(newAngle)), (float)(length*Math.sin(newAngle)));

        //Move to the place the map editor thinks is correct
        body.setTransform(body.getPosition().add(newCornerOffset.x - cornerOffset.x, newCornerOffset.y - cornerOffset.y), body.getAngle());
    }

    public void setBall(GameObject ball) {
        this.ball = ball;
    }

    public Level buildLevel() {
        //this stuff had better not be null
        if(world == null) {
            System.err.println("Could not build level - world is null");
            System.exit(1);
        }

        if(particleSystem == null) {
            System.err.println("Could not build level - particle system is null");
            System.exit(1);
        }

        if(gameObjects == null) {
            System.err.println("Could not build level - gameobjects is null");
            System.exit(1);
        }

        if(ropeJoints == null) {
            System.err.println("Could not build level - ropejoints is null");
            System.exit(1);
        }

        if(particleGroups == null) {
            System.err.println("Could not build level - particlegroups is null");
            System.exit(1);
        }

        if(ball == null) {
            System.err.println("Could not build level - ball is null");
            System.exit(1);
        }

        if(finish == null) {
            System.err.println("Could not build level - finish is null");
            System.exit(1);
        }

        return new Level(world, particleSystem, ball, finish, gameObjects, ropeJoints, particleGroups);
    }


    public void setFinish(GameObject finish) {
        this.finish = finish;
    }

}
