package io.deltawave.Service.Level;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import finnstr.libgdx.liquidfun.ParticleGroup;
import io.deltawave.Domain.GameObject;
import io.deltawave.Domain.Level;
import io.deltawave.Service.World.LevelSetListener;
import io.deltawave.Util.ColorConverter;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * Created by will on 6/3/16.
 */
public class LevelService {

    //Static variables
    private static String MAP_FILE_PATH = "maps/";
    private static float MAP_SCALE_FACTOR = 0.5f;

    //Current level
    private Level level;

    //Listeners
    private ArrayList<LevelSetListener> levelSetListeners;


    public LevelService() {

        //Setup listeners
        levelSetListeners = new ArrayList<>();

        //loadLevelFile("testmap2.json");
        loadBlankLevel();
    }

    public void loadBlankLevel() {
        LevelBuilder lb = new LevelBuilder();
        lb.setBall(lb.addCircle(2, 0, 52 + 4, 10 + 2, false, "black"));
        lb.addRectangle(1, 43, 0, 34 + 1, 89 + 43, true, "red");
        lb.addRectangle(19, 1, 0, 36 + 19, 3 + 1, true, "red");
        lb.addRectangle(1, 17, 0, 74 + 1, 47 + 17, true, "red");
        lb.setFinish(lb.addRectangle(2.5f, 1.5f, 0, 387 + 2.5f, 418 + 1.5f, true, "blue"));

        level = lb.buildLevel();
        notifyLevelSetListeners(level);
    }

    public Level getLevel() {
        return level;
    }

    public Level loadLevelFile(String levelName) {

        System.out.println("Loading level: " + levelName);

        Scanner mapFileScanner = openMapFile(MAP_FILE_PATH + levelName);
        String mapString = readMapFile(mapFileScanner);
        JsonObject jsonMap = parseMap(mapString);
        level = createLevelFromJson(jsonMap);

        //Notify listeners that a level has been loaded
        notifyLevelSetListeners(level);

        return level;
    }

    private Scanner openMapFile(String mapName) {
        Scanner reader = null;
        try {
            reader = new Scanner(new FileReader(mapName));
        } catch (FileNotFoundException e) {
            System.err.println("Could not find map file");
            e.printStackTrace();
            System.exit(1);
        }
        return reader;
    }

    private String readMapFile(Scanner fileScanner) {
        StringBuffer buffer = new StringBuffer(1024);
        while(fileScanner.hasNextLine()) {
            buffer.append(fileScanner.nextLine());
        }
        return buffer.toString();
    }

    private JsonObject parseMap(String mapString) {
        JsonParser jsonParser = new JsonParser();
        JsonObject jsonMap = jsonParser.parse(mapString).getAsJsonObject();
        return jsonMap;
    }

    private Level createLevelFromJson(JsonObject jsonMap) {

        LevelBuilder levelBuilder = new LevelBuilder();

        //Get the height and width
        float mapHeight = jsonMap.getAsJsonPrimitive("height").getAsFloat();
        float mapWidth = jsonMap.getAsJsonPrimitive("height").getAsFloat();

        ArrayList<JsonObject> jsonBall = new ArrayList<>();
        ArrayList<JsonObject> jsonFinish = new ArrayList<>();
        ArrayList<JsonObject> jsonRectangles = new ArrayList<>();
        ArrayList<JsonObject> jsonRopeJoints = new ArrayList<>();
        ArrayList<JsonObject> jsonLiquids = new ArrayList<>();

        //Read each layer
        JsonArray layers = jsonMap.getAsJsonArray("layers");
        layers.forEach(layer -> {
            JsonObject layerObject = layer.getAsJsonObject();
            JsonArray objects = layerObject.getAsJsonArray("objects");

            //Read the objects
            objects.forEach(object -> {
                JsonObject jsonBody = object.getAsJsonObject();
                if (jsonBody.getAsJsonPrimitive("name").getAsString().equals("ball")) {
                    jsonBall.add(jsonBody);
                } else if (jsonBody.getAsJsonPrimitive("type").getAsString().equals("rope")) {
                    jsonRopeJoints.add(jsonBody);
                } else if (jsonBody.getAsJsonPrimitive("type").getAsString().equals("liquid")) {
                    jsonLiquids.add(jsonBody);
                } else if (jsonBody.getAsJsonPrimitive("name").getAsString().equals("finish")) {
                    jsonFinish.add(jsonBody);
                } else {
                    jsonRectangles.add(jsonBody);
                }
            });
        });

        //Add these items to the map in order
        jsonBall.forEach(jb -> {
            GameObject ball = addCircleFromJson(jb, mapHeight, levelBuilder);
            levelBuilder.setBall(ball);
        });
        jsonFinish.forEach(jf -> {
            GameObject finish = addRectangleFromJson(jf, mapHeight, levelBuilder);
            levelBuilder.setFinish(finish);
        });
        jsonRectangles.forEach(jr -> {
            addRectangleFromJson(jr, mapHeight, levelBuilder);
        });
        jsonRopeJoints.forEach(jrj -> {
            addRope(jrj, mapHeight, levelBuilder);
        });
        jsonLiquids.forEach(jl -> {
            addLiquidFromJson(jl, mapHeight, levelBuilder);
        });

        return levelBuilder.buildLevel();
    }

