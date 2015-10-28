package com.lang.demo.WaterDrop;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lang.demo.R;

/**
 * Created by android on 10/28/15.
 */
public class HeaderView extends FrameLayout {
    private final String TAG = "duanlang";

    private Context mContext;
    private LinearLayout mContainer;
    private ProgressBar mProgressBar;
    private WaterDropView mWaterDropView;


    private int stretchHeight;
    private int readyHeight;
    private static final int DISTANCE_BETWEEN_STRETCH_READY = 250;

    private IStateChangedListener mStateChangedListener;
    private WaterDropListView stateChangedListener;
    private int visiableHeight;

    private STATE mState = STATE.normal;

    public enum STATE {
        normal,//正常
        stretch,//准备进行拉伸
        ready,//拉伸到最大位置
        refreshing,//刷新
        end//刷新结束，回滚
    }


    public HeaderView(Context context) {
        super(context);
        initWithContext(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mContext = context;
        mContainer = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.header, null);
        mProgressBar = (ProgressBar) mContainer.findViewById(R.id.header_progressbar);
        mWaterDropView = (WaterDropView) mContainer.findViewById(R.id.waterdrop);

        LayoutParams layoutParams = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        addView(mContainer, layoutParams);
        initHeight();
    }

    private void initHeight() {
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stretchHeight = mWaterDropView.getHeight();
                readyHeight = stretchHeight + DISTANCE_BETWEEN_STRETCH_READY;
                getViewTreeObserver().removeOnGlobalLayoutListener(this);
            }
        });
    }

    public void updateState(STATE state) {
        if (mState == state) return;
        STATE oldstate = mState;
        mState = state;
        if (null != mStateChangedListener) {
            mStateChangedListener.notifyStateChanged(oldstate, mState);
        }
        switch (mState) {
            case normal:
                handleStateNormal();
                break;
            case stretch:
                handleStateStretch();
                break;
            case ready:
                handleStateReady();
                break;
            case refreshing:
                handleStateRefreshing();
                break;
            case end:
                handleStateEnd();
                break;
            default:
                break;
        }
    }

    private void handleStateNormal() {
        mWaterDropView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
    }

    private void handleStateStretch() {
        mWaterDropView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        mContainer.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL);
    }

    private void handleStateReady() {
        mWaterDropView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.GONE);
        Animator shrinkAnimator = mWaterDropView.createAnimator();
        shrinkAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                //回弹结束后即进入refreshing状态
                updateState(STATE.refreshing);
            }
        });
        shrinkAnimator.start();//开始回弹
    }

    private void handleStateRefreshing() {
        mWaterDropView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void handleStateEnd() {
        mWaterDropView.setVisibility(View.GONE);
        mProgressBar.setVisibility(View.GONE);
    }


    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams) mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);
        //通知水滴进行更新
        if (mState == STATE.stretch) {
            float pullOffset = (float) Utils.mapValueFromRangeToRange(height, stretchHeight, readyHeight, 0, 1);
            if (pullOffset < 0 || pullOffset > 1) {
                throw new IllegalArgumentException("pullOffset should between 0 and 1!" + mState + " " + height);
            }
            Log.e("pullOffset", "pullOffset:" + pullOffset);
            mWaterDropView.updateComleteState(pullOffset);
        }

    }

    public int getVisiableHeight() {
        return mContainer.getHeight();
    }

    public int getStretchHeight() {
        return stretchHeight;
    }

    public int getReadyHeight() {
        return readyHeight;
    }

    public STATE getCurrentState() {
        return mState;
    }

    public void setStateChangedListener(IStateChangedListener l) {
        mStateChangedListener = l;
    }

    public interface IStateChangedListener {
        public void notifyStateChanged(STATE oldState, STATE newState);
    }
}
