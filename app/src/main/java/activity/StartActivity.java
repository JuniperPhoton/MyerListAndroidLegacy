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

import moe.feng.material.statusbar.StatusBarCompat;


public class StartActivity extends AppCompatActivity {
    private LinearLayout mRootLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        setContentView(R.layout.activity_start);

        mRootLinearLayout = (LinearLayout) findViewById(R.id.activity_start_root_ll);
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
        ValueAnimator valueAnimator1 = new ValueAnimator();
        valueAnimator1.setDuration(500);
        valueAnimator1.setIntValues(100, 0);
        valueAnimator1.setStartDelay(600);
        valueAnimator1.setInterpolator(new DecelerateInterpolator(1.5f));
        valueAnimator1.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRootLinearLayout.scrollTo(0, (int) valueAnimator.getAnimatedValue());
            }
        });

        ValueAnimator valueAnimator2 = new ValueAnimator();
        valueAnimator2.setDuration(500);
        valueAnimator2.setFloatValues(0f, 1f);
        valueAnimator2.setStartDelay(600);
        valueAnimator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                mRootLinearLayout.setAlpha((float) valueAnimator.getAnimatedValue());
            }
        });
        valueAnimator1.start();
        valueAnimator2.start();
    }

    public void toLoginClick(View v) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToLogin");
        startActivity(intent);
    }

    public void toRegisterClick(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.putExtra("LOGIN_STATE", "ToRegister");
        startActivity(intent);
    }

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