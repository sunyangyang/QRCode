package com.sunyy.qrcode.qrcode;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by sunyangyang on 2018/1/11.
 */

public class QRView extends View {
    private static final long ANIMATION_DELAY = 16L;
    private static final int POINT_SIZE = 6;
    private Context mContext;
    private RectListener mListener;
    private Paint mPaint;
    private Paint mBitmapPaint;
    private Rect mRect;
    private int mWidth;
    private int mHeight;
    private int mBackgroundColor;
    private Bitmap mBitmap;
    private Bitmap mLineBitmap;
    private int mLineWidth;
    private int mPosition = 0;
    private boolean mIsFirst = true;

    private final Xfermode SRC_IN = new PorterDuffXfermode(PorterDuff.Mode.SRC_IN);
    private final Xfermode SRC_OVER = new PorterDuffXfermode(PorterDuff.Mode.SRC_OVER);

    public QRView(Context context) {
        super(context);
        mContext = context;
    }

    public void setListener(RectListener listener) {
        mListener = listener;
    }

    public QRView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.qr);
        mBackgroundColor = array.getColor(R.styleable.qr_bg_color, 0x33000000);
        mWidth = array.getDimensionPixelSize(R.styleable.qr_width, Utils.dip2px(context, 258));
        mHeight = mWidth;
        int id = array.getResourceId(R.styleable.qr_slide_bitmap, R.mipmap.scans_box);
        mBitmap = getBitmap(id, true, true);

        int lineId = array.getResourceId(R.styleable.qr_line_bitmap, R.mipmap.scans_line);
        mLineBitmap = getBitmap(lineId, true, false);
        mLineWidth = mLineBitmap.getWidth();

        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mBitmapPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mRect = new Rect();
        array.recycle();
    }

    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        int width = canvas.getWidth();
        int height = canvas.getHeight();
        if (mRect.width() <= 0) {
            int minLength = Math.min(width, height);
            if (minLength < mWidth) {
                mWidth = minLength;
                mHeight = minLength;
            }
            int offsetX = (width - mWidth) / 2;
            int offsetY = (height - mHeight) / 2;
            mRect.set(offsetX, offsetY, offsetX + mWidth, offsetY + mHeight);
        }
        if (mIsFirst && mListener != null) {
            mIsFirst = false;
            mListener.getRect(mRect);
        }
        mPaint.setColor(mBackgroundColor);
        canvas.drawRect(0, 0, width, height, mPaint);
        canvas.drawRect(0, 0, width, mRect.top, mPaint);
        canvas.drawRect(0, mRect.top, mRect.left, mRect.bottom + 1, mPaint);
        canvas.drawRect(mRect.right + 1, mRect.top, width, mRect.bottom + 1, mPaint);
        canvas.drawRect(0, mRect.bottom + 1, width, height, mPaint);
        canvas.drawBitmap(mBitmap, mRect.left, mRect.top, mBitmapPaint);
        mPosition += 5;
        if (mRect.top + mPosition > mRect.bottom) {
            mPosition = 0;
        }
        canvas.drawBitmap(mLineBitmap, mRect.left + (mRect.width() - mLineWidth) / 2, mRect.top + mPosition, mBitmapPaint);
        postInvalidateDelayed(ANIMATION_DELAY,
                mRect.left - POINT_SIZE,
                mRect.top - POINT_SIZE,
                mRect.right + POINT_SIZE,
                mRect.bottom + POINT_SIZE);
    }

    public interface RectListener {
        void getRect(Rect rect);
    }

    public Bitmap getBitmap(int id, boolean isWidthChange, boolean isHeightChange) {
        Bitmap bitmap = ((BitmapDrawable) getResources().getDrawable(id)).getBitmap();
        int bitWidth = bitmap.getWidth();
        int bitHeight = bitmap.getHeight();
        float scaleWidth = 1.0f;
        float scaleHeight = 1.0f;
        if (isWidthChange) {
            scaleWidth = mWidth * 1.0f / bitWidth;
        }
        if (isHeightChange) {
            scaleHeight = mHeight * 1.0f / bitHeight;
        }
        if (scaleWidth == scaleHeight && scaleWidth == 1.0f) {
            return bitmap;
        } else {
            Matrix matrix = new Matrix();
            matrix.postScale(scaleWidth, scaleHeight);
            return Bitmap.createBitmap(bitmap, 0, 0 ,bitWidth, bitHeight, matrix, true);
        }
    }
}
