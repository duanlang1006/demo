package com.lang.demo.BottomRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import com.lang.demo.WaterDrop.HeaderView;

/**
 * Created by android on 10/29/15.
 */
public class BottomRefreshListView extends ListView implements AbsListView.OnScrollListener {
    private final String TAG = "duanlang";

    private float mLastY = -1; // save event y

    private IListViewListener mListViewListener;
    private OnScrollListener mScrollListener; // user's scroll listener

    private Scroller mScroller; // used for scroll back

    private FooterView mFooterView;

    private boolean mIsFooterReady = false;
    private boolean mEnablePullLoad;
    private boolean mPullLoading;

    private final static int SCROLL_DURATION = 400; // scroll back duration

    private final static float OFFSET_RADIO = 1.8f; // support iOS like pull
    private final static int PULL_LOAD_MORE_DELTA = 50; // when pull up >= 50px

    private int mTotalItemCount;


    public BottomRefreshListView(Context context) {
        super(context);
        initWithContext(context);
    }

    public BottomRefreshListView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    public BottomRefreshListView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mScroller = new Scroller(context, new DecelerateInterpolator());
        // XListView need the scroll event, and it will dispatch the event to
        // user's listener (as a proxy).
        super.setOnScrollListener(this);

        // init footer view
        mFooterView = new FooterView(context);
    }

    /**
     * Sets the data behind this ListView.
     * <p/>
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

    private void startLoadMore() {
        mPullLoading = true;
        mFooterView.setState(FooterView.STATE.loading);
        if (null != mListViewListener) {
            mListViewListener.onLoadMore();
        }
    }

    public void stopLoadMore() {
        if (mPullLoading) {
            mPullLoading = false;
            mFooterView.setState(FooterView.STATE.normal);
        }
        mFooterView.setEnabled(true);
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
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            invalidate();
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
                break;
            case MotionEvent.ACTION_MOVE:
                final float deltaY = ev.getRawY() - mLastY;
                mLastY = ev.getRawY();
                if (getLastVisiblePosition() == mTotalItemCount - 1 && (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
                    updateFooterHeight(-deltaY / OFFSET_RADIO);
                }
                break;
            default:
                mLastY = -1;

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
            mFooterView.setBottomMargin(mScroller.getCurrY());
            postInvalidate();
            invokeOnScrolling();
        }
        super.computeScroll();
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
        mTotalItemCount = totalItemCount;
        if (null != mScrollListener) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

    private void invokeOnScrolling() {
        if (mScrollListener instanceof OnXScrollListener) {
            OnXScrollListener l = (OnXScrollListener) mScrollListener;
            l.onXScrolling(this);
        }
    }

    public void setListViewListener(IListViewListener l) {
        mListViewListener = l;
    }

    public interface OnXScrollListener extends OnScrollListener {
        public void onXScrolling(View view);
    }

    public interface IListViewListener {
        public void onLoadMore();
    }

}
