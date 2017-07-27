package com.fendoudebb.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.renderscript.Type;
import android.util.Log;

/**
 * zbj on 2017-07-26 18:10.
 */

public class QuickBlur {
    private static final String TAG    = "QuickBlur";
    private static final float  SCALE  = 1 / 8.0F;//default scale
    private static final int    RADIUS = 5;//default radius

    private int   mRadius = RADIUS;
    private float mScale  = SCALE;

    private Bitmap  mBitmap;
    private Context mContext;

    private volatile static QuickBlur singleton = null;

    public static QuickBlur with(Context context) {
        if (singleton == null) {
            synchronized (QuickBlur.class) {
                if (singleton == null) {
                    singleton = new QuickBlur(context);
                }
            }
        }
        return singleton;
    }

    private QuickBlur(Context context) {
        mContext = context.getApplicationContext();
    }

    /**
     * 链式编程
     *
     * @param bitmap 需要被模糊的bitmap
     * @return Builder构建模式
     */
    public Builder bitmap(Bitmap bitmap) {
        if (bitmap != null) {
            return new Builder(bitmap);
        }
        return null;
    }

    public class Builder {
        Builder(Bitmap bitmap) {
            mBitmap = bitmap;
        }

        /**
         * 指定模糊前缩小的倍数,默认为8,即1/8的缩放
         *
         * @param scale 缩放的系数
         * @return Builder构建模式
         */
        public Builder scale(int scale) {
            mScale = 1.0f / scale;
            return this;
        }

        /**
         * 模糊半径,默认为5
         *
         * @param radius radius值域: (0,25]
         * @return Builder构建模式
         */
        public Builder radius(int radius) {
            mRadius = radius;
            return this;
        }

        public Bitmap blur() {
            if (mBitmap == null) {
                throw new RuntimeException("Bitmap can not be null");
            }
            if (mRadius == 0 || mRadius > 25) {
                throw new RuntimeException("radius must between  0 < r <= 25 ");
            }
            return rsBlur(mContext, mBitmap, mRadius, mScale);
        }
    }

    /**
     * 使用RenderScript 模糊图片
     *
     * @param context 上下文
     * @param source  传入的需要被模糊的原图片
     * @return 模糊完成后的bitmap
     */
    private Bitmap rsBlur(Context context, Bitmap source, int radius, float scale) {
        Log.d(TAG, "origin size:" + source.getWidth() + "*" + source.getHeight());
        // 计算图片缩小后的长宽
        int width = Math.round(source.getWidth() * scale);
        int height = Math.round(source.getHeight() * scale);

        if (width <= 0 || height <= 0) {
            Log.d(TAG, "rsBlur: width and height must be > 0");
            return source;
        }

        // 将缩小后的图片做为预渲染的图片。
        //java.lang.IllegalArgumentException: width and height must be > 0
        Bitmap inputBmp = Bitmap.createScaledBitmap(source, width, height, false);

        // 创建RenderScript内核对象
        RenderScript rs = RenderScript.create(context);

        Log.d(TAG, "scale size:" + inputBmp.getWidth() + "*" + inputBmp.getHeight());

        // 创建一个模糊效果的RenderScript的工具对象
        Element e = Element.U8_4(rs);
        ScriptIntrinsicBlur blurScript = ScriptIntrinsicBlur.create(rs, e);

        // 由于RenderScript并没有使用VM来分配内存,所以需要使用Allocation类来创建和分配内存空间。
        // 创建Allocation对象的时候其实内存是空的,需要使用copyTo()将数据填充进去。
        final Allocation input = Allocation.createFromBitmap(rs, inputBmp);
        Type type = input.getType();
        final Allocation output = Allocation.createTyped(rs, type);

        // 设置blurScript对象的输入内存
        blurScript.setInput(input);

        // 设置渲染的模糊程度, 25f是最大模糊度
        blurScript.setRadius(radius);

        // 将输出数据保存到输出内存中
        blurScript.forEach(output);

        // 将数据填充到Allocation中
        output.copyTo(inputBmp);

        //Destroy everything to free memory
//        e.destroy();
        input.destroy();
        output.destroy();
        blurScript.destroy();
//        type.destroy();
        rs.destroy();
        return inputBmp;
    }
}
