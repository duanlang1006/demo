package com.lang.demo.Bezier;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.util.Random;

/**
 * Created by lang on 10/23/15.
 */
public class AutoBezier extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    private SurfaceHolder mSurfaceHolder;
    private Thread mThread;
    private boolean flag;
    private Canvas mCanvas;
    public static int screenW, screenH;

    // 贝赛尔曲线成员变量(起始点，控制（操作点），终止点，3点坐标)
    private int startX, startY, controlX, controlY, endX, endY;
    // Path
    private Path mPath;
    // 绘制贝赛尔曲线的画笔
    private Paint mPaint;
    // 随机库（让贝赛尔曲线更明显）
    private Random random;

    /**
     * SurfaceView初始化函数
     */
    public AutoBezier(Context context) {
        super(context);
        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        setFocusable(true);

        //贝赛尔曲线相关初始化
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setAntiAlias(true);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(5);
        mPaint.setColor(Color.WHITE);
        random = new Random();
    }

    /**
     * SurfaceView视图创建，响应此函数
     */
    public void surfaceCreated(SurfaceHolder holder) {
        screenW = this.getWidth();
        screenH = this.getHeight();
        flag = true;
        // 实例线程
        mThread = new Thread(this);
        // 启动线程
        mThread.start();
        // -----------以上是SurfaceView游戏框架
    }

    //SurfaceView绘制
    public void myDraw() {
        try {
            mCanvas = mSurfaceHolder.lockCanvas();
            if (mCanvas != null) {
                mCanvas.drawColor(Color.BLACK);
                drawPath(mCanvas);
            }
        } catch (Exception e) {
            // TODO: handle exception
        } finally {
            if (mCanvas != null)
                mSurfaceHolder.unlockCanvasAndPost(mCanvas);
        }
    }

    /**
     * 绘制贝赛尔曲线
     *
     * @param canvas 主画布
     */
    public void drawPath(Canvas canvas) {
        mPath.reset();// 重置path

        // 贝赛尔曲线的起始点
        mPath.moveTo(startX, startY);

        // 设置贝赛尔曲线的操作点以及终止点
        mPath.quadTo(controlX, controlY, endX, endY);

        // 绘制贝赛尔曲线（Path）
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 触屏事件监听
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        endX = (int) event.getX();
        endY = (int) event.getY();
        return true;
    }

    //控制点随机取得
    private void logic() {
        if (endX != 0 && endY != 0) {
            // 设置操作点为线段x， y的一半
            controlX = random.nextInt((endX - startX) / 2); //0~(endX - startX) / 2区间的随机数
            controlY = random.nextInt((endY - startY) / 2); //0~(endX - startX) / 2区间的随机数
        }
    }

    /**
     * 按键事件监听
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return super.onKeyDown(keyCode, event);
    }

    public void run() {
        while (flag) {
            long start = System.currentTimeMillis();
            myDraw();
            logic();
            long end = System.currentTimeMillis();
            try {
                if (end - start < 50) {
                    Thread.sleep(50 - (end - start));
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * SurfaceView视图状态发生改变，响应此函数
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    /**
     * SurfaceView视图消亡时，响应此函数
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;
    }

}