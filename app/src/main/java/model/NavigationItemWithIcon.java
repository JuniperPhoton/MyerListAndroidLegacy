package model;


import android.graphics.drawable.Drawable;

public class NavigationItemWithIcon {
    private String mText;
    private Drawable mDrawable;

    public NavigationItemWithIcon(String text, Drawable drawable) {
        mText = text;
        mDrawable = drawable;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public Drawable getDrawable() {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }
}
