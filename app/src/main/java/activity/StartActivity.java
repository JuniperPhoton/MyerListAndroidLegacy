package activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.juniper.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import util.ConfigHelper;
import moe.feng.material.statusbar.StatusBarCompat;


public class StartActivity extends AppCompatActivity
{
    private LinearLayout mRootLinearLayout;
    private ImageView mLogoImageView;
    private LinearLayout mTitleTextView;
    private TextView mSubTitleTextView;
    private Button mLoginBtnView;
    private Button mRegisterBtnView;
    private Button mOfflineBtnView;
    private ImageView mMaskView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_start);

        mRootLinearLayout=(LinearLayout)findViewById(R.id.rootLinearLayout);
        mRootLinearLayout.setAlpha(0f);

        mMaskView=(ImageView)findViewById(R.id.activity_start_mask);
        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT)
        {
            mMaskView.setVisibility(View.GONE);
        }

        if(Build.VERSION.SDK_INT<Build.VERSION_CODES.LOLLIPOP)
        {
            CardView toLoginCard=(CardView)findViewById(R.id.toLoginBtn_cardview);
            LinearLayout.LayoutParams layoutParamsForTop=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsForTop.setMargins(40,60,40,0);
            layoutParamsForTop.height=140;
            toLoginCard.setLayoutParams(layoutParamsForTop);

            CardView toRegisterCard=(CardView)findViewById(R.id.toRegisterBtn_cardview);
            LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.setMargins(40,0,40,0);
            layoutParams.height=140;
            toRegisterCard.setLayoutParams(layoutParams);

            CardView toMainCard=(CardView)findViewById(R.id.toMainBtn_cardview);
            LinearLayout.LayoutParams layoutParamsForBottom=new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParamsForBottom.setMargins(40,0,40,10);
            layoutParamsForBottom.height=140;

            toMainCard.setLayoutParams(layoutParamsForBottom);
        }
    }

    private void startNavigatedToAnim()
    {
        ValueAnimator  valueAnimator1=new ValueAnimator();
        valueAnimator1.setDuration(500);
        valueAnimator1.setIntValues(100, 0);
        valueAnimator1.setStartDelay(600);
        valueAnimator1.setInterpolator(new DecelerateInterpolator(1.5f));
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                mRootLinearLayout.scrollTo(0,(int)valueAnimator.getAnimatedValue());
            }
        });

        ValueAnimator  valueAnimator2=new ValueAnimator();
        valueAnimator2.setDuration(500);
        valueAnimator2.setFloatValues(0f, 1f);
        valueAnimator2.setStartDelay(600);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener()
        {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator)
            {
                mRootLinearLayout.setAlpha((float)valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator1.start();
        valueAnimator2.start();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        MobclickAgent.onResume(this);
        mRootLinearLayout.setAlpha(0f);
        startNavigatedToAnim();
    }
    @Override

    public void onPause()
    {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    public void toLoginClick(View v)
    {
        Intent intent=new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToLogin");
        startActivity(intent);
    }

    public void toRegisterClick(View view)
    {
        Intent intent=new Intent(this,LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToRegister");
        startActivity(intent);
    }

    public void toMainClick(View view)
    {
        Intent intent=new Intent(this,MainActivity.class);
        intent.putExtra("LOGIN_STATE","OfflineMode");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        ConfigHelper.putBoolean(this, "offline_mode", true);
    }
}