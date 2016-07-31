package viewholder;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

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
    private LinearLayout mRootLayout;
    private ImageView mDeleteView;

    public CateListViewHolder(View view, int viewType) {
        super(view);
        if (viewType == IS_NORMAL) {
            mCateCircle = (CircleView) view.findViewById(R.id.raw_cate_per_cv);
            mTextView = (EditText) view.findViewById(R.id.raw_cate_per_tv);
            mDeleteView = (ImageView) view.findViewById(R.id.row_cate_per_deleteView);
        }
        mRootLayout = (LinearLayout) view.findViewById(R.id.row_cate_per_ll);
    }

    public void setCircleColor(int color) {
        mCateCircle.setColor(color);
    }

    public void setCateName(String name) {
        mTextView.setText(name);
    }

    public LinearLayout getRootView() {
        return mRootLayout;
    }

    public ImageView getDeleteView() {
        return this.mDeleteView;
    }

    public EditText getTextView(){
        return mTextView;
    }

    public CircleView getCircleView(){
        return mCateCircle;
    }
}
