package viewholder;

import android.view.View;
import android.widget.ImageView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import view.CircleView;
import view.RectangleView;

public class PickColorViewHolder extends BaseViewHolder {

    private CircleView mRectangleView;

    public PickColorViewHolder(View view) {
        super(view);
        mRectangleView = (CircleView) view.findViewById(R.id.row_cate_color_per_rect_view);
    }

    public void setColor(int color) {
        mRectangleView.setColor(color);
    }

    public CircleView getRectView() {
        return mRectangleView;
    }
}
