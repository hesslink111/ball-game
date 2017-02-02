package io.deltawave.Domain;

import com.badlogic.gdx.physics.box2d.Body;

import java.util.ArrayList;

/**
 * Created by will on 6/3/16.
 */
public class GameObject {

    private Body body;
    private float[] color;

    private ArrayList<GameObjectListener> gameObjectListeners;

    public GameObject(Body body, float[] color) {
        this.body = body;
        body.setUserData(this);
        this.color = color;
        gameObjectListeners = new ArrayList<>();
    }

    public Body getBody() {
        return body;
    }

    public void addGameObjectListener(GameObjectListener gol) {
        gameObjectListeners.add(gol);
    }

    public void removeGameObjectListener(GameObjectListener gol) {
        gameObjectListeners.remove(gol);
    }

    public float[] getColor() {
        return color;
    }

    public void onBeginContact(GameObject contactedObject) {
        gameObjectListeners.stream().forEach(gol -> gol.onBeginGameObjectContact(this, contactedObject));
    }

    public void onEndContact(GameObject contactedObject) {
        gameObjectListeners.stream().forEach(gol -> gol.onEndGameObjectContact(this, contactedObject));
    }


}
