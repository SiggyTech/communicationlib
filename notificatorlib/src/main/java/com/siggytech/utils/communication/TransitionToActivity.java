package com.siggytech.utils.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.siggytech.view.PhotoView;


public class TransitionToActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transition_to);

        Uri imageUri = Uri.parse(getIntent().getStringExtra("ImageUri"));

        try {
            //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
            Bitmap bitmap = BitmapFactory.decodeFile(imageUri.toString());
            PhotoView photoView = findViewById(R.id.iv_photo);
            photoView.setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
            finish();
        }
    }
}
