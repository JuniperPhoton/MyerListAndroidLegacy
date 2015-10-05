package model;

import android.graphics.drawable.Drawable;

/**
 * Created by dengw on 9/27/2015.
 */
public class CateNaviItem
{
    private Drawable mDrawable;

    public CateNaviItem(Drawable drawable)
    {
        mDrawable = drawable;
    }

    public Drawable getDrawable()
    {
        return mDrawable;
    }

    public void setDrawable(Drawable drawable)
    {
        mDrawable = drawable;
    }
}
