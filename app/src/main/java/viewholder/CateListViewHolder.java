package viewholder;

import android.view.View;
import android.widget.EditText;

import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import view.CircleView;

/**
 * Created by JuniperPhoton on 2016-07-25.
 */
public class CateListViewHolder extends BaseViewHolder {

    private final int IS_NORMAL = 0;
    private final int IS_HEADER = 1;

    private CircleView mCateCircle;
    private EditText mTextView;

    public CateListViewHolder(View view, int viewType) {
        super(view);
        if (viewType == IS_NORMAL) {
            mCateCircle = (CircleView) view.findViewById(R.id.raw_cate_per_cv);
            mTextView = (EditText) view.findViewById(R.id.raw_cate_per_tv);
        }
    }

    public void setCircleColor(int color) {
        mCateCircle.setEllipseColor(color);
    }

    public void setCateName(String name) {
        mTextView.setText(name);
    }
}
