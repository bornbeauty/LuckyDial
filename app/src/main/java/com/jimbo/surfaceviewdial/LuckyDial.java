package com.jimbo.surfaceviewdial;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by jimbo on 2015/9/12.
 * 转盘抽奖
 */
public class LuckyDial extends SurfaceView implements SurfaceHolder.Callback, Runnable {
    //SurfaceView的holder类
    private SurfaceHolder mHolder;

    //绘制SurfaceView的线程类
    private Thread mDrawThread;

    //控制thread的开关
    private boolean mIsThreadRunning = true;

    //padding
    private int mPadding;

    //转盘的宽度
    private int mRadius;

    //转盘的专心点
    private int mCenter;

    //转盘上的奖项文字
    private String[] mDialStrings = {"IPad",
            "恭喜发财", "妹纸一枚", "单反相机",
            "恭喜发财", "IPhone"
    };
    //转盘上的图片
    private int[] mDialImages = {R.mipmap.ipad,
            R.mipmap.smile, R.mipmap.meizi, R.mipmap.danfan,
            R.mipmap.se, R.mipmap.iphone
    };

    //与图片对应的bitmap
    private Bitmap[] mDialBitmap;

    //转盘颜色
    private int[] mDialColors = {0XFFFFc300,
            0XFFF17E01, 0XFFFFc300, 0XFFF17E01,
            0XFFFFc300, 0XFFF17E01
    };

