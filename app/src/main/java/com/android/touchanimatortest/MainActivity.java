package com.android.touchanimatortest;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "Touch";
    private ImageView mImageBg;
    private ImageView view;
    private FrameLayout.LayoutParams params;
    private Bitmap finalBitmap;
    Rect originRect = new Rect();
    Rect tmp;
    DisplayMetrics metrics;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //  final ImageView imageView = (ImageView) findViewById(R.id.blur_image);
        mImageBg = (ImageView) findViewById(R.id.img);
        view = (ImageView) findViewById(R.id.blur_text);
        final Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.test);

        //  imageView.setImageBitmap(bitmap);

        long startTime = System.currentTimeMillis();

        //Bitmap finalBitmap = EasyBlur.fastBlur(bitmap,scale,20);

        long endTime = System.currentTimeMillis();


        mImageBg.setImageBitmap(bitmap);

        mImageBg.getViewTreeObserver().addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
            @Override
            public boolean onPreDraw() {
                mImageBg.getViewTreeObserver().removeOnPreDrawListener(this);
                mImageBg.buildDrawingCache();
                Bitmap bmp = mImageBg.getDrawingCache();
                Bitmap overlay = Bitmap.createBitmap((int) (view.getMeasuredWidth()),
                        (int) (view.getMeasuredHeight()), Bitmap.Config.ARGB_8888);

                Canvas canvas = new Canvas(overlay);

                canvas.translate(-view.getLeft(), -view.getTop());
                canvas.drawBitmap(bmp, 0, 0, null);

                finalBitmap = EasyBlur.with(MainActivity.this)
                        .bitmap(overlay) //要模糊的图片
                        .radius(10)//模糊半径
                        .blur();

                Bitmap mBitmap = Bitmap.createBitmap(finalBitmap, 0, 0, 100, 200, null, false);

                view.setBackground(new BitmapDrawable(getResources(), finalBitmap));
                return true;
            }
        });
        metrics = new DisplayMetrics();
        WindowManager windowManager = getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        tmp = new Rect(0, 0, metrics.widthPixels, 0);
//        view.setOutlineProvider(new CustomOutlineProvider());
    }


    @SuppressLint("NewApi")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                //getGlobalVisibleRect()相对与父布局的rect
//                view.getGlobalVisibleRect(originRect);
//                int centerX = (originRect.right - originRect.left) / 2;
//                int centerY = (originRect.bottom - originRect.top) / 2;
                //设置View的显示区域，坐标是自身

                break;
            case MotionEvent.ACTION_MOVE:
                tmp.set(0, 0, metrics.widthPixels, (int) event.getY());
                view.setClipBounds(tmp);
                break;
        }
        return false;
    }

    private int dip2px(Context context, float dipValue) {
        Resources r = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dipValue, r.getDisplayMetrics());
    }

}
