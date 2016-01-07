package adapter;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.juniper.myerlistandroid.R;

/**
 * Created by dengw on 10/5/2015.
 */
public class CateSelectorAdapter extends RecyclerView.Adapter<CateSelectorAdapter.ViewHolder>
{
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        return null;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position)
    {

    }

    @Override
    public int getItemCount()
    {
        return 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder
    {
        private ImageView mCateImage;

        public ViewHolder(View itemView)
        {
            super(itemView);
            mCateImage = (ImageView) itemView.findViewById(R.id.cate_img);
        }
    }
}
