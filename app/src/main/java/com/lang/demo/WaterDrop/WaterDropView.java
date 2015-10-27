package com.lang.demo.WaterDrop;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;

import com.lang.demo.R;

/**
 * Created by android on 10/27/15.
 */
public class WaterDropView extends LinearLayout {
    private final String TAG = "WaterDropViewHeader";

    private Circle topCircle;
    private Circle bottomCircle;

    private Paint mPaint;
    private Path mPath;

    private float mMaxCircleRadius;//圆半径最大值
    private float mMinCircleRaidus;//圆半径最小值

    private Bitmap arrowBitmap;//箭头

    private final static int BACK_ANIM_DURATION = 180;
    private final static float STROKE_WIDTH = 2;//边线宽度

    public WaterDropView(Context context) {
        super(context);
        init(context, null);
    }

    public WaterDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        Log.i(TAG, "init");
        topCircle = new Circle();
        bottomCircle = new Circle();
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setColor(Color.GRAY);
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        mPaint.setStrokeWidth(STROKE_WIDTH);
        Drawable drawable = getResources().getDrawable(R.drawable.refresh_arrow);
        arrowBitmap = Utils.drawableToBitmap(drawable);
        parseAttrs(context, attrs);
    }

    private void parseAttrs(Context context, AttributeSet attrs) {
        Log.i(TAG, "parseAttrs");
        if (attrs != null) {
            TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterDropView, 0, 0);
            try {
                if (a.hasValue(R.styleable.WaterDropView_waterdrop_color)) {
                    int waterDropColor = a.getColor(R.styleable.WaterDropView_waterdrop_color, Color.GRAY);
                    mPaint.setColor(waterDropColor);
                }
                if (a.hasValue(R.styleable.WaterDropView_max_circle_radius)) {
                    mMaxCircleRadius = a.getDimensionPixelSize(R.styleable.WaterDropView_max_circle_radius, 0);

                    topCircle.setRadius(mMaxCircleRadius);
                    bottomCircle.setRadius(mMaxCircleRadius);

                    topCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
                    topCircle.setY(STROKE_WIDTH + mMaxCircleRadius);

                    bottomCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
                    bottomCircle.setY(STROKE_WIDTH + mMaxCircleRadius);
                }
                if (a.hasValue(R.styleable.WaterDropView_min_circle_radius)) {
                    mMinCircleRaidus = a.getDimensionPixelSize(R.styleable.WaterDropView_min_circle_radius, 0);
                    if (mMinCircleRaidus > mMaxCircleRadius) {
                        throw new IllegalStateException("Circle's MinRaidus should be equal or lesser than the MaxRadius");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                a.recycle();
            }
        }
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        Log.i(TAG, "onDraw");
        makeBezierPath();
        canvas.drawPath(mPath, mPaint);
        //绘制顶部圆，圆半径逐渐减小
        canvas.drawCircle(topCircle.getX(), topCircle.getY(), topCircle.getRadius(), mPaint);
        //绘制底部圆，圆半径逐渐减小
        canvas.drawCircle(bottomCircle.getX(), bottomCircle.getY(), bottomCircle.getRadius(), mPaint);
        //设置顶部圆中间显示的箭头区域
        RectF bitmapArea = new RectF(topCircle.getX() - 0.5f * topCircle.getRadius(), topCircle.getY() - 0.5f * topCircle.getRadius(), topCircle.getX() + 0.5f * topCircle.getRadius(), topCircle.getY() + 0.5f * topCircle.getRadius());
        //绘制箭头
        canvas.drawBitmap(arrowBitmap, null, bitmapArea, mPaint);
        super.onDraw(canvas);
    }

    private void makeBezierPath() {
        Log.i(TAG, "makeBezierPath");

        //清屏重置
        mPath.reset();

        //获取两个圆切线与圆心连线的夹角
        double angle = getAngle();

        //获取两圆的两个切线形成的四个切点坐标左上、右上、左下、右下
        float top_x1 = (float) (topCircle.getX() - topCircle.getRadius() * Math.cos(angle));
        float top_y1 = (float) (topCircle.getY() + topCircle.getRadius() * Math.sin(angle));

        float top_x2 = (float) (topCircle.getX() + topCircle.getRadius() * Math.cos(angle));
        float top_y2 = top_y1;

        float bottom_x1 = (float) (bottomCircle.getX() - bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y1 = (float) (bottomCircle.getY() + bottomCircle.getRadius() * Math.sin(angle));

        float bottom_x2 = (float) (bottomCircle.getX() + bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y2 = bottom_y1;

        //画笔移动至上圆圆心位置
        mPath.moveTo(topCircle.getX(), topCircle.getY());

        //从上圆圆心位置绘制一条直线至左上切点
        mPath.lineTo(top_x1, top_y1);

        //从左上切点位置绘制贝塞尔曲线(控制点为两切点中间位置，最终点为左下切点)
        mPath.quadTo((bottomCircle.getX() - bottomCircle.getRadius()),
                (bottomCircle.getY() + topCircle.getY()) / 2,
                bottom_x1,
                bottom_y1);

        //从左下切点位置绘制一条直线到右下切点处
        mPath.lineTo(bottom_x2, bottom_y2);

        //从右下切点开始绘制贝塞尔曲线(控制点在右下与右上切点中间，最终点为右上切点)
        mPath.quadTo((bottomCircle.getX() + bottomCircle.getRadius()),
                (bottomCircle.getY() + top_y2) / 2,
                top_x2,
                top_y2);

        mPath.close();
    }

    /**
     * 获得两个圆切线与圆心连线的夹角
     *
     * @return
     */
    private double getAngle() {
        Log.i(TAG, "getAngle");
        if (bottomCircle.getRadius() > topCircle.getRadius()) {
            throw new IllegalStateException("bottomCircle's radius must be less than the topCircle's");
        }
        return Math.asin((topCircle.getRadius() - bottomCircle.getRadius()) / (bottomCircle.getY() - topCircle.getY()));
    }

    /**
     * 完成的百分比
     *
     * @param percent between[0,1]
     */
    public void updateComleteState(float percent) {
        if (percent < 0 || percent > 1) {
            throw new IllegalStateException("completion percent should between 0 and 1!");
        }
        float top_r = (float) (mMaxCircleRadius - 0.25 * percent * mMaxCircleRadius);
        float bottom_r = (mMinCircleRaidus - mMaxCircleRadius) * percent + mMaxCircleRadius;
        float bottomCricleOffset = 2 * percent * mMaxCircleRadius;
        topCircle.setRadius(top_r);
        bottomCircle.setRadius(bottom_r);
        bottomCircle.setY(topCircle.getY() + bottomCricleOffset);
        requestLayout();
        postInvalidate();
    }
}
