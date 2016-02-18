package activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;

import com.juniperphoton.myerlistandroid.R;
import com.umeng.analytics.MobclickAgent;

import moe.feng.material.statusbar.StatusBarCompat;


public class AboutActivity extends AppCompatActivity {
    private ImageView mMaskView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StatusBarCompat.setUpActivity(this);

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

        setContentView(R.layout.activity_about);

        mMaskView = (ImageView) findViewById(R.id.activity_about_mask);
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            mMaskView.setVisibility(View.GONE);
        }
    }

    public void EmailClick(View view) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setType("message/rfc822");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"dengweichao@hotmail.com"}); // recipients
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "MyerList Android feedback");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "");
        startActivity(Intent.createChooser(emailIntent, "Choose app to send an email"));
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override

    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

}
