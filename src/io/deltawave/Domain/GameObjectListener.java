package io.deltawave.Domain;

/**
 * Created by will on 6/10/16.
 */
public interface GameObjectListener {

    public void onBeginGameObjectContact(GameObject go1, GameObject go2);

    public void onEndGameObjectContact(GameObject go1, GameObject go2);
}
