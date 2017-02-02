package io.deltawave.Service.Input;

import java.awt.event.KeyEvent;

/**
 * Created by will on 6/3/16.
 */
public interface InputEventListener {

    public void onKeyDownEvent(KeyEvent event);

    public void onKeyUpEvent(KeyEvent event);
}
