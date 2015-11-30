package com.dlut.wanghao.circlemenu;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ListAdapter;


/**
 * Created by wanghao on 2015/11/25.
 */
public class CircleMenuGroup extends ViewGroup {

    private ListAdapter mAdapter;

    private View centerView;//中央的视图

    private int mRadius;
    /**
     * 该容器内child item的默认尺寸
     */
    private static float radioChildDimen;
    /**
     * 菜单的中心child的默认尺寸
     */
    private float radioCenterDimen;
    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private static float radioPadding;

    /**
     * 如果移动角度达到该值，则屏蔽点击
     */
    private static final int NOCLICK_VALUE = 3;

    /**
     * 该容器的内边距,无视padding属性，如需边距请用该变量
     */
    private float mPadding;
    /**
     * 布局时的开始角度
     */
    private double mStartAngle = 0;

    /**
     * 检测按下到抬起时旋转的角度
     */
    private float mTmpAngle;
    /**
     * 检测按下到抬起时使用的时间
     */
    private long mDownTime;

    /**
     * 判断是否正在自动滚动
     */
    private boolean isFling;

    private VelocityTracker velocityTracker;//速度追踪

    private double mCurrentVelocity;//记录滑动的速度

    private int cl ;//中心按钮的左边或上边离边界的距离

    private int cr ;//中心按钮的右边或下边离边界的距离

    private boolean isCenterMove;//判断是否对中央施加滑动

    public CircleMenuGroup(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray typedArray = context.obtainStyledAttributes(attrs,R.styleable.CircleMenu);
        radioChildDimen = typedArray.getFloat(R.styleable.CircleMenu_radioChildDimen, 1/4f);
        radioCenterDimen = typedArray.getFloat(R.styleable.CircleMenu_radioCenterDimen, 1/3f);
        radioPadding = typedArray.getFloat(R.styleable.CircleMenu_radioPadding, 1/12f);
        typedArray.recycle();
        // 无视padding
        setPadding(0, 0, 0, 0);
    }

    public void setAdapter(ListAdapter mAdapter){
        this.mAdapter = mAdapter;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        if (mAdapter!=null) buildMenuItems();
        if (centerView!=null) addView(centerView);
    }

