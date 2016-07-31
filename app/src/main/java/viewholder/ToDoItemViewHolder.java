package viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import view.CircleView;

/**
 * Created by JuniperPhoton on 2016-07-25.
 */
public class ToDoItemViewHolder extends BaseViewHolder {
    private String mId;
    public TextView mTextView;
    public ImageView mLineView;
    public RelativeLayout mRelativeLayout;
    public ImageView mGreenImageView;
    public ImageView mRedImageView;
    public CircleView mCateCircle;

    public ToDoItemViewHolder(View itemView, int viewType) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.todoBlock);
        mLineView = (ImageView) itemView.findViewById(R.id.lineView);
        mGreenImageView = (ImageView) itemView.findViewById(R.id.greenImageView);
        mRedImageView = (ImageView) itemView.findViewById(R.id.redImageView);
        mRelativeLayout = (RelativeLayout) itemView.findViewById(R.id.todo_layout);
        mCateCircle = (CircleView) itemView.findViewById(R.id.cateCircle);
    }

    public String getID() {
        return mId;
    }

    public void setID(String id) {
        this.mId = id;
    }
}