package com.lang.demo.WaterDrop;

import android.animation.Animator;
import android.animation.ValueAnimator;
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
import android.view.View;
import android.view.animation.DecelerateInterpolator;

import com.lang.demo.R;

/**
 * Created by android on 10/28/15.
 */
public class WaterDropView extends View {
    private final String TAG = "duanlang";

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
        initWithContext(context, null);
    }

    public WaterDropView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context, attrs);
    }

    public WaterDropView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWithContext(context, attrs);
    }

    private void initWithContext(Context context, AttributeSet attrs) {
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

    private void parseAttrs(Context context, AttributeSet attributeSet) {
        if (null != attributeSet) {
            TypedArray typedArray = context.obtainStyledAttributes(attributeSet, R.styleable.WaterDropView, 0, 0);
            try {
                if (typedArray.hasValue(R.styleable.WaterDropView_waterdrop_color)) {
                    int waterDropColor = typedArray.getColor(R.styleable.WaterDropView_waterdrop_color, Color.GRAY);
                    mPaint.setColor(waterDropColor);        // mPaint.setColor(Color.GRAY);   ????
                }
                if (typedArray.hasValue(R.styleable.WaterDropView_max_circle_radius)) {
                    mMaxCircleRadius = typedArray.getDimensionPixelSize(R.styleable.WaterDropView_max_circle_radius, 0);

                    topCircle.setRadius(mMaxCircleRadius);
                    bottomCircle.setRadius(mMaxCircleRadius);

                    topCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
                    topCircle.setY(STROKE_WIDTH + mMaxCircleRadius);

                    bottomCircle.setX(STROKE_WIDTH + mMaxCircleRadius);
                    bottomCircle.setY(STROKE_WIDTH + mMaxCircleRadius);
                }
                if (typedArray.hasValue(R.styleable.WaterDropView_min_circle_radius)) {
                    mMinCircleRaidus = typedArray.getDimensionPixelSize(R.styleable.WaterDropView_min_circle_radius, 0);
                    if (mMaxCircleRadius < mMinCircleRaidus) {
                        throw new IllegalStateException("Circle's MinRaidus should be equal or lesser than the MaxRadius");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                typedArray.recycle();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param widthMeasureSpec
     * @param heightMeasureSpec
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //宽度：上圆和下圆的最大直径
        int width = (int) ((mMaxCircleRadius + STROKE_WIDTH) * 2);
        //高度：上圆半径 + 圆心距 + 下圆半径
        int height = (int) Math.ceil(bottomCircle.getY() + bottomCircle.getRadius() + STROKE_WIDTH * 2);
        Log.i(TAG, "width : " + width + "  height : " + height);
        setMeasuredDimension(width, height);
    }

    /**
     * Implement this to do your drawing.
     *
     * @param canvas the canvas on which the background will be drawn
     */
    @Override
    protected void onDraw(Canvas canvas) {
        makeBezierPath();
        canvas.drawPath(mPath, mPaint);
        Log.i(TAG, "topCircle.getRadius() = " + topCircle.getRadius() + " bottomCircle.getRadius() = " + bottomCircle.getRadius());
        canvas.drawCircle(topCircle.getX(), topCircle.getY(), topCircle.getRadius(), mPaint);
        canvas.drawCircle(bottomCircle.getX(), bottomCircle.getY(), bottomCircle.getRadius(), mPaint);
//        Matrix m = new Matrix();
//        float orientationDegree = 2f;
//        m.setRotate(orientationDegree, topCircle.getX(), topCircle.getY());
        RectF bitmapArea = new RectF(topCircle.getX() - 0.5f * topCircle.getRadius(), topCircle.getY() - 0.5f * topCircle.getRadius(), topCircle.getX() + 0.5f * topCircle.getRadius(), topCircle.getY() + 0.5f * topCircle.getRadius());
//        arrowBitmap = Bitmap.createBitmap(arrowBitmap,0,0,arrowBitmap.getWidth(),arrowBitmap.getHeight(),m,true);
        canvas.drawBitmap(arrowBitmap, null, bitmapArea, mPaint);
        super.onDraw(canvas);
    }

    private void makeBezierPath() {
        mPath.reset();
        //获得两个圆切线与圆心连线的夹角
        double angle = getAngle();

        //获取两圆的两个切线形成的四个切点
        float top_x1 = (float) (topCircle.getX() - topCircle.getRadius() * Math.cos(angle));
        float top_y1 = (float) (topCircle.getY() + topCircle.getRadius() * Math.sin(angle));

        float top_x2 = (float) (topCircle.getX() + topCircle.getRadius() * Math.cos(angle));
        float top_y2 = top_y1;

        float bottom_x1 = (float) (bottomCircle.getX() - bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y1 = (float) (bottomCircle.getY() + bottomCircle.getRadius() * Math.sin(angle));

        float bottom_x2 = (float) (bottomCircle.getX() + bottomCircle.getRadius() * Math.cos(angle));
        float bottom_y2 = bottom_y1;

        mPath.moveTo(topCircle.getX(), topCircle.getY());

        mPath.lineTo(top_x1, top_y1);

        mPath.quadTo((bottomCircle.getX() - bottomCircle.getRadius()),
                (bottomCircle.getY() + topCircle.getY()) / 2,
                bottom_x1,
                bottom_y1);
        mPath.lineTo(bottom_x2, bottom_y2);

        mPath.quadTo((bottomCircle.getX() + bottomCircle.getRadius()),
                (bottomCircle.getY() + top_y2) / 2,
                top_x2,
                top_y2);

        mPath.close();
    }

    public double getAngle() {
        return Math.asin((topCircle.getRadius() - bottomCircle.getRadius()) / (bottomCircle.getY() - topCircle.getY()));
    }

    /**
     * 创建回弹动画
     * 上圆半径减速恢复至最大半径
     * 下圆半径减速恢复至最大半径
     * 圆心距减速从最大值减到0(下圆Y从当前位置移动到上圆Y)。
     *
     * @return
     */
    public Animator createAnimator() {
        ValueAnimator valueAnimator = ValueAnimator.ofFloat(1, 0).setDuration(BACK_ANIM_DURATION);
        valueAnimator.setInterpolator(new DecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                updateComleteState((float) valueAnimator.getAnimatedValue());
            }
        });
        return valueAnimator;
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
