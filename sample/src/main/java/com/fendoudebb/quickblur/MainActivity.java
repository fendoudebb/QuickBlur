package com.fendoudebb.quickblur;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;

import com.fendoudebb.util.QuickBlur;

public class MainActivity extends AppCompatActivity {

    private ImageView mSrcPic;
    private View mBlurPic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSrcPic = (ImageView) findViewById(R.id.src_pic);
        mBlurPic = findViewById(R.id.blur_pic);
    }

    public void onClick(View view) {
        mSrcPic.setDrawingCacheEnabled(true);
        Bitmap drawingCache = mSrcPic.getDrawingCache();

        Bitmap srcBitmap = Bitmap.createBitmap((mBlurPic.getMeasuredWidth()),
                (mBlurPic.getMeasuredHeight()), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(srcBitmap);
        canvas.translate(-mBlurPic.getLeft(),-mBlurPic.getTop());
        canvas.drawBitmap(drawingCache, 0, 0, null);
        mSrcPic.setDrawingCacheEnabled(false);

        Bitmap blurBitmap = QuickBlur.with(getApplicationContext()).bitmap(srcBitmap).radius(15).scale(4).blur();
        mBlurPic.setBackground(new BitmapDrawable(getResources(),blurBitmap));
    }
}
