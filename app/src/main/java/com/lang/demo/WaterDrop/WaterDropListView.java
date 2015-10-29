package com.lang.demo.WaterDrop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
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
    private FooterView mFooterView;

    private boolean mEnablePullRefresh = true;

    private boolean mIsFooterReady = false;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;

    private ScrollBack mScrollBack;
    private boolean isTouchingScreen = false;//手指是否触摸屏幕
    private final static int SCROLL_DURATION = 400; // scroll back duration

    private enum ScrollBack {
        header,
        footer
    }

    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
    private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px

    // total list items, used to detect is at the bottom of listview.
    private int mTotalItemCount;
    private boolean pullEnable;


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

        // init footer view
        mFooterView = new FooterView(context);
    }

    /**
     * Sets the data behind this ListView.
     * <p>
     * The adapter passed to this method may be wrapped by a {@link WrapperListAdapter},
     * depending on the ListView features currently in use. For instance, adding
     * headers and/or footers will cause the adapter to be wrapped.
     *
     * @param adapter The ListAdapter which is responsible for maintaining the
     *                data backing this list and for producing a view to represent an
     *                item in that data set.
     * @see #getAdapter()
     */
    @Override
    public void setAdapter(ListAdapter adapter) {
        // make sure XListViewFooter is the last footer view, and only add once.
        if (!mIsFooterReady) {
            mIsFooterReady = true;
            addFooterView(mFooterView);
        }
        super.setAdapter(adapter);
    }

    public void setPullEnable(boolean pullEnable) {
        mEnablePullLoad = pullEnable;
        if (!mEnablePullLoad) {
            mFooterView.hide();
            mFooterView.setOnClickListener(null);
        } else {
            mPullLoading = false;
            mFooterView.show();
            mFooterView.setState(FooterView.STATE.normal);
            mFooterView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFooterView.setEnabled(false);
                    startLoadMore();
                }
            });
        }
    }



    public void stopRefresh() {
        Log.i(TAG, "stopRefresh");
        if (mHeaderView.getCurrentState() == HeaderView.STATE.refreshing) {
            mHeaderView.updateState(HeaderView.STATE.end);
            if (!isTouchingScreen) {
                resetHeaderHeight();
            }
        } else {
            throw new IllegalStateException("can not stop refresh while it is not refreshing!");
        }
    }

    public void stopLoadMore() {
        if (mPullLoading) {
            mPullLoading = false;
            mFooterView.setState(FooterView.STATE.normal);
        }
        mFooterView.setEnabled(true);
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
        Log.i(TAG, "resetHeaderHeight");
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

        mScrollBack = ScrollBack.header;
        mScroller.startScroll(0, height, 0, finalHeight - height, SCROLL_DURATION);
        // trigger computeScroll
        invalidate();
    }

    private void updateFooterHeight(float delta) {
        int height = mFooterView.getBottomMargin() + (int) delta;
        if (mEnablePullLoad && !mPullLoading) {
            if (height > PULL_LOAD_MORE_DELTA) {
                mFooterView.setState(FooterView.STATE.ready);
            } else {
                mFooterView.setState(FooterView.STATE.normal);
            }
        }
        mFooterView.setBottomMargin(height);
    }

    private void resetFooterHeight() {
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0) {
            mScrollBack = ScrollBack.footer;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
        }
    }

    private void startLoadMore() {
        mPullLoading = true;
        mFooterView.setState(FooterView.STATE.loading);
        if (null != mListViewListener) {
            mListViewListener.onLoadMore();
        }
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
                } else if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
                    updateFooterHeight(-deltaY / OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1;
                isTouchingScreen = false;

                if (getFirstVisiblePosition() == 0) {
                    resetHeaderHeight();
                }

                if (getLastVisiblePosition() == mTotalItemCount - 1) {
                    if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
                        startLoadMore();
                    }
                    resetFooterHeight();
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
            if (mScrollBack == ScrollBack.header) {
                updateHeaderHeight(mScroller.getCurrY());
                if (mScroller.getCurrY() < 2 && mHeaderView.getCurrentState() == HeaderView.STATE.end) {
                    //停止滚动了
                    //逻辑：如果header范围进入了一个极小值内，且当前的状态是end，就把状态置成normal
                    mHeaderView.updateState(HeaderView.STATE.normal);
                }
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
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
        mTotalItemCount = totalItemCount;
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

        public void onLoadMore();
    }
}
