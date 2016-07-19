package activity;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.juniperphoton.myerlistandroid.R;

import adapter.CateListAdapter;
import moe.feng.material.statusbar.StatusBarCompat;

/**
 * Created by JuniperPhoton on 2016-07-17.
 */
public class CatePersonalizaionActivity extends AppCompatActivity {

    private RecyclerView mCateListRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarCompat.setUpActivity(this);
        setContentView(R.layout.activity_cate_per);

        LinearLayoutManager linearLayoutManager=new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        mCateListRecyclerView=(RecyclerView)findViewById(R.id.activity_cate_per_rv);
        mCateListRecyclerView.setLayoutManager(linearLayoutManager);
        mCateListRecyclerView.setAdapter(new CateListAdapter());
    }
}