    /**
     * 构建菜单项
     */
    private void buildMenuItems() {
        for (int i=0;i<mAdapter.getCount();i++){
            final View itemView = mAdapter.getView(i,null,this);
            final int position = i;
            itemView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMenuItemClickListener!=null) mOnMenuItemClickListener.itemClick(v, position);
                }
            });
            addView(itemView);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        //测量自身尺寸
        measureMyself(widthMeasureSpec, heightMeasureSpec);
        //测试子视图尺寸
        measureChild();

        mPadding = radioPadding * mRadius;
    }

    private void measureMyself(int widthMeasureSpec, int heightMeasureSpec) {

        int resWidth, resHeight;

        /**
         * 根据传入的参数，分别获取测量模式和测量值
         */
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);

        int height = MeasureSpec.getSize(heightMeasureSpec);
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);

        /**
         * 如果宽或者高的测量模式非精确值
         */
        if (widthMode != MeasureSpec.EXACTLY
                || heightMode != MeasureSpec.EXACTLY)
        {
            // 主要设置为背景图的高度
            resWidth = getSuggestedMinimumWidth();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resWidth = resWidth == 0 ? getDefaultWidth() : resWidth;

            resHeight = getSuggestedMinimumHeight();
            // 如果未设置背景图片，则设置为屏幕宽高的默认值
            resHeight = resHeight == 0 ? getDefaultWidth() : resHeight;
        } else
        {
            // 如果都设置为精确值，则直接取小值；
            resWidth = resHeight = Math.min(width, height);
        }

        setMeasuredDimension(resWidth, resHeight);
    }

    private void measureChild() {
        // 获得半径
        mRadius = Math.max(getMeasuredWidth(), getMeasuredHeight());

        // menu item数量
        final int count = getChildCount();
        // menu item尺寸
        int childSize = (int) (mRadius * radioChildDimen);
        // menu item测量模式
        int childMode = MeasureSpec.EXACTLY;

        // 迭代测量
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE) {
                continue;
            }

            // 计算menu item的尺寸；以及和设置好的模式，去对item进行测量
            int makeMeasureSpec = -1;

            if (child == centerView) {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(
                        (int) (mRadius * radioCenterDimen),
                        childMode);
            } else {
                makeMeasureSpec = MeasureSpec.makeMeasureSpec(childSize,
                        childMode);
            }
            child.measure(makeMeasureSpec, makeMeasureSpec);
        }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {

        int layoutRadius = mRadius;

        int left, top;
        // menu item 的尺寸
        int cWidth = (int) (layoutRadius * radioChildDimen);

        // 根据menu item的个数，计算角度
        float angleDelay = 360/mAdapter.getCount();

        // 遍历去设置menuitem的位置
        for (int i = 0; i < mAdapter.getCount(); i++)
        {
            final View child = getChildAt(i);

            if (child.getVisibility() == GONE)
            {
                continue;
            }

            mStartAngle %= 360;

            // 计算，中心点到menu item中心的距离
            float tmp = layoutRadius / 2f - cWidth / 2 - mPadding;

            // tmp cosa 即menu item中心点的横坐标
            left = layoutRadius/2 + (int) Math.round(tmp
                    * Math.cos(Math.toRadians(mStartAngle)) - 1 / 2f
                    * cWidth);
            // tmp sina 即menu item的纵坐标
            top = layoutRadius/2 + (int) Math.round(tmp
                    * Math.sin(Math.toRadians(mStartAngle)) - 1 / 2f
                    * cWidth);

            child.layout(left, top, left + cWidth, top + cWidth);
            // 叠加尺寸
            mStartAngle += angleDelay;
        }

        if (centerView != null)
        {
            // 设置center item位置
            cl = layoutRadius / 2 - centerView.getMeasuredWidth() / 2;
            cr = cl + centerView.getMeasuredWidth();
            centerView.layout(cl, cl, cr, cr);
        }

    }

    /**
     * 记录上一次的x，y坐标
     */
    private float mLastX;
    private float mLastY;

    /**
     * 自动滚动的Runnable
     */
    private AutoFlingRunnable mFlingRunnable;

    @Override
    public boolean dispatchTouchEvent(MotionEvent event)
    {
        float x = event.getX();
        float y = event.getY();
        boolean isInCenter = centerView!=null ? cl<(int)x && (int)x<cr && cl<y && y<cr : false;

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN:
                velocityTracker = VelocityTracker.obtain();
                isCenterMove = false;
                mLastX = x;
                mLastY = y;
                mDownTime = System.currentTimeMillis();
                mTmpAngle = 0;

                // 如果当前已经在快速滚动
                if (isFling)
                {
                    // 移除快速滚动的回调，立刻停止
                    removeCallbacks(mFlingRunnable);
                    isFling = false;
                    //如果是不是点击中央不响应 ，否则点击中央则会响应
                    if (!isInCenter) {
                        return true;
                    }
                }

                break;
            case MotionEvent.ACTION_MOVE:

                /**
                 * 获得开始的角度
                 */
                float start = (float)(180/Math.PI*Math.atan2((double)(mLastY-mRadius/2f),(double)(mLastX-mRadius/2f)));

                /**
                 * 获得当前的角度
                 */
                float end = (float)(180/Math.PI*Math.atan2((double)(y-mRadius/2f),(double)(x-mRadius/2f)));

                int factor = 0;
                //因为第2、3象限的-180度和180度是重合的，所以需要进行处理
                if (getQuadrant(mLastX,mLastY)==3 && getQuadrant(x,y)==2) {
                    factor = 1;
                }else if(getQuadrant(mLastX,mLastY)==2 && getQuadrant(x,y)==3){
                    factor = -1;
                }
                mStartAngle += end - start + factor*360;
                mTmpAngle += end - start + factor*360;
                //判断一定时间内的差值，判断是加速还是减速，还是靠velocitytracker的sqrt
                velocityTracker.addMovement(event);
                velocityTracker.computeCurrentVelocity(100);
                mCurrentVelocity = Math.hypot(velocityTracker.getXVelocity(), velocityTracker.getYVelocity());

                // 如果当前旋转角度超过NOCLICK_VALUE，则判定为界面滑动
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE){
                    //在中心按钮上滑动时，屏蔽滑动
                    if (isInCenter) {
                        isCenterMove = true;
                        return false;
                    }
                    // 重新布局
                    requestLayout();
                }

                mLastX = x;
                mLastY = y;
                break;
            case MotionEvent.ACTION_UP:

                velocityTracker.recycle();
                // 计算，每秒移动的角度
                float anglePerSecond = mTmpAngle * 1000
                        / (System.currentTimeMillis() - mDownTime);