    private ParticleGroup addLiquidFromJson(JsonObject jsonBody, float mapHeight, LevelBuilder levelBuilder) {
        String color;
        try {
            color = jsonBody.getAsJsonObject("properties").getAsJsonPrimitive("color").getAsString();
        } catch(NullPointerException ex) {
            color = "black";
        }
        float height = jsonBody.getAsJsonPrimitive("height").getAsFloat() / 2f;
        float width = jsonBody.getAsJsonPrimitive("width").getAsFloat() / 2f;
        float rotation = jsonBody.getAsJsonPrimitive("rotation").getAsFloat();
        float x = jsonBody.getAsJsonPrimitive("x").getAsFloat() + width;
        float y = mapHeight - (jsonBody.getAsJsonPrimitive("y").getAsFloat() + height);
        ParticleGroup particleGroup = levelBuilder.addParticles(
                MAP_SCALE_FACTOR * width,
                MAP_SCALE_FACTOR * height,
                rotation,
                MAP_SCALE_FACTOR * x,
                MAP_SCALE_FACTOR * y,
                color);
        return particleGroup;
    }

    private void addRope(JsonObject jsonBody, float mapHeight, LevelBuilder levelBuilder) {
        float x = jsonBody.getAsJsonPrimitive("x").getAsFloat();
        float y = mapHeight - (jsonBody.getAsJsonPrimitive("y").getAsFloat());

        JsonArray polyline = jsonBody.getAsJsonArray("polyline");
        float p1x = x + polyline.get(0).getAsJsonObject().getAsJsonPrimitive("x").getAsFloat();
        float p1y = y - polyline.get(0).getAsJsonObject().getAsJsonPrimitive("y").getAsFloat();
        float p2x = x + polyline.get(1).getAsJsonObject().getAsJsonPrimitive("x").getAsFloat();
        float p2y = y - polyline.get(1).getAsJsonObject().getAsJsonPrimitive("y").getAsFloat();

        levelBuilder.addRope(
                MAP_SCALE_FACTOR * p1x,
                MAP_SCALE_FACTOR * p1y,
                MAP_SCALE_FACTOR * p2x,
                MAP_SCALE_FACTOR * p2y);
    }

    private GameObject addRectangleFromJson(JsonObject jsonBody, float mapHeight, LevelBuilder levelBuilder) {
        String color;
        try {
            color = jsonBody.getAsJsonObject("properties").getAsJsonPrimitive("color").getAsString();
        } catch(NullPointerException ex) {
            color = "black";
        }
        float height = jsonBody.getAsJsonPrimitive("height").getAsFloat() / 2f;
        float width = jsonBody.getAsJsonPrimitive("width").getAsFloat() / 2f;
        float rotation = jsonBody.getAsJsonPrimitive("rotation").getAsFloat();
        boolean fixed = jsonBody.getAsJsonPrimitive("type").getAsString().equals("fixed");
        float x = jsonBody.getAsJsonPrimitive("x").getAsFloat() + width;
        float y = mapHeight - (jsonBody.getAsJsonPrimitive("y").getAsFloat() + height);
        GameObject gameObject = levelBuilder.addRectangle(
                MAP_SCALE_FACTOR * width,
                MAP_SCALE_FACTOR * height,
                rotation,
                MAP_SCALE_FACTOR * x,
                MAP_SCALE_FACTOR * y,
                fixed, color);
        return gameObject;
    }

    private GameObject addCircleFromJson(JsonObject jsonBody, float mapHeight, LevelBuilder levelBuilder) {
        String color;
        try {
            color = jsonBody.getAsJsonObject("properties").getAsJsonPrimitive("color").getAsString();
        } catch(NullPointerException ex) {
            color = "black";
        }
        float height = jsonBody.getAsJsonPrimitive("height").getAsFloat();
        float radius = height / 2.0f;
        float rotation = jsonBody.getAsJsonPrimitive("rotation").getAsFloat();
        boolean fixed = jsonBody.getAsJsonPrimitive("type").getAsString().equals("fixed");
        float x = jsonBody.getAsJsonPrimitive("x").getAsFloat() + radius;
        float y = mapHeight - (jsonBody.getAsJsonPrimitive("y").getAsFloat() + radius);
        GameObject gameObject = levelBuilder.addCircle(
                MAP_SCALE_FACTOR * radius,
                rotation,
                MAP_SCALE_FACTOR * x,
                MAP_SCALE_FACTOR * y,
                fixed, color);
        return gameObject;
    }

    public void addLevelSetListener(LevelSetListener levelSetListener) {
        levelSetListeners.add(levelSetListener);
    }

    public void removeLevelSetListener(LevelSetListener levelSetListener) {
        levelSetListeners.remove(levelSetListener);
    }

    public void notifyLevelSetListeners(Level level) {
        levelSetListeners.stream().forEach(lsl -> lsl.onLevelSet(level));
    }

}
