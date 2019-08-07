package com.android.touchanimatortest;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Custom view used to render an horizontal part (slice) of an image.
 *
 * @author Nicolas POMEPUY
 *
 */
class ScrollableImageView extends View {

    // A bitmap adapted to the View size
    private Bitmap adaptedImage;
    // A Paint object used to render the image
    private Paint paint = new Paint();
    // The original Bitmap
    private Bitmap originalImage;
    // The screen width used to render the image
    private int scrollY = 0;//初始纵轴偏移，应该为正
    private int scrollX = 0;//初始横轴偏移，应该为正

    public ScrollableImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ScrollableImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollableImageView(Context context) {
        this(context, null);
    }

    /**
     * Draws the view if the adapted image is not null
     */
    @Override
    protected void onDraw(Canvas canvas) {

        if (adaptedImage != null)
            canvas.drawBitmap(adaptedImage, 0, 0, paint);

    }

    /**
     * Handle an external scroll and render the image by switching it by a
     * distance
     * 最核心的方法
     * 传入的应为正值,为向下和向右的偏移量
     * @param distY
     *            the distance from the top
     */
    public void handleScroll(float distY,float distX) {
        if (getHeight() > 0 && getWidth()>0 && originalImage != null) {
            Log.i("Alex", "scrolly是"+scrollY+"  distY是"+distY+"  origi高是"+originalImage.getHeight()+"  自高是"+getHeight());
            Log.i("Alex", "scrolly是"+scrollX+"  distX是"+distX+"  origi宽是"+originalImage.getWidth()+"  自宽是"+getWidth());
            //distY为正值，正值向下滑
            //scrollY一直是0或负值
            //getHeight是自高
            if (Math.abs(scrollY+distY) <= originalImage.getHeight() - getHeight() && Math.abs(scrollX+distX) <= originalImage.getWidth() - getWidth()) {
                adaptedImage = Bitmap.createBitmap(originalImage, (int)distX+scrollX, (int) distY+scrollY, getWidth(), getHeight());

                invalidate();
            }
        }

    }

    public void setoriginalImage(Bitmap bmp) {
        this.originalImage = bmp;
        adaptedImage = Bitmap.createBitmap(bmp);
        invalidate();
    }


}
