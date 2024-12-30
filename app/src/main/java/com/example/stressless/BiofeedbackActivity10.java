package com.example.stressless;

import android.animation.ValueAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class BiofeedbackActivity10 extends AppCompatActivity {

    private View animatedPolygon;
    private MediaPlayer rainSound;
    private TextView messageText;
    private List<String> relaxationMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biofeedback_10);

        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });


        animatedPolygon = findViewById(R.id.animatedPolygon);
        messageText = findViewById(R.id.messageText);

        rainSound = MediaPlayer.create(this, R.raw.relax2);
        if (rainSound == null) {
            Log.e("BiofeedbackActivity10", "Error: MediaPlayer is null");
        } else {
            rainSound.setLooping(true);
            rainSound.start();
        }

        relaxationMessages = new ArrayList<>();
        relaxationMessages.add("Go with the flow");
        relaxationMessages.add("Focus on your breath");
        relaxationMessages.add("Let go of tension");
        relaxationMessages.add("Feel the calm flow");

        startPolygonAnimation();
        startRelaxationMessages();
        new android.os.Handler().postDelayed(this::finishSession, 10 * 60 * 1000);
    }

    private void startPolygonAnimation() {
        ValueAnimator scaleAnimator = ValueAnimator.ofFloat(1f, 1.5f);
        scaleAnimator.setDuration(3000);
        scaleAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        scaleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        scaleAnimator.setRepeatCount(ValueAnimator.INFINITE);

        scaleAnimator.addUpdateListener(animation -> {
            float scale = (float) animation.getAnimatedValue();
            animatedPolygon.setScaleX(scale);
            animatedPolygon.setScaleY(scale);
        });

        ValueAnimator rotationAnimator = ValueAnimator.ofFloat(0f, 360f);
        rotationAnimator.setDuration(6000);
        rotationAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        rotationAnimator.setRepeatCount(ValueAnimator.INFINITE);

        rotationAnimator.addUpdateListener(animation -> {
            float rotation = (float) animation.getAnimatedValue();
            animatedPolygon.setRotation(rotation);
        });

        scaleAnimator.start();
        rotationAnimator.start();
    }


    private void startRelaxationMessages() {
        new android.os.Handler().postDelayed(() -> {
            Random random = new Random();
            String message = relaxationMessages.get(random.nextInt(relaxationMessages.size()));
            messageText.setText(message);
            startRelaxationMessages();
        }, 5000);
    }

    private void finishSession() {
        if (rainSound.isPlaying()) {
            rainSound.stop();
            rainSound.release();
        }
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rainSound != null && rainSound.isPlaying()) {
            rainSound.stop();
            rainSound.release();
        }
    }
}
