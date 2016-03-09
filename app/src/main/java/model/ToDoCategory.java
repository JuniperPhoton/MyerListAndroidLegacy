package model;

import android.graphics.drawable.Drawable;

/**
 * Created by dengw on 9/27/2015.
 */
public class ToDoCategory {
    private Drawable mDrawable;

    public ToDoCategory(Drawable drawable) {
        mDrawable = drawable;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
