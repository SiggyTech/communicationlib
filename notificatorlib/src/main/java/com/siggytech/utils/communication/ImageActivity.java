package com.siggytech.utils.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.siggytech.view.PhotoView;


public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        try {
            Uri imageUri = Uri.parse(getIntent().getStringExtra("ImageUri"));
            try {
                //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                Bitmap bitmap = BitmapFactory.decodeFile(imageUri.toString());
                PhotoView photoView = findViewById(R.id.iv_photo);
                photoView.setImageBitmap(bitmap);
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