    //背景图片 直接初始化
    private Bitmap mBGBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.bg2);

    //设置转盘的小单元的数码样
    private int mItemCount = 6;

    //设置转盘的范围 用一个矩形来限制
    RectF mRange;

    //背景图片的范围
    RectF mBGRange;

    //文字画笔
    private Paint mTextPaint;

    //设置文字的大小 http://blog.csdn.net/buleriver/article/details/9407541
    //为了统一单位
    private float mTextSize = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_SP, 20,
            getResources().getDisplayMetrics()
    );

    //图片画笔
    private Paint mImagePaint;

    //画布
    private Canvas mCanvas;

    //滚动速度
    private int mSpeed = 0;

    //起始角度
    private volatile int mStartAngle = 0;

    //是否点击了停止按钮
    private boolean mIsShouldStop = true;

    //停止的回调函数
    private Handler mStopHandler;

    //标记是否已经回调过函数
    private boolean mIsHavedSend = true;

    public LuckyDial(Context context) {
        this(context, null);
    }

    public LuckyDial(Context context, AttributeSet attrs) {
        super(context, attrs);
        //获取SurfaceView的holder
        mHolder = getHolder();
        //为holder添加会调方法
        mHolder.addCallback(this);
        //初始化线程
        mDrawThread = new Thread(this);

        setFocusable(true);
        setFocusableInTouchMode(true);
        setKeepScreenOn(true);
    }

    //设置停止转动

    /**
     * description 结束转盘转动
     * return 没有返回值
     */
    public void stop() {
        mIsShouldStop = true;
    }


    /**
     * 设置停在的位置
     * @param stopIndex 停止位置的索引 范围是0 ~ n-1
     */
    public void stop(int stopIndex) {
        int angle = 360 / mItemCount;
        int stop = 210 - stopIndex*angle;
        int random = (int)(Math.random()*60);
        while (0 == random) {
            random = (int)(Math.random()*60);
        }
        mStartAngle = 166 + stop + random;
        mSpeed = 50;
        stop();
    }

    //设置回调
    /**
     * 在转盘转动结束后返回抽到的奖品
     * @param stopHandler 回调类
     */
    public void setHandler(Handler stopHandler) {
        this.mStopHandler = stopHandler;
    }

    //是否还在转动

    /**
     * 转盘是否还在转动
     * @return 是否还在转动
     */
    public boolean isRunning() {
        return mSpeed > 0;
    }

    //是否接受到了停止的请求

    /**
     * 判断是否已经点击了停止按钮
     * @return 是否点击了停止
     */
    public boolean isStoped() {
        return mIsShouldStop;
    }

    /**
     * 开始抽奖
     */
    public void start() {
        mSpeed = 0;
        mStartAngle = 0;
        mIsShouldStop = false;
        mIsThreadRunning = true;
        mIsHavedSend = false;
    }

    /**
     * 抽中物品
     * @return 返回抽中物品的索引
     */
    public int get() {
        if (isRunning()) {
            return -1;
        } else {
            return (270 - mStartAngle) / (360 / mItemCount) + 1;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //用设置的小的作为控件的大小
        int width = Math.min(getMeasuredWidth(), getMeasuredHeight());
        //获取设置的padding
        mPadding = getPaddingLeft();
        //减去设置的padding
        mRadius = width - mPadding * 2;
        //计算出转盘的中心线
        mCenter = width / 2;

        //设置大小
        setMeasuredDimension(width, width);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        //初始化图片画笔
        mImagePaint = new Paint();
        //参考 http://blog.sina.com.cn/s/blog_783ede0301012ilk.html
        mImagePaint.setDither(true);
        mImagePaint.setAntiAlias(true);

        //初始化文字画笔
        mTextPaint = new Paint();
        mTextPaint.setColor(Color.WHITE);
        mTextPaint.setTextSize(mTextSize);

        //初始化转盘大小
        mRange = new RectF(mPadding, mPadding,
                mPadding + mRadius,
                mPadding + mRadius);

        //初始化转盘背景的范围
        mBGRange = new RectF(mPadding / 2, mPadding / 2,
                getMeasuredWidth() - mPadding / 2,
                getMeasuredWidth() - mPadding / 2);

        //初始化图片
        mDialBitmap = new Bitmap[mItemCount];
        for (int i = 0; i < mItemCount; i++) {
            mDialBitmap[i] = BitmapFactory.decodeResource(getResources(), mDialImages[i]);
        }

        //开启线程来绘制
        mDrawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsThreadRunning = false;
    }

    @Override
    public void run() {
        while (mIsThreadRunning) {
            //设置每一次绘制时间为50毫米
            int start = (int) System.currentTimeMillis();
            //绘制
            draw();
            int end = (int) System.currentTimeMillis();
            int drawTime = end - start;
            if (drawTime < 50) {
                try {
                    Thread.sleep(50 - drawTime);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //绘制
    private void draw() {
        //获取canvas
        mCanvas = mHolder.lockCanvas();
        try {
            //do someting
            //绘制背景
            drawBG();
            //绘制盘块
            drawLump();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("绘制函数出错");
        } finally {
            if (null != mCanvas) {
                mHolder.unlockCanvasAndPost(mCanvas);
            }
        }
    }

    //绘制背景的
    private void drawBG() {
        mCanvas.drawColor(getResources().getColor(android.R.color.white));
        mCanvas.drawBitmap(mBGBitmap, null, new RectF(mPadding / 2, mPadding / 2,
                getMeasuredWidth() - mPadding / 2,
                getMeasuredWidth() - mPadding / 2), null);
    }

    //绘制盘块
    private void drawLump() {
        //开始的角度
        float tempAngle = mStartAngle;
        //每一块盘子的大小
        float lumpAngle = 360 / mItemCount;
        for (int i = 0; i < mItemCount; i++) {
            //一下是画每一块的背景
            //设置颜色
            mImagePaint.setColor(mDialColors[i]);
            //在画布上绘制
            mCanvas.drawArc(mRange, tempAngle, lumpAngle, true, mImagePaint);

            //以下是画每一块的文字
            drawText(tempAngle, lumpAngle, mDialStrings[i]);

            //绘制每一个小图片
            drawIcon(tempAngle, lumpAngle, mDialBitmap[i]);

            //递增角度
            tempAngle += lumpAngle;
        }

        //改变角度 实现旋转
        mStartAngle += mSpeed;

        //模360
        mStartAngle %= 360;

        //改变旋转速度 越来越快 到一定值保持不变
        //如果点下了stop则开始减速
        changeSpeed();
    }

    private void changeSpeed() {
        if (mSpeed <= 50 && !mIsShouldStop) {
            mSpeed++;
        } else if (mIsShouldStop && 0 != mSpeed) {
            mSpeed--;
        }
        if (mIsShouldStop && 0 == mSpeed) {
            if (null != mStopHandler && !mIsHavedSend) {
                mStopHandler.sendEmptyMessage(mStartAngle - 90);
                mIsHavedSend = true;
            }
        }
    }

    private void drawIcon(float tempAngle, float lumpAngle, Bitmap bitmap) {
        //设置图片大小为直径的八分之一
        int imageWidth = mRadius / 8;
        //角度
        float angle = (float) ((tempAngle + lumpAngle / 2) * Math.PI / 180);
        //设置中心位置为距离圆心二分之一个半径的地方
        int r = mRadius / 4;
        //xy是中心店的位置
        //x
        int x = (int) (r * Math.cos(angle) + mCenter);
        //y
        int y = (int) (r * Math.sin(angle) + mCenter);
        //绘制图片
        mCanvas.drawBitmap(bitmap, null, new RectF(
                x - imageWidth / 2, y - imageWidth / 2, x + imageWidth / 2, y + imageWidth / 2
        ), mImagePaint);

    }

    /**
     * 绘制文本
     *
     * @param tempAngle   起始角度
     * @param lumpAngle   每一块的弧度
     * @param mDialString 绘制的文字
     */
    private void drawText(float tempAngle, float lumpAngle, String mDialString) {
        //文字绘制路径
        Path path = new Path();
        //设置路径
        path.addArc(mRange, tempAngle, lumpAngle);
        //文字的宽度 弧度值
        int textWidth = (int) mTextPaint.measureText(mDialString);
        //文字离水平边距 弧的长度减去文字的长度 然后 除以2
        int horOffset = (int) (mRadius * Math.PI / mItemCount / 2 - textWidth / 2);
        //文字离垂直边距
        int verOffset = mRadius / 2 / 6;
        //画文字
        mCanvas.drawTextOnPath(mDialString, path, horOffset, verOffset, mTextPaint);
    }

}
/**
 * android开发者网站上有相关的说明文档：
 * public View (Context context)是在java代码创建视图的时候被调用，如果是从xml填充的视图，就不会调用这个
 * public View (Context context, AttributeSet attrs)这个是在xml创建但是没有指定style的时候被调用
 * public View (Context context, AttributeSet attrs, int defStyle)这个不用说也懂了吧
 */