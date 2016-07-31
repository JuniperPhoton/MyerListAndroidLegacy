package model;

import android.graphics.Color;
import android.graphics.drawable.Drawable;

import com.google.gson.annotations.Expose;

/**
 * Created by dengw on 9/27/2015.
 */
public class ToDoCategory {

    public static String DefaultCateJsonString = "{ \"modified\":true, \"cates\":[{\"name\":\"Work\",\"color\":\"#FF436998\",\"id\":1},{\"name\":\"Life\",\"color\":\"#FFFFB542\",\"id\":2},{\"name\":\"Family\",\"color\":\"#FFFF395F\",\"id\":3},{\"name\":\"Entertainment\",\"color\":\"#FF55C1C1\",\"id\":4}]}";
    public static String ModifiedCateJsonStringFore = "{ \"modified\":true, \"cates\":";

    private String mName;
    private int mid;
    private int mcolor;


    public ToDoCategory(String name, int id, int color) {
        this.mName = name;
        this.mid = id;
        this.mcolor = color;
    }

//        public ToDoCategory(String name,int id,Drawable drawable) {
//        this.name=name;
//        this.mid=id;
//        this.mDrawable=drawable;
//    }

//    public Drawable getcurrentDrawable() {
//        return this.mcurrentDrawable;
//    }

    public String getName() {
        return this.mName;
    }

    public int getID() {
        return this.mid;
    }

    public int getColor() {
        return this.mcolor;
    }

    public void setName(String name) {
        this.mName = name;
    }

    public void setId(int id) {
        this.mid = id;
    }

    public void setColor(int color) {
        this.mcolor = color;
    }
}
