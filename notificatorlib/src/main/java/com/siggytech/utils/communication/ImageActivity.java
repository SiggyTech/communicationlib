package com.siggytech.utils.communication;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.siggytech.view.PhotoView;


public class ImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);

        try {
            Uri url = Uri.parse(getIntent().getStringExtra("ImageUri"));
            try {
                PhotoView photoView = findViewById(R.id.iv_photo);

                if(getIntent().getStringExtra("ImageUri").startsWith(Conf.HTTP)) {
                    long thumb = 5000 * 1000;
                    RequestOptions options = new RequestOptions().frame(thumb);
                    Glide.with(ImageActivity.this).load(url).apply(options).into(photoView);
                }else{
                    Bitmap bitmap = BitmapFactory.decodeFile(getIntent().getStringExtra("ImageUri"));
                    photoView.setImageBitmap(bitmap);
                }
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
