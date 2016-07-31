package adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.juniperphoton.myerlistandroid.R;

import java.util.List;

import interfaces.IPickedColor;
import model.ColorWrapper;
import viewholder.CateColorListViewHolder;
import viewholder.CateListViewHolder;

/**
 * Created by JuniperPhoton on 2016-07-31.
 */
public class CateColorAdapter extends BaseQuickAdapter<ColorWrapper> {

    private IPickedColor mCallback;

    public CateColorAdapter(List<ColorWrapper> data, IPickedColor callback) {
        super(R.id.row_cate_color_per_rect_view, data);
        mCallback = callback;
    }

    @Override
    protected void convert(BaseViewHolder baseViewHolder, final ColorWrapper color) {
        CateColorListViewHolder holder = (CateColorListViewHolder) baseViewHolder;
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
    public CateColorListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_cate_color_per, parent, false);
        return new CateColorListViewHolder(view);
    }
}
