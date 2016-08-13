package viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

/**
 * Created by JuniperPhoton on 2016-07-25.
 */
public class DeleteItemViewHolder extends BaseViewHolder {
    public TextView mTextView;
    public ImageView mReDoView;
    public ImageView mDeleteView;

    public DeleteItemViewHolder(View itemView) {
        super(itemView);
        mTextView = (TextView) itemView.findViewById(R.id.row_deleted_tv);
        mReDoView = (ImageView) itemView.findViewById(R.id.row_deleted_reAdd_iv);
        mDeleteView = (ImageView) itemView.findViewById(R.id.row_deleted_btn_iv);
    }
}