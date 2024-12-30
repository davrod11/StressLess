package com.example.stressless;

import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MeditationActivity extends AppCompatActivity {

    private View breathAnimation;
    private TextView timerText;
    private MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meditation);

        breathAnimation = findViewById(R.id.breathAnimation);
        timerText = findViewById(R.id.timerText);

        mediaPlayer = MediaPlayer.create(this, R.raw.relax);
        if (mediaPlayer == null) {
            Log.e("MeditationActivity", "Error: MediaPlayer is null");
        } else {
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
        }
        startBreathingAnimation();
        startTimer();
        findViewById(R.id.exitButton).setOnClickListener(v -> finishMeditation());
    }

    private void startBreathingAnimation() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(breathAnimation, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(breathAnimation, "scaleY", 1f, 1.5f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(breathAnimation, "scaleX", 1.5f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(breathAnimation, "scaleY", 1.5f, 1f);
        scaleUpX.setDuration(3000);
        scaleUpY.setDuration(3000);
        scaleDownX.setDuration(3000);
        scaleDownY.setDuration(3000);
        scaleUpX.start();
        scaleUpY.start();
        scaleUpX.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                scaleDownX.start();
                scaleDownY.start();
            }
        });
        scaleDownX.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                startBreathingAnimation();
            }
        });
    }
    private void startTimer() {
        new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                int seconds = (int) (millisUntilFinished / 1000);
                timerText.setText(String.format("%d:%02d", seconds / 60, seconds % 60));
            }

            @Override
            public void onFinish() {
                finishMeditation();
            }
        }.start();
    }
    private void finishMeditation() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        finish();
    }
}