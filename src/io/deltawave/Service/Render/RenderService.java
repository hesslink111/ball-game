package io.deltawave.Service.Render;

import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.utils.Array;
import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;

import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.awt.TextRenderer;
import com.jogamp.opengl.util.gl2.GLUT;
import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.Level;
import io.deltawave.Service.World.LevelSetListener;
import io.deltawave.View.Window;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by will on 6/2/16.
 */
public class RenderService implements GLEventListener, LevelSetListener {

    //Static variables
    public static final float[] DEFAULT_COLOR = {
            (float)Math.random() * 0.5f + 0.5f,
            (float)Math.random() * 0.5f + 0.5f,
            (float)Math.random() * 0.5f + 0.5f,
            1.0f
    };
    private static final double SCALE = 10.0;               //Pixels per meter
    private static final int N = 20;                        //Number of sides on a circle
    private static final double THETA = 2.0 * Math.PI / N;  //Angle of each side
    private static final double COS = Math.cos(THETA);      //Precomputed value
    private static final double SIN = Math.sin(THETA);

    //World
    private Level level;
    private GameObject ball;

    //Render variables
    private Window window;
    private GLCanvas canvas;
    private FPSAnimator animator;
    private GLUT glut;
    private Dimension size;

    //Render listeners
    private ArrayList<RenderFrameListener> renderFrameListeners;

    public RenderService(int width, int height, Level level) {

        //Set the level
        this.level = level;
        this.ball = level.getBall();

        // create the size of the window
        size = new Dimension(width, height);

        // setup OpenGL capabilities
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
        caps.setDoubleBuffered(true);
        caps.setHardwareAccelerated(true);
        glut = new GLUT();

        //Make the canvas
        canvas = new GLCanvas(caps);
        canvas.setPreferredSize(size);
        canvas.setMinimumSize(size);
        canvas.setMaximumSize(size);
        canvas.setIgnoreRepaint(true);
        canvas.addGLEventListener(this);
        window = new Window(canvas);

        //Set up listeners
        renderFrameListeners = new ArrayList<>();
    }

