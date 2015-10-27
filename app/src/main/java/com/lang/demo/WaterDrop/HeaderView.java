package com.lang.demo.WaterDrop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.lang.demo.R;

/**
 * Created by android on 10/27/15.
 */
public class HeaderView extends FrameLayout {
    private final String TAG = "HeaderView";

    private Context mContext;
    private LinearLayout mContainer;

    private ProgressBar mProgressBar;
    private WaterDropView mWaterDropView;

    private STATE mState = STATE.normal;

    private int stretchHeight;      //拉伸高度
    private int readyHeight;
    private static final int DISTANCE_BETWEEN_STRETCH_READY = 250;      //拉伸距离

    public enum STATE {
        normal,//正常
        stretch,//准备进行拉伸
        ready,//拉伸到最大位置
        refreshing,//刷新
        end//刷新结束，回滚
    }

    private IStateChangedListener mStateChangedListener;
    public void setStateChangedListener(IStateChangedListener l) {
        Log.i(TAG, "setStateChangedListener");
        mStateChangedListener = l;
    }

    public interface IStateChangedListener {
        public void notifyStateChanged(STATE oldState, STATE newState);
    }

    public HeaderView(Context context) {
        super(context);
        init(context);
    }

    public HeaderView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    private void init(Context context) {
        mContext = context;
        mContainer = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.refresh, null);

        mProgressBar = (ProgressBar) mContainer.findViewById(R.id.header_progressbar);
        mWaterDropView = (WaterDropView) mContainer.findViewById(R.id.waterdrop);

        LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);
        addView(mContainer, lp);
        initHeight();
    }

    private void initHeight() {
        mContainer.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                stretchHeight = mWaterDropView.getHeight();
                readyHeight = stretchHeight + DISTANCE_BETWEEN_STRETCH_READY;
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
            }
        });
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

    public void setVisiableHeight(int height) {
        if (height < 0)
            height = 0;
        LayoutParams lp = (LayoutParams)mContainer.getLayoutParams();
        lp.height = height;
        mContainer.setLayoutParams(lp);

        //通知水滴更新
        if(mState == STATE.stretch){
            float pullOffset = (float) Utils.mapValueFromRangeToRange(height, stretchHeight, readyHeight, 0, 1);
            if (pullOffset < 0 || pullOffset > 1) {
                throw new IllegalArgumentException("pullOffset should between 0 and 1!" + mState + " " + height);
            }
            mWaterDropView.updateComleteState(pullOffset);
        }

    }
}
