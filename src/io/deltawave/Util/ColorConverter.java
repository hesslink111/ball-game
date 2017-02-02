package io.deltawave.Util;

/**
 * Created by will on 6/3/16.
 */
public class ColorConverter {

    public static float[] StringToFloat(String colorString) {

        if(colorString == null) {
            colorString = "black";
        }


        float[] color = new float[3];

        switch(colorString.toLowerCase()) {
            case "blue":
                color[0] = 0x21;
                color[1] = 0x96;
                color[2] = 0xF3;
                break;

            case "red":
                color[0] = 0xF4;
                color[1] = 0x43;
                color[2] = 0x36;
                break;

            case "green":
                color[0] = 0x4C;
                color[1] = 0xAF;
                color[2] = 0x50;
                break;

            case "white":
                color[0] = 0xFF;
                color[1] = 0xFF;
                color[2] = 0xFF;
                break;

            case "orange":
                color[0] = 0xFF;
                color[1] = 0x57;
                color[2] = 0x22;
                break;

            case "brown":
                color[0] = 0x79;
                color[1] = 0x55;
                color[2] = 0x48;
                break;

            case "grey":
                color[0] = 0x9E;
                color[1] = 0x9E;
                color[2] = 0x9E;
                break;

            case "purple":
                color[0] = 0x9C;
                color[1] = 0x27;
                color[2] = 0xB0;
                break;

            case "black":
            default:
                color[0] = 0x00;
                color[1] = 0x00;
                color[2] = 0x00;
                break;
        }

        color[0] /= 256.0;
        color[1] /= 256.0;
        color[2] /= 256.0;

        return color;
    }

}
