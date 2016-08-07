package viewholder;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

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
    private EditText mEditText;
    private LinearLayout mRootLayout;
    private ImageView mDeleteView;
    private RelativeLayout mCircleParent;

    private TextWatcher mWatcher;

    public CateListViewHolder(View view, int viewType) {
        super(view);
        if (viewType == IS_NORMAL) {
            mCateCircle = (CircleView) view.findViewById(R.id.raw_cate_per_cv);
            mEditText = (EditText) view.findViewById(R.id.raw_cate_per_tv);
            mDeleteView = (ImageView) view.findViewById(R.id.row_cate_per_deleteView);
            mCircleParent = (RelativeLayout) view.findViewById(R.id.raw_cate_per_rl);
        }
        mRootLayout = (LinearLayout) view.findViewById(R.id.row_cate_per_ll);
    }

    public void setCircleColor(int color) {
        mCateCircle.setColor(color);
    }

    public void setCateName(String name) {
        String oldText = mEditText.getText().toString();
        if (oldText.equals(name)) return;
        mEditText.setText(name);
    }

    public LinearLayout getRootView() {
        return mRootLayout;
    }

    public ImageView getDeleteView() {
        return this.mDeleteView;
    }

    public CircleView getCircleView() {
        return mCateCircle;
    }

    public RelativeLayout getCircleParent() {
        return mCircleParent;
    }

    public void setWatcher(TextWatcher watcher) {
        if (mWatcher == null) {
            mWatcher = watcher;
            mEditText.addTextChangedListener(mWatcher);
        }
    }

    public void removeWatcher() {
        if (mWatcher != null) {
            mEditText.removeTextChangedListener(mWatcher);
        }
    }
}
