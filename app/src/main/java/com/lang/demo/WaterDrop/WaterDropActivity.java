package com.lang.demo.WaterDrop;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Scroller;

import com.lang.demo.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by android on 10/24/15.
 */
public class WaterDropActivity extends Activity implements View.OnTouchListener, AbsListView.OnScrollListener {
    private final String TAG = "duanlang";

    private Scroller mScroller; // used for scroll back
    private AbsListView.OnScrollListener mScrollListener;

    private ListView mListView;
    private HeaderView mHeaderView;

    private boolean mEnablePullLoad;
    private boolean mPullLoading;


    private FooterView mFooterView;


    private float mLastY = -1;  //记录上一次y坐标值
    private boolean isTouchingScreen = false;

    private int mTotalItemCount;
    private final static int PULL_LOAD_MORE_DELTA = 50;

    private final static int SCROLL_DURATION = 400;

    private enum ScrollBack {
        header,
        footer
    }

    private ScrollBack mScrollBack;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    stopRefresh();
                    break;
                case 2:
                    stopLoad();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.waterdrop_main);
        mListView = (ListView) findViewById(R.id.listView);
        mListView.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1, getdata()));
        mScroller = new Scroller(this, new DecelerateInterpolator());
        mListView.setOnTouchListener(this);
        mListView.setOnScrollListener(this);

        //init header view
        mHeaderView = new HeaderView(this);
//        mHeaderView.setStateChangedListener(this);
        mListView.addHeaderView(mHeaderView);

        //init footer view
        mFooterView = (FooterView)findViewById(R.id.footview);
        setPullLoadEnable(true);
    }

    private List<String> getdata() {
        List<String> data = new ArrayList<String>();
        data.add("To see a world in a grain of sand,");
        data.add("And a heaven in a wild flower,");
        data.add("Hold infinity in the palm of your hand,");
        data.add("And eternity in an hour.");
        return data;
    }

    private void setPullLoadEnable(boolean enable) {
        Log.i(TAG, "setPullLoadEnable enable = " + enable);
        mEnablePullLoad = enable;
        if (!mEnablePullLoad) {
            mFooterView.hide();
            mFooterView.setOnClickListener(null);
        } else {
            Log.i(TAG, "mFooterView.show()");
            mPullLoading = false;
            mFooterView.show();
            mFooterView.setState(FooterView.STATE.normal);
            mFooterView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i(TAG, "mFooterView.onClick");
                    mFooterView.setEnabled(false);
                    startLoad();
                }
            });
        }
    }

    private void startLoad() {
        mPullLoading = true;
        mFooterView.setState(FooterView.STATE.loading);
        onLoad();
    }

    private void stopLoad() {
        if (mPullLoading) {
            mPullLoading = false;
            mFooterView.setState(FooterView.STATE.normal);
        }
        mFooterView.setEnabled(true);
    }

    private void onLoad() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    handler.sendEmptyMessage(2);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void stopRefresh() {

    }

    private void onRefresh() {
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                    handler.sendEmptyMessage(1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void updateHeaderHeight(int height) {
        mHeaderView.setVisiableHeight(height);
    }

    private void updateHeaderHeight(float delta) {
        Log.i(TAG, "updateHeaderHeight delta = " + delta);
        int newHeight = (int) delta + mHeaderView.getVisiableHeight();
        updateHeaderHeight(newHeight);
    }

    private void updateFooterHeight(float delta) {
        Log.i(TAG, "updateFooterHeight delta = " + delta);
        int height = mFooterView.getBottomMargin() + (int) delta;
        if (mEnablePullLoad && !mPullLoading) {
            if (height > PULL_LOAD_MORE_DELTA) { // height enough to invoke load
                mFooterView.setState(FooterView.STATE.upmove);
            } else {
                mFooterView.setState(FooterView.STATE.normal);
            }
        }
        mFooterView.setBottomMargin(height);
    }

    private void resetFooterHeight() {
        Log.i(TAG, "resetFooterHeight");
        int bottomMargin = mFooterView.getBottomMargin();
        if (bottomMargin > 0) {
            Log.i(TAG, "bottomMargin > 0");
            mScrollBack = ScrollBack.footer;
            mScroller.startScroll(0, bottomMargin, 0, -bottomMargin, SCROLL_DURATION);
            mListView.invalidate();
            computeScroll();
        }
    }

    private void computeScroll() {
        Log.i(TAG, "computeScroll");
        if (mScroller.computeScrollOffset()) {
            if (mScrollBack == ScrollBack.header) {
                Log.i(TAG, "computeScroll  ScrollBack.header");
//                updateHeaderHeight(mScroller.getCurrY());
//                if (mScroller.getCurrY() < 2 && mHeaderView.getCurrentState() == WaterDropListViewHeader.STATE.end) {
//                    //停止滚动了
//                    //逻辑：如果header范围进入了一个极小值内，且当前的状态是end，就把状态置成normal
//                    mHeaderView.updateState(WaterDropListViewHeader.STATE.normal);
//                }
            } else {
                mFooterView.setBottomMargin(mScroller.getCurrY());
            }
            mListView.postInvalidate();
//            invokeOnScrolling();
        }
        mListView.computeScroll();
    }


    /**
     * Called when a touch event is dispatched to a view. This allows listeners to
     * get a chance to respond before the target view.
     *
     * @param v     The view the touch event has been dispatched to.
     * @param event The MotionEvent object containing full information about
     *              the event.
     * @return True if the listener has consumed the event, false otherwise.
     */
    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if (mLastY == -1) {
            mLastY = event.getRawY();       //getY is to the container
        }

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLastY = event.getRawY();
                isTouchingScreen = true;
                break;
            case MotionEvent.ACTION_MOVE:
                float deltaY = event.getRawY() - mLastY;
                mLastY = deltaY;
                if ((mListView.getLastVisiblePosition() == mTotalItemCount - 1) &&
                        (mFooterView.getBottomMargin() > 0 || deltaY < 0)) {
                    updateFooterHeight(-deltaY / 1.8f);
                } else if (mListView.getFirstVisiblePosition() == 0 &&
                        (mHeaderView.getVisiableHeight() > 0 || deltaY > 0)) {
                    updateHeaderHeight(deltaY / 1.8f);
                }
                break;
            default:
                mLastY = -1; // reset
                isTouchingScreen = false;

                if (mListView.getLastVisiblePosition() == mTotalItemCount - 1) {
                    Log.i(TAG, "MotionEvent.default   1");
                    if (mEnablePullLoad && mFooterView.getBottomMargin() > PULL_LOAD_MORE_DELTA) {
                        Log.i(TAG, "MotionEvent.default   2");
                        startLoad();
                    }
                    resetFooterHeight();
                }
                break;
        }

        return super.onTouchEvent(event);
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
        Log.i(TAG, "onScrollStateChanged  scrollState :" + scrollState);
        if (mScrollListener != null) {
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
        Log.i(TAG, "onScroll  visibleItemCount :" + visibleItemCount + "  totalItemCount :" + totalItemCount);
        mTotalItemCount = totalItemCount;
        if (mScrollListener != null) {
            mScrollListener.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);
        }
    }

}
