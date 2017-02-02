package io.deltawave.Service.Input;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

/**
 * Created by will on 6/6/16.
 */
public class SwingInputService implements KeyListener, KeyEventDispatcher {

    private ArrayList<InputEventListener> inputEventListeners;

    //keycodes
    public static final int SPACE_KEY = KeyEvent.VK_SPACE;
    public static final int LEFT_KEY = KeyEvent.VK_LEFT;
    public static final int RIGHT_KEY = KeyEvent.VK_RIGHT;
    public static final int UP_KEY = KeyEvent.VK_UP;
    public static final int DOWN_KEY = KeyEvent.VK_DOWN;

    //keypressed
    private boolean[] keyDown;

    public SwingInputService() {

        //Set up listeners
        inputEventListeners = new ArrayList<>();

        //keypressed
        setUpKeyDown();
    }

    private void setUpKeyDown() {
        /*
        int max = 0;
        Field[] fields = KeyEvent.class.getDeclaredFields();
        for (Field f : fields) {
            if (Modifier.isStatic(f.getModifiers())) {
                if(f.getName().startsWith("VK_")) {
                    int code = 0;
                    try {
                        code = f.getInt(f.getName());
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    if(code > max) {
                        max = code;
                    }
                }
            }
        }
        */
        int max = 65536;

        keyDown = new boolean[max];
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        if(!keyDown[e.getKeyCode()]) {
            keyDown[e.getKeyCode()] = true;
            notifyInputEventListenersKeyDown(e);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyDown[e.getKeyCode()] = false;
        notifyInputEventListenersKeyUp(e);
    }

    public void notifyInputEventListenersKeyDown(KeyEvent event) {
        inputEventListeners.parallelStream().forEach(iel -> iel.onKeyDownEvent(event));
    }

    public void notifyInputEventListenersKeyUp(KeyEvent event) {
        inputEventListeners.parallelStream().forEach(iel -> iel.onKeyUpEvent(event));
    }

    public void addInputEventListener(InputEventListener inputEventListener) {
        inputEventListeners.add(inputEventListener);
    }

    public void removeInputEventListener(InputEventListener inputEventListener) {
        inputEventListeners.remove(inputEventListener);
    }

    public boolean getKeyDown(int keyCode) {
        return keyDown[keyCode];
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        switch(e.getID()) {
            case KeyEvent.KEY_PRESSED:
                keyPressed(e);
                break;
            case KeyEvent.KEY_RELEASED:
                keyReleased(e);
                break;
        }
        return false;
    }
}
