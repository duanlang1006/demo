package com.lang.demo.BottomRefresh;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lang.demo.R;

/**
 * Created by android on 10/29/15.
 */
public class FooterView extends LinearLayout {
    private final String TAG = "duanlang";

    private Context mContext;

    private View mContentView;
    private View mProgressBar;
    private TextView mHintView;
    private LinearLayout layout_progress;

    public enum STATE {
        normal,
        ready,
        loading
    }

    public FooterView(Context context) {
        super(context);
        initWithContext(context);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initWithContext(context);
    }

    private void initWithContext(Context context) {
        mContext = context;
        LinearLayout footview = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.foot, null);
        addView(footview);
        footview.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mContentView = footview.findViewById(R.id.load_progressbar_layout);
        mProgressBar = footview.findViewById(R.id.load_progressbar);
        mHintView = (TextView) footview.findViewById(R.id.load_hint_textview);
        layout_progress = (LinearLayout) footview.findViewById(R.id.progresslayout);

    }

    public void setState(STATE state) {
        mProgressBar.setVisibility(INVISIBLE);
        mHintView.setVisibility(INVISIBLE);
        layout_progress.setVisibility(INVISIBLE);
        if (state == STATE.ready) {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(getResources().getString(R.string.listfooterview_ready));
        } else if (state == STATE.loading) {
            mProgressBar.setVisibility(VISIBLE);
            layout_progress.setVisibility(VISIBLE);
        } else {
            mHintView.setVisibility(View.VISIBLE);
            mHintView.setText(getResources().getString(R.string.listfooterview_normal));
        }
    }

    public void show() {
        LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
        layoutParams.height = LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(layoutParams);
    }

    public void hide() {
        LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
        layoutParams.height = 0;
        mContentView.setLayoutParams(layoutParams);
    }

    public void setBottomMargin(int height) {
        if (height < 0)
            return;
        LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
        layoutParams.bottomMargin = height;
        mContentView.setLayoutParams(layoutParams);
    }

    public int getBottomMargin() {
        LayoutParams layoutParams = (LayoutParams) mContentView.getLayoutParams();
        return layoutParams.bottomMargin;
    }

}
