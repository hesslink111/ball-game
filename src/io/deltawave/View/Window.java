package io.deltawave.View;

import javax.swing.*;
import java.awt.*;

/**
 * Created by will on 6/2/16.
 */
public class Window extends JFrame {

    public Window(Canvas canvas) {
        setTitle("Game Window");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        add(canvas);
        pack();
        setResizable(false);
        setVisible(true);
    }


}
