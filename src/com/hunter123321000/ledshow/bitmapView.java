package com.hunter123321000.ledshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PaintFlagsDrawFilter;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class bitmapView extends SurfaceView implements Callback, Runnable {
    private Paint paint;// 画布
    private SurfaceHolder sfh; // 用于控制SurfaceView
    private Canvas canvas;// 画布
    private boolean flag;// 关闭线程标志
    private Thread th;// 新建线程
    private Bitmap bmp;// 位图

    // 设置画布绘图无锯齿
    private PaintFlagsDrawFilter pfd = new PaintFlagsDrawFilter(0,
            Paint.ANTI_ALIAS_FLAG);

    public bitmapView(Context context) {
        super(context);
        sfh = this.getHolder();// 实例SurfaceHolder
        sfh.addCallback(this);// 为SurfaceView添加状态监听
        paint = new Paint();// 实例一个画笔
        paint.setColor(Color.WHITE);// 设置画笔颜色为白色
        setFocusable(true);// 设置焦点
        bmp = BitmapFactory.decodeResource(getResources(), R.drawable.r1);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        flag = true;
        // 实例线程
        th = new Thread(this);
        // 启动线程
        th.start();
    }

    public void mydraw() {
        try {
            canvas = sfh.lockCanvas();// 锁定画布
            if (canvas != null) {
                canvas.drawColor(Color.BLACK);

                // ----------旋转位图(方式1)
                canvas.save();
                canvas.rotate(30, bmp.getWidth() / 2, bmp.getHeight() / 2);// 旋转弧度，旋转中心点x,旋转中心点y
                canvas.drawBitmap(bmp, bmp.getWidth() / 2 + 20,bmp.getHeight() + 10, paint);
                canvas.restore();

                // ----------平移位图(方式1)
                canvas.save();
                canvas.translate(100, 0);// 平移坐标X,Y
                canvas.drawBitmap(bmp, 0, 2 * bmp.getHeight() + 10, paint);
                canvas.restore();

                // ----------缩放位图(方式1)
                canvas.save();
                canvas.scale(2f, 2f, 50 + bmp.getWidth() / 2,50 + bmp.getHeight() / 2);// X方向缩放比例，Y方向缩放比例，缩放中心X,缩放中心Y
                canvas.drawBitmap(bmp, 150, 3 * bmp.getHeight() + 10, paint);
                canvas.restore();

                // ----------镜像反转位图(方式1)
                // X轴镜像
                canvas.save();
                canvas.scale(-1, 1, 100 + bmp.getWidth() / 2,100 + bmp.getHeight() / 2);// scale（）当第一个参数为负数时表示x方向镜像反转，同理第二参数，镜像反转x,镜像反转Y
                canvas.drawBitmap(bmp, 0, 4 * bmp.getHeight() + 10, paint);
                canvas.restore();
                // Y轴镜像
                canvas.save();
                canvas.scale(1, -1, 100 + bmp.getWidth() / 2,100 + bmp.getHeight() / 2);
                canvas.drawBitmap(bmp, 4 * bmp.getHeight(),bmp.getWidth() + 10, paint);
                canvas.restore();

                // ----------旋转位图(方式2)--通过矩阵旋转
                Matrix mx = new Matrix();
                mx.postRotate(60, bmp.getWidth() / 2, bmp.getHeight() / 2);// 旋转弧度，旋转中心点x,旋转中心点y
                canvas.drawBitmap(bmp, mx, paint);

                // ----------平移位图(方式2)--通过矩阵
                Matrix maT = new Matrix();
                maT.postTranslate(2 * bmp.getWidth(), 0);// 平移坐标X,Y
                canvas.drawBitmap(bmp, maT, paint);

                // ----------镜像反转位图(方式2)
                // X轴镜像
                Matrix maMiX = new Matrix();
                maMiX.postTranslate(0, 2 * bmp.getHeight());
                maMiX.postScale(-1, 1, 100 + bmp.getWidth() / 2,100 + bmp.getHeight() / 2);
                canvas.drawBitmap(bmp, maMiX, paint);

                // Y轴镜像
                Matrix maMiY = new Matrix();
                maMiY.postScale(1, -1, 100 + bmp.getWidth() / 2,100 + bmp.getHeight() / 2);
                canvas.drawBitmap(bmp, maMiY, paint);

                // ----------缩放位图(方式2)-放大
                Matrix maS = new Matrix();
                maS.postTranslate(200, 2 * bmp.getHeight());
                maS.postScale(2f, 2f, 50 + bmp.getWidth() / 2,50 + bmp.getHeight() / 2);// X方向缩放比例，Y方向缩放比例，缩放中心X,缩放中心Y
                canvas.drawBitmap(bmp, maS, paint);
                // ----------缩放位图(方式2)-缩小
                Matrix maS1 = new Matrix();
                maS1.postTranslate(0, 2 * bmp.getHeight());
                maS1.postScale(0.5f, 0.5f, 50 + bmp.getWidth() / 2,50 + bmp.getHeight() / 2);// X方向缩放比例，Y方向缩放比例，缩放中心X,缩放中心Y
                canvas.drawBitmap(bmp, maS1, paint);
            } else {
                Log.i("tt", "获取不到画布");// 释放画布
            }
        } catch (Exception e) {

        } finally {
            if (canvas != null)
                sfh.unlockCanvasAndPost(canvas);
        }
    }

    /**
     * SurfaceView视图状态发生改变，响应此函数
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub

    }

    /**
     * SurfaceView视图消亡时，响应此函数
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        flag = false;// 结束游戏，设置线程关闭标志为false

    }

    public void logic() {

    };

    @Override
    public void run() {
        while (flag) {
            long start = System.currentTimeMillis();
            mydraw();
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

    // 获取点击坐标
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

}
