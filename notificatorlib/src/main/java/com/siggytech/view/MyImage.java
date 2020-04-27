package com.siggytech.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.core.widget.ContentLoadingProgressBar;

import com.siggytech.utils.communication.R;

public class MyImage extends RelativeLayout {

    private ContentLoadingProgressBar progressBar;
    private ImageView imageView;

    public MyImage(Context context) {
        super(context);
        init();
    }

    public MyImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

    }

    public MyImage(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();

    }

    private void init() {
        inflate(getContext(), R.layout.my_image, this);
        //Create your layout here
        progressBar = this.findViewById(R.id.pbProgress);
        imageView = this.findViewById(R.id.ivImage);
    }

    public ContentLoadingProgressBar getProgressBar() {
        return progressBar;
    }

    public ImageView getImageView() {
        return imageView;
    }

    public void setRoundImage(Bitmap mbitmap){
        try {
            Bitmap imageRounded = Bitmap.createBitmap(mbitmap.getWidth(), mbitmap.getHeight(), mbitmap.getConfig());
            Canvas canvas = new Canvas(imageRounded);
            Paint mpaint = new Paint();
            mpaint.setAntiAlias(true);
            mpaint.setShader(new BitmapShader(mbitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));
            canvas.drawRoundRect((new RectF(0, 0, mbitmap.getWidth(), mbitmap.getHeight())), 100, 100, mpaint); // Round Image Corner 100 100 100 100
            imageView.setImageBitmap(imageRounded);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
