package com.lang.demo.WaterDrop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.Scroller;

/**
 * Created by android on 10/28/15.
 */
public class WaterDropListView extends ListView implements AbsListView.OnScrollListener, HeaderView.IStateChangedListener {
    private final String TAG = "duanlang";

    private float mLastY = -1; // save event y

    private IWaterDropListViewListener mListViewListener;
    private OnScrollListener mScrollListener; // user's scroll listener

    private Scroller mScroller; // used for scroll back

    private HeaderView mHeaderView;

    private boolean mEnablePullRefresh = true;

    private boolean isTouchingScreen = false;//手指是否触摸屏幕
    private final static int SCROLL_DURATION = 400; // scroll back duration

    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull

    public WaterDropListView(Context context) {
        super(context);
        initWithContext(context);
    }

    public WaterDropListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        // XListView need the scroll event, and it will dispatch the event to
        // user's listener (as a proxy).
        super.setOnScrollListener(this);

        // init header view
        mHeaderView = new HeaderView(context);
        mHeaderView.setStateChangedListener(this);
        addHeaderView(mHeaderView);
    }

    public void stopRefresh() {
        if (mHeaderView.getCurrentState() == HeaderView.STATE.refreshing) {
            mHeaderView.updateState(HeaderView.STATE.end);
            if (!isTouchingScreen) {
                resetHeaderHeight();
            }
        } else {
            throw new IllegalStateException("can not stop refresh while it is not refreshing!");
        }
    }

    private void invokeOnScrolling() {
        if (mScrollListener instanceof OnXScrollListener) {
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrolling(this);
        }
    }

    private void updateHeaderHeight(int height) {
        if (mEnablePullRefresh) {
            if (mHeaderView.getCurrentState() == HeaderView.STATE.normal && height >= mHeaderView.getStretchHeight()) {
                //由normal变成stretch的逻辑：1、当前状态是normal；2、下拉.头达到了stretchheight的高度
                mHeaderView.updateState(HeaderView.STATE.stretch);
            } else if (mHeaderView.getCurrentState() == HeaderView.STATE.stretch && height >= mHeaderView.getReadyHeight()) {
                //由stretch变成ready的逻辑：1、当前状态是stretch；2、下拉头达到了readyheight的高度
                mHeaderView.updateState(HeaderView.STATE.ready);
            } else if (mHeaderView.getCurrentState() == HeaderView.STATE.stretch && height < mHeaderView.getStretchHeight()) {
                // 由stretch变成normal的逻辑：1、当前状态是stretch；2、下拉头高度小于stretchheight的高度
                mHeaderView.updateState(HeaderView.STATE.normal);
            } else if (mHeaderView.getCurrentState() == HeaderView.STATE.end && height < 2) {
                //由end变成normal的逻辑：1、当前状态是end；2、下拉头高度小于一个极小值
                mHeaderView.updateState(HeaderView.STATE.normal);
            }
        }
        mHeaderView.setVisiableHeight(height);      //动态设置HeaderView的高度
    }

    private void updateHeaderHeight(float delta) {
        int newheight = (int) delta + mHeaderView.getVisiableHeight();
        updateHeaderHeight(newheight);
    }

    /**
     * reset header view's height.
     * 重置headerheight的高度
     * 逻辑：1、如果状态处于非refreshing，则回滚到height=0状态2；2、如果状态处于refreshing，则回滚到stretchheight高度
     */
    private void resetHeaderHeight() {
        int height = mHeaderView.getVisiableHeight();
        if (height == 0) {
            // not visible.
            return;
        }
        // refreshing and header isn't shown fully. do nothing.
        if (mHeaderView.getCurrentState() == HeaderView.STATE.refreshing && height <= mHeaderView.getStretchHeight()) {
            return;
        }
        int finalHeight = 0; // default: scroll back to dismiss header.
        // is refreshing, just scroll back to show all the header.
        if ((mHeaderView.getCurrentState() == HeaderView.STATE.ready || mHeaderView.getCurrentState() == HeaderView.STATE.refreshing) && height > mHeaderView.getStretchHeight()) {
            finalHeight = mHeaderView.getStretchHeight();
        }

        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        // trigger computeScroll
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (mLastY == -1) {
            mLastY = ev.getRawY();
        }
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = ev.getRawY();
                isTouchingScreen = true;
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getFirstVisiblePosition() == 0 && (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
                    updateHeaderHeight(deltaY / OFFSET_RADIO);
                    invokeOnScrolling();
                }
                break;
            default:
                mLastY = -1;
                isTouchingScreen = false;

                if (getFirstVisiblePosition() == 0) {
                    resetHeaderHeight();
                }

                break;
        }
        return super.onTouchEvent(ev);
    }

    /**
     * Called by a parent to request that a child update its values for mScrollX
     * and mScrollY if necessary. This will typically be done if the child is
     * animating a scroll using a {@link Scroller Scroller}
     * object.
     */
    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            updateHeaderHeight(mScroller.getCurrY());
            if (mScroller.getCurrY() < 2 && mHeaderView.getCurrentState() == HeaderView.STATE.end) {
                //停止滚动了
                //逻辑：如果header范围进入了一个极小值内，且当前的状态是end，就把状态置成normal
                mHeaderView.updateState(HeaderView.STATE.normal);
            }
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
    }

    @Override
    public void setOnScrollListener(OnScrollListener l) {
        mScrollListener = l;
    }

    /**
     * Callback method to be invoked while the list view or grid view is being scrolled. If the
     * view is being scrolled, this method will be called before the next frame of the scroll is
     * rendered. In particular, it will be called before any calls to
     * {@link Adapter#getView(int, View, ViewGroup)}.
     *
     * @param view        The view whose scroll state is being reported
     * @param scrollState The current scroll state. One of
     *                    {@link #SCROLL_STATE_TOUCH_SCROLL} or {@link #SCROLL_STATE_IDLE}.
     */
    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (null != mScrollListener) {
            mScrollListener.onScrollStateChanged(view, scrollState);
        }
    }

    /**
     * Callback method to be invoked when the list or grid has been scrolled. This will be
     * called after the scroll has completed
     *
     * @param view             The view whose scroll state is being reported
     * @param firstVisibleItem the index of the first visible cell (ignore if
     *                         visibleItemCount == 0)
     * @param visibleItemCount the number of visible cells
     * @param totalItemCount   the number of items in the list adaptor
     */
    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        // send to user's listener
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    public void setWaterDropListViewListener(IWaterDropListViewListener l) {
        mListViewListener = l;
    }

    @Override
    public void notifyStateChanged(HeaderView.STATE oldState, HeaderView.STATE newState) {
        if (newState == HeaderView.STATE.refreshing) {
            if (mListViewListener != null) {
                mListViewListener.onRefresh();
            }
        }
    }

    public interface OnXScrollListener extends OnScrollListener {
        public void onXScrolling(View view);
    }


    public interface IWaterDropListViewListener {
        public void onRefresh();
    }
}
