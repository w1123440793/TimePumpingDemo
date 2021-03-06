package com.jusfoun.timepumpingdemo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.media.ThumbnailUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Description 底部滚动view
 */
public class CustomeScrollView extends View {
    private Context context;
    private float currentX = 0, moveX;
    private float downX;
    private Paint paint, graduationPaint, textPaint;
    private int totalLength;

    private int width;
    private int screenHeight, screenWidth;
    private List<RectF> points;
    private int radius;
    private int clickCount = -1;

    private Bitmap bitmap;
    private int lineCount;//长度
    private int mMaximumVelocity;
    private VelocityTracker mVelocityTracker;

    private Rect totalRect;

    public CustomeScrollView(Context context) {
        super(context);
        initView(context);
    }

    public CustomeScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public CustomeScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView(context);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {

        super.onSizeChanged(w, h, oldw, oldh);
        screenWidth = w;
        screenHeight = h;
    }

    private void initView(Context context) {
        this.context = context;
        width = Utils.dip2px(context, 80);
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setAntiAlias(true);

        graduationPaint = new Paint();
        graduationPaint.setColor(Color.WHITE);
        graduationPaint.setAntiAlias(true);
        graduationPaint.setStrokeWidth(1);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setAntiAlias(true);
        textPaint.setTextSize(30);

        points = new ArrayList<>();

        radius = Utils.dip2px(context, 5);

        bitmap = BitmapFactory.decodeResource(context.getResources(), R.mipmap.img_biaoji);
        mMaximumVelocity = ViewConfiguration.get(context).getScaledMaximumFlingVelocity();
        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
    }


    public void refresh(int count) {
        lineCount = width * count;
        int index = points.size();
        for (int i = index; i < index + count; i++) {
            RectF rect = new RectF();
            if (i == 0)
                rect.set(width, 0, width, Utils.dip2px(context, 50));
            else {
                rect.set(points.get(i - 1).right + width, 0, points.get(i - 1).right + width, Utils.dip2px(context, 50));
            }
            points.add(rect);
        }
        totalLength = (index + count) * width + screenWidth;
    }

    public int getClickCount(int x, int y) {
        for (int i = 0; i < points.size(); i++) {
            RectF rect = points.get(i);
            if (rect.left <= x
                    && rect.right >= x
                    && rect.top <= y
                    && rect.bottom >= y) {
                return i;
            }
        }
        return -1;
    }

    private boolean isMove = false;
    private boolean isDoublePoint = false;

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        mVelocityTracker.computeCurrentVelocity(1000, mMaximumVelocity);
        mVelocityTracker.addMovement(event);
        if (event.getPointerCount() >= 2) {
            isDoublePoint = true;
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                moveX = 0;
                isMove = false;
                isDoublePoint = false;
                break;
            case MotionEvent.ACTION_MOVE:
                if (isDoublePoint)
                    break;
                /*************超出后禁止滑动***************/
                /**
                 * 第一个点向左滑出屏幕或者最后一个点向右滑出屏幕禁止滑动
                 */

                if (event.getX() - downX > 0) {
                    if (currentX + event.getX() - downX > 0) {

                        moveX = 0 - currentX;
                        pointMove(moveX, true);
                        postInvalidate();
                        currentX = 0;
                        downX=event.getX();
                        return true;
                    }
                } else if (event.getX() - downX < 0) {
                    if (downX - currentX - event.getX() > totalLength) {
                        moveX = -(currentX+totalLength);
                        pointMove(moveX,true);
                        currentX=-totalLength;
                        downX=event.getX();
                        return true;
                    }
                }
                /*************超出后禁止滑动***************/
                moveX = event.getX() - downX;
                pointMove(moveX, true);
                downX = event.getX();
                break;
            case MotionEvent.ACTION_UP:
                if (isMove) {
                    pointUp(currentX, moveX, true);
                }
                clickCount = getClickCount((int) (event.getX()), (int) event.getY());
                if (clickCount != -1 && !isMove) {
                    Toast.makeText(context, "第" + clickCount + "个点", Toast.LENGTH_SHORT).show();
                    //点击跳转
                    if (listener != null) {
//                        listener.touchMove(moveX-clickCount*Utils.dip2px(context,60));
//                        listener.touchUp(currentX,moveX-clickCount*Utils.dip2px(context,60));
                    }
                }

                // TODO 暂时屏蔽惯性
//                if (Math.abs(moveX) > 50 && Math.abs(mVelocityTracker.getYVelocity()) > 1000) {
////                    handler.sendEmptyMessage(100);
//                    if (moveX < 0) {
//                        inertiaAnimation(true);
//                    } else {
//                        inertiaAnimation(false);
//                    }
//                } else {
//                moveX=event.getX()-downX;
//                }
                break;
        }
        return true;
    }

//    private float lastV=0;
//    @Override
//    public void computeScroll() {
//        super.computeScroll();
//        if (Math.abs(velocity)<50)
//            return;
//        pointMove(velocity/50-lastV,false);
//        lastV=velocity/50;
//        velocity*=0.9f;
//    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.save();
        canvas.drawColor(Color.parseColor("#00000000"));
        for (int i = 0; i < points.size(); i++) {
            RectF rect = points.get(i);
            canvas.drawBitmap(bitmap, rect.left, screenHeight / 2, paint);
        }
        for (int i = 0; i < (lineCount + screenWidth) / Utils.dip2px(context, 5); i++) {

            float startX = Utils.dip2px(context, 5) * (i + 1);
            float startY = screenHeight - Utils.dip2px(context, 7);
            if (i % 5 == 0) {
                startY = screenHeight - Utils.dip2px(context, 14);
            }
            canvas.drawLine(startX + currentX, startY, startX + currentX, screenHeight, graduationPaint);

        }
        for (int i = 4; i <= 6; i++) {
            canvas.drawText("201" + i, 7 * (i - 3) * width + currentX, 40, textPaint);
        }
        canvas.restore();
    }

    private int velocity = 0;

    public void pointUp(float currentX, float moveX, boolean isHasListener) {

        if (listener != null && isHasListener)
            listener.touchUp(currentX, moveX);
        velocity = (int) mVelocityTracker.getXVelocity()/100;
    }

    public void pointMove(float moveX, boolean isHasListener) {
//        if (Math.abs(moveX) > 10) {
        isMove = true;
        currentX += moveX;
        for (int i = 0; i < points.size(); i++) {
            RectF rect = points.get(i);
            rect.left += moveX;
            rect.right += moveX;
        }
        postInvalidate();
        if (listener != null && isHasListener) {
            listener.touchMove(moveX);
        }
//        }
    }

    private OnScrollTouchListener listener;

    public void setListener(OnScrollTouchListener listener) {
        this.listener = listener;
    }

    public interface OnScrollTouchListener {
        void touchUp(float currentX, float moveX);

        void touchMove(float moveX);

        void onClick(int clickCount);
    }

    protected void inertiaAnimation(final boolean isUp) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 30f);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {

                if (!isUp) {
                    pointMove(moveX + (float) animation.getAnimatedValue(), true);
                    moveX = moveX + (float) animation.getAnimatedValue();
                } else {
                    pointMove(moveX - (float) animation.getAnimatedValue(), true);
                    moveX = moveX - (float) animation.getAnimatedValue();
                }
            }

        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                pointUp(currentX, moveX, true);
            }
        });
        animator.setInterpolator(new DecelerateInterpolator());
        animator.setDuration(400);
        animator.start();
    }
}
