package activity;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;

import com.juniperphoton.jputils.LocalSettingHelper;
import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import moe.feng.material.statusbar.StatusBarCompat;


public class StartActivity extends AppCompatActivity {
    @Bind(R.id.activity_start_root_ll)
    LinearLayout mRootLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_start);
        ButterKnife.bind(this);

        mRootLinearLayout.setAlpha(0f);
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
        mRootLinearLayout.setAlpha(0f);
        startNavigatedToAnim();
    }

    @Override

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    private void startNavigatedToAnim() {
        ValueAnimator offsetAnimator = new ValueAnimator();
        offsetAnimator.setDuration(500);
        offsetAnimator.setIntValues(100, 0);
        offsetAnimator.setStartDelay(600);
        offsetAnimator.setInterpolator(new DecelerateInterpolator(1.5f));
        offsetAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRootLinearLayout.scrollTo(0, (int) valueAnimator.getAnimatedValue());
            }
        });

        ValueAnimator fadeAnimator = new ValueAnimator();
        fadeAnimator.setDuration(500);
        fadeAnimator.setFloatValues(0f, 1f);
        fadeAnimator.setStartDelay(600);
        fadeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRootLinearLayout.setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
        offsetAnimator.start();
        fadeAnimator.start();
    }

    @OnClick(R.id.activity_start_login_btn)
    public void toLoginClick(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToLogin");
        startActivity(intent);
    }

    @OnClick(R.id.activity_start_register_btn)
    public void toRegisterClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToRegister");
        startActivity(intent);
    }

    @OnClick(R.id.activity_start_offline_btn)
    public void toMainClick(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("LOGIN_STATE", "OfflineMode");
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_CLEAR_TASK |
                Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

        LocalSettingHelper.putBoolean(this, "offline_mode", true);
    }
}