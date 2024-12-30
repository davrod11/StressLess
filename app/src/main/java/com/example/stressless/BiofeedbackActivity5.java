package com.example.stressless;

import android.animation.ObjectAnimator;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Random;

public class BiofeedbackActivity5 extends AppCompatActivity {

    private View breathCircle;
    private TextView messageText;
    private MediaPlayer rainSound;
    private Handler messageHandler;
    private ArrayList<String> relaxationMessages;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_biofeedback_5);

        Button btnExit = findViewById(R.id.btnExit);
        btnExit.setOnClickListener(v -> {
            finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        breathCircle = findViewById(R.id.breathCircle);
        messageText = findViewById(R.id.messageText);

        rainSound = MediaPlayer.create(this, R.raw.relax);
        if (rainSound == null) {
            Log.e("BiofeedbackActivity5", "Error: MediaPlayer is null");
        } else {
            rainSound.setLooping(true);
            rainSound.start();
        }

        relaxationMessages = new ArrayList<>();
        relaxationMessages.add("Relax your mind");
        relaxationMessages.add("Close your eyes");
        relaxationMessages.add("Breathe deeply");
        relaxationMessages.add("Feel the calm around you");

        startBreathingAnimation();
        startRelaxationMessages();

        new Handler().postDelayed(this::finishSession, 5 * 60 * 1000);
    }

    private void startBreathingAnimation() {
        ObjectAnimator scaleUpX = ObjectAnimator.ofFloat(breathCircle, "scaleX", 1f, 1.5f);
        ObjectAnimator scaleUpY = ObjectAnimator.ofFloat(breathCircle, "scaleY", 1f, 1.5f);
        ObjectAnimator scaleDownX = ObjectAnimator.ofFloat(breathCircle, "scaleX", 1.5f, 1f);
        ObjectAnimator scaleDownY = ObjectAnimator.ofFloat(breathCircle, "scaleY", 1.5f, 1f);

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

    private void startRelaxationMessages() {
        messageHandler = new Handler();
        Runnable updateMessage = new Runnable() {
            @Override
            public void run() {
                int index = new Random().nextInt(relaxationMessages.size());
                messageText.setText(relaxationMessages.get(index));
                messageHandler.postDelayed(this, 5000);
            }
        };
        messageHandler.post(updateMessage);
    }

    private void finishSession() {
        if (rainSound.isPlaying()) {
            rainSound.stop();
            rainSound.release();
        }
        messageHandler.removeCallbacksAndMessages(null);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (rainSound != null && rainSound.isPlaying()) {
            rainSound.stop();
            rainSound.release();
        }
        if (messageHandler != null) {
            messageHandler.removeCallbacksAndMessages(null);
        }
    }
}
