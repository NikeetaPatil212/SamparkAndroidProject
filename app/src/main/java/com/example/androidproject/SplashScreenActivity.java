package com.example.androidproject;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.OvershootInterpolator;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

public class SplashScreenActivity extends AppCompatActivity {

    private CardView logoCard;
    private TextView tvAppName, tvTagline;
    private View dot1, dot2, dot3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        logoCard = findViewById(R.id.logoCard);
        tvAppName = findViewById(R.id.tvAppName);
        tvTagline = findViewById(R.id.tvTagline);

        dot1 = findViewById(R.id.dot1);
        dot2 = findViewById(R.id.dot2);
        dot3 = findViewById(R.id.dot3);

        startAnimation();
    }

    private void startAnimation() {

        Animation logoAnim =
                AnimationUtils.loadAnimation(this,R.anim.logo_scale);

        Animation textAnim =
                AnimationUtils.loadAnimation(this,R.anim.text_up);

        Animation fade =
                AnimationUtils.loadAnimation(this,R.anim.fade_in);

        logoCard.startAnimation(logoAnim);

        new Handler().postDelayed(() -> {
            tvAppName.startAnimation(textAnim);
        },300);

        new Handler().postDelayed(() -> {
            tvTagline.startAnimation(fade);
        },600);

        animateDots();

        new Handler().postDelayed(() -> {

            startActivity(new Intent(
                    SplashScreenActivity.this,
                    MainActivity.class));

            overridePendingTransition(
                    android.R.anim.fade_in,
                    android.R.anim.fade_out);

            finish();

        },2800);

    }

    private void animateDots() {

        AlphaAnimation alpha =
                new AlphaAnimation(0.3f,1f);

        alpha.setDuration(450);
        alpha.setRepeatMode(Animation.REVERSE);
        alpha.setRepeatCount(Animation.INFINITE);

        dot1.startAnimation(alpha);

        AlphaAnimation alpha2 =
                new AlphaAnimation(0.3f,1f);

        alpha2.setStartOffset(200);
        alpha2.setDuration(450);
        alpha2.setRepeatMode(Animation.REVERSE);
        alpha2.setRepeatCount(Animation.INFINITE);

        dot2.startAnimation(alpha2);

        AlphaAnimation alpha3 =
                new AlphaAnimation(0.3f,1f);

        alpha3.setStartOffset(400);
        alpha3.setDuration(450);
        alpha3.setRepeatMode(Animation.REVERSE);
        alpha3.setRepeatCount(Animation.INFINITE);

        dot3.startAnimation(alpha3);
    }
}