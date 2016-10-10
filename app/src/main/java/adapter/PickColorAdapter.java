package adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import java.util.List;

import interfaces.IPickedColor;
import model.ColorWrapper;
import viewholder.PickColorViewHolder;

public class PickColorAdapter extends BaseQuickAdapter<ColorWrapper> {

    private IPickedColor mCallback;

    public PickColorAdapter(List<ColorWrapper> data, IPickedColor callback) {
        super(R.id.row_cate_color_per_rect_view, data);
        mCallback = callback;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final ColorWrapper color) {
        final PickColorViewHolder holder = (PickColorViewHolder) baseViewHolder;
        holder.setColor(color.getColor());
        holder.getRectView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mCallback != null) {
                    mCallback.pickedColor(color.getColor());
                }
            }
        });
    }

    @Override
    public PickColorViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cate_color_per, parent, false);
        return new PickColorViewHolder(view);
    }
}