    public void start() {
        //Tell listeners
        notifyListenersRenderStart();

        // create an animator to animated the canvas
        animator = new FPSAnimator(canvas, 60);

        animator.setUpdateFPSFrames(3, null);

        // start the animator
        animator.start();
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public Window getWindow() {
        return window;
    }

    public void render(GL2 gl) {

        // apply a scaling transformation
        gl.glScaled(SCALE, SCALE, SCALE);

        //Render each object
        List<GameObject> gameObjects = level.getGameObjects();
        for(GameObject gameObject: gameObjects) {
            renderBody(gl, gameObject);
        }

        //Draw the ropes
        List<RopeJoint> ropeJoints = level.getRopeJoints();
        for(RopeJoint ropeJoint: ropeJoints) {
            renderRope(gl, ropeJoint);
        }

        //Draw the particles
        Transform transform = new Transform();
        Vector2 groupPosition = level.getParticleGroups().get(0).getCenter();
        Array<Vector2> particlePositions = level.getParticleSystem().getParticlePositionBuffer();
        Array<com.badlogic.gdx.graphics.Color> particleColors = level.getParticleSystem().getParticleColorBuffer();

        for(int i=0; i<particlePositions.size; i++) {
            renderParticle(gl, particlePositions.get(i), particleColors.get(i), groupPosition);
        }

        //Display fps
        renderFPS(gl);
    }

    private void renderParticle(GL2 gl, Vector2 position, com.badlogic.gdx.graphics.Color color, Vector2 groupPosition) {
        // save the original transform
        gl.glPushMatrix();

        //gl.glScaled(1, 0.5, 1);

        // transform the coordinate system from world coordinates to local coordinates
        gl.glTranslated(
                 - ball.getBody().getTransform().getPosition().x,
                 - ball.getBody().getTransform().getPosition().y,
                0.0);

        // set the color
        gl.glColor3f(color.r, color.g, color.b);

        //Fill circle
        double cx = position.x;
        double cy = position.y;

        double c = COS;
        double s = SIN;
        double t;

        // start at 0
        double x = 0.125;
        double y = 0;

        gl.glBegin(GL2.GL_POLYGON);
        for(int i = 0; i < N; i++)
        {
            gl.glVertex2d(x + cx, y + cy);//output vertex

            //apply the rotation matrix
            t = x;
            x = c * x - s * y;
            y = s * t + c * y;
        }
        gl.glEnd();

        // set the original transform
        gl.glPopMatrix();

    }

    private void renderFPS(GL2 gl) {
        float fps = animator.getLastFPS();

        String fpsString = String.format("FPS: %.2f", fps);

        gl.glPushMatrix();
        gl.glColor3f(1.0f, 0.0f, 0.0f);
        gl.glRasterPos2d(30, -20);
        glut.glutBitmapString(GLUT.BITMAP_HELVETICA_18, fpsString);
        gl.glPopMatrix();
    }

    public void renderBody(GL2 gl, GameObject gameObject) {

        // save the original transform
        gl.glPushMatrix();

        //Get transform from body
        Transform transform = gameObject.getBody().getTransform();

        // transform the coordinate system from world coordinates to local coordinates
        gl.glTranslated(
                transform.getPosition().x-ball.getBody().getTransform().getPosition().x,
                transform.getPosition().y-ball.getBody().getTransform().getPosition().y,
                0.0);
        //gl.glTranslated(transform.getTranslationX(), transform.getTranslationY(), 0.0);
        // rotate about the z-axis
        gl.glRotated(Math.toDegrees(gameObject.getBody().getTransform().getRotation()), 0.0, 0.0, 1.0);

        // set the color
        gl.glColor4fv(gameObject.getColor(), 0);

        for(Fixture fixture: gameObject.getBody().getFixtureList()) {
            Shape shape = fixture.getShape();

            if(shape instanceof PolygonShape) {
                renderPolygon(gl, (PolygonShape) shape);
            } else if(shape instanceof CircleShape) {
                renderCircle(gl, (CircleShape) shape);
            }

        }

        // set the original transform
        gl.glPopMatrix();
    }

    public void renderPolygon(GL2 gl, PolygonShape polygon) {
        //Fill polygon
        gl.glBegin(GL2.GL_POLYGON);
        int vCount = polygon.getVertexCount();
        for(int i=0; i<vCount; i++) {
            Vector2 v2 = new Vector2();
            polygon.getVertex(i, v2);
            gl.glVertex2f(v2.x, v2.y);
        }
        gl.glEnd();
    }

    //Only used for ball for now
    public void renderCircle(GL2 gl, CircleShape circle) {
        //Fill circle
        double cx = circle.getPosition().x;
        double cy = circle.getPosition().y;

        double c = COS;
        double s = SIN;
        double t;

        // start at 0
        double x = circle.getRadius();
        double y = 0;

        gl.glBegin(GL2.GL_POLYGON);
        for(int i = 0; i < N; i++)
        {
            gl.glVertex2d(x + cx, y + cy);//output vertex

            //apply the rotation matrix
            t = x;
            x = c * x - s * y;
            y = s * t + c * y;
        }
        gl.glEnd();
    }

    public void renderRope(GL2 gl, RopeJoint ropeJoint) {
        gl.glPushMatrix();
        gl.glColor3f(0x60 / 256f, 0x7D / 256f, 0x8B / 256f);
        Vector2 a = ropeJoint.getAnchorA();
        Vector2 b = ropeJoint.getAnchorB();
        gl.glTranslated(
                a.x-ball.getBody().getTransform().getPosition().x,
                a.y-ball.getBody().getTransform().getPosition().y,
                0f);
        gl.glLineWidth(5f);
        gl.glBegin(GL2.GL_LINES);
        gl.glVertex2f(0f, 0f);
        gl.glVertex2f(b.x-a.x, b.y-a.y);
        gl.glEnd();
        gl.glPopMatrix();
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        //I'm basically copying this stuff from a tutorial
        //https://github.com/wnbittle/dyn4j/blob/master/examples/org/dyn4j/examples/ExampleJOGL.java

        // get the OpenGL context
        GL2 gl = glAutoDrawable.getGL().getGL2();

        // set the matrix mode to projection
        gl.glMatrixMode(GL2.GL_PROJECTION);
        // initialize the matrix
        gl.glLoadIdentity();
        // set the view to a 2D view
        gl.glOrtho(
                -size.getWidth()/2,
                size.getWidth()/2,
                -size.getHeight()/2,
                size.getHeight()/2,
                0, 1);

        // switch to the model view matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        // initialize the matrix
        gl.glLoadIdentity();

        // set the clear color to white
        gl.glClearColor(1.0f, 1.0f, 1.0f, 1.0f);

        // set the swap interval to as fast as possible
        gl.setSwapInterval(0);

    }

    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {
        //Nothing to dispose of apparently
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        //This is called when the canvas is to be redrawn

        // get the OpenGL context
        GL2 gl = glAutoDrawable.getGL().getGL2();

        // clear the screen
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);
        // switch to the model view matrix
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        // initialize the matrix (0,0) is in the center of the window
        gl.glLoadIdentity();

        // render scene
        render(gl);

        // Inform others up frame rendering
        notifyRenderFrameListeners();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int i, int i1, int i2, int i3) {
        //This is called when the window is resized
    }

    public void addRenderFrameListener(RenderFrameListener renderFrameListener) {
        renderFrameListeners.add(renderFrameListener);
    }

    public void removeRenderFrameListener(RenderFrameListener renderFrameListener) {
        renderFrameListeners.remove(renderFrameListener);
    }

    private void notifyListenersRenderStart() {
        renderFrameListeners.stream().forEach(rfl -> rfl.onRenderStart());
    }

    private void notifyRenderFrameListeners() {
        renderFrameListeners.stream().forEach(rfl -> rfl.onRenderFrame());
    }

    @Override
    public void onLevelSet(Level level) {
        this.level = level;
        ball = level.getBall();
    }
}
