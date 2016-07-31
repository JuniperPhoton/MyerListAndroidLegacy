package viewholder;

import android.view.View;

import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import view.CircleView;
import view.RectangleView;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class CateColorListViewHolder extends BaseViewHolder {

    private RectangleView mRectangleView;

    public CateColorListViewHolder(View view) {
        super(view);
        mRectangleView = (RectangleView) view.findViewById(R.id.row_cate_color_per_rect_view);
    }

    public void setColor(int color) {
        mRectangleView.setColor(color);
    }

    public RectangleView getRectView(){
        return mRectangleView;
    }
}
