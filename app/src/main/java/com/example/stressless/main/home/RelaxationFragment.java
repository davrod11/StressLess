package com.example.stressless.main.home;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.GridLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.stressless.R;

import java.util.Random;

public class RelaxationFragment extends Fragment {

    private DrawingView drawingView;
    private FrameLayout gameContainer;
    private boolean isDrawingGameActive = true;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_relaxation, container, false);

        drawingView = new DrawingView(requireContext(), null);
        gameContainer = view.findViewById(R.id.game_container);

        Button clearButton = view.findViewById(R.id.clear_button);
        Button colorButton = view.findViewById(R.id.color_button);
        Button switchGameButton = view.findViewById(R.id.switch_game_button);

        gameContainer.addView(drawingView);

        clearButton.setOnClickListener(v -> drawingView.clearCanvas());

        colorButton.setOnClickListener(v -> {
            Random random = new Random();
            int color = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
            drawingView.setBrushColor(color);
        });

        switchGameButton.setOnClickListener(v -> toggleGame());

        return view;
    }

    private void toggleGame() {
        gameContainer.removeAllViews();

        if (isDrawingGameActive) {
            View popItGame = LayoutInflater.from(requireContext()).inflate(R.layout.pop_it_game, gameContainer, false);
            GridLayout gridLayout = popItGame.findViewById(R.id.popItGrid);

            int rows = 4;
            int cols = 4;
            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int buttonSize = screenWidth / cols - 30;

            for (int i = 0; i < rows * cols; i++) {
                Button button = new Button(requireContext());
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.OVAL);
                drawable.setStroke(2, android.graphics.Color.BLACK);
                drawable.setColor(getRandomPredefinedColor());
                button.setBackground(drawable);

                GridLayout.LayoutParams params = new GridLayout.LayoutParams();
                params.width = buttonSize;
                params.height = buttonSize;
                params.setMargins(8, 8, 8, 8);
                button.setLayoutParams(params);

                button.setOnClickListener(new View.OnClickListener() {
                    private boolean isPopped = false;

                    @Override
                    public void onClick(View v) {
                        if (!isPopped) {
                            button.setAlpha(0.5f);
                            isPopped = true;
                            playPopSound();
                        } else {
                            button.setAlpha(1.0f);
                            isPopped = false;
                            playUnpopSound();
                        }
                    }
                });

                gridLayout.addView(button);
            }

            gameContainer.addView(popItGame);
        } else {
            gameContainer.addView(drawingView);
        }

        isDrawingGameActive = !isDrawingGameActive;
    }

    private final int[] predefinedColors = {
            android.graphics.Color.RED,
            android.graphics.Color.GREEN,
            android.graphics.Color.BLUE,
            android.graphics.Color.YELLOW,
            android.graphics.Color.MAGENTA
    };

    private int getRandomPredefinedColor() {
        Random random = new Random();
        return predefinedColors[random.nextInt(predefinedColors.length)];
    }

    private void playPopSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.pop1);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }

    private void playUnpopSound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(requireContext(), R.raw.pop2);
        mediaPlayer.setOnCompletionListener(MediaPlayer::release);
        mediaPlayer.start();
    }


}
