package com.siggytech.utils.communication.presentation;

import android.net.Uri;
import android.os.Bundle;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;

import com.siggytech.utils.communication.R;

/**
 * Video Android support 3GGP (.3gp), MPEG-4 (.mp4), WebM (.webm), Matroska (.mkv)
 */
public class VideoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        try {
            Uri videoUri = Uri.parse(getIntent().getStringExtra("VideoUri"));
            try {
                MediaController mediaController = new MediaController(this);
                VideoView videoView = findViewById(R.id.videoView);
                videoView.setVideoURI(videoUri);
                videoView.setMediaController(mediaController);
                mediaController.setAnchorView(videoView);
                videoView.start();
            } catch (Exception e) {
                e.printStackTrace();
                finish();
            }
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
}
