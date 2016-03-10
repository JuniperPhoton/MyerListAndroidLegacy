package model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

/**
 * Created by dengw on 9/27/2015.
 */
public class ToDoCategory {

    public static String DefaultCateJsonString = "{ \"modified\":true, \"cates\":[{\"name\":\"Work\",\"color\":\"#FF436998\",\"id\":1},{\"name\":\"Life\",\"color\":\"#FFFFB542\",\"id\":2},{\"name\":\"Family\",\"color\":\"#FFFF395F\",\"id\":3},{\"name\":\"Entertainment\",\"color\":\"#FF55C1C1\",\"id\":4}]}";
    public static String ModifiedCateJsonStringFore = "{ \"modified\":true, \"cates\":";

    private String name;
    private int id;
    private Color color;

    public ToDoCategory(String name,int id,Color color) {
        this.name=name;
        this.id=id;
        this.color=color;
    }

    public String getName(){
        return this.name;
    }

    public int getID(){
        return this.id;
    }

    public Color getColor(){
        return this.color;
    }
}