//			Log.e("TAG", "CurrentVelocity = " + mCurrentVelocity+"  anglePerSecond = "+Math.abs(anglePerSecond));
                //防止滑动速度过快，用户体验更好
                int mark = (int) (anglePerSecond/Math.abs(anglePerSecond));
                anglePerSecond = Math.abs(anglePerSecond)>900 ? 900*mark : anglePerSecond;
                //最后滑动的速度，大于一定值判断为快速滑动
                if (mCurrentVelocity>50 && !isFling) {
                    // post一个任务，去自动滚动
                    post(mFlingRunnable = new AutoFlingRunnable(anglePerSecond));
                }

                // 如果当前旋转角度超过NOCLICK_VALUE，则屏蔽点击事件
                if (Math.abs(mTmpAngle) > NOCLICK_VALUE) return true;

                break;
        }
        return super.dispatchTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (isCenterMove && ev.getAction()==MotionEvent.ACTION_UP) {
            return true;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    /**
     * 根据当前位置计算象限
     *
     * @param x
     * @param y
     * @return
     */
    private int getQuadrant(float x, float y)
    {
        int tmpX = (int) (x - mRadius / 2);
        int tmpY = (int) (y - mRadius / 2);
        if (tmpX >= 0)
        {
            return tmpY >= 0 ? 4 : 1;
        } else {
            return tmpY >= 0 ? 3 : 2;
        }

    }

    /**
     * 获得默认该layout的尺寸
     *
     * @return
     */
    private int getDefaultWidth()
    {
        WindowManager wm = (WindowManager) getContext().getSystemService(
                Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(outMetrics);
        return Math.min(outMetrics.widthPixels, outMetrics.heightPixels);
    }

    /**
     * 自动滚动的任务
     *
     *
     */
    private class AutoFlingRunnable implements Runnable
    {

        private float angelPerSecond;

        public AutoFlingRunnable(float velocity)
        {
            this.angelPerSecond = velocity;
        }

        public void run()
        {
            // 如果小于20,则停止
            if ((int) Math.abs(angelPerSecond) < 20)
            {
                isFling = false;
                return;
            }
            isFling = true;
            // 不断改变mStartAngle，让其滚动，/30为了避免滚动太快
            mStartAngle += (angelPerSecond / 30);
            // 逐渐减小这个值
            angelPerSecond /= 1.1F;
            postDelayed(this, 30);
            // 重新布局
            requestLayout();
        }
    }

    /**
     * MenuItem的点击事件接口
     *
     *
     */
    public interface OnMenuItemClickListener
    {
        void itemClick(View view, int pos);

    }

    /**
     * MenuItem的点击事件接口
     */
    private OnMenuItemClickListener mOnMenuItemClickListener;

    /**
     * 设置MenuItem的点击事件接口
     *
     * @param mOnMenuItemClickListener
     */
    public void setOnMenuItemClickListener(
            OnMenuItemClickListener mOnMenuItemClickListener)
    {
        this.mOnMenuItemClickListener = mOnMenuItemClickListener;
    }

    /**
     * 设置中央的视图
     *
     * @param v
     */
    public void setCenterView(View v){
        this.centerView = v;
    }
}
