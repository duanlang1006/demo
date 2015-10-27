package com.lang.demo.WaterDrop;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.lang.demo.R;

/**
 * Created by android on 10/27/15.
 */
public class FooterView extends LinearLayout {
    private final String TAG = "duanlang";

    private Context mContext;

    private View mContentView;

    private LinearLayout mloadprogressbar;
    private TextView mloadtextview;

    public enum STATE {
        upmove,
        loading,
        normal
    }

    public FooterView(Context context) {
        super(context);
        init(context);
    }

    public FooterView(Context context, AttributeSet attrs) {
        super(context, attrs);
        Log.i(TAG, "FooterView");
        init(context);
    }

    public FooterView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        Log.i(TAG, "FooterView init");
        mContext = context;
        LinearLayout loadmoreView = (LinearLayout) LayoutInflater.from(mContext).inflate(R.layout.load, null);
        addView(loadmoreView);
        loadmoreView.setLayoutParams(new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        mContentView = loadmoreView.findViewById(R.id.load_progressbar_layout);

        mloadprogressbar = (LinearLayout) findViewById(R.id.load_progressbar);
        mloadtextview = (TextView) findViewById(R.id.load_hint_textview);

        mloadprogressbar.setVisibility(INVISIBLE);
        mloadtextview.setVisibility(VISIBLE);
    }

    public void setState(STATE state) {
        Log.i(TAG, "state = " + state);
        mloadprogressbar.setVisibility(INVISIBLE);
        mloadtextview.setVisibility(INVISIBLE);
        if (state == STATE.loading) {
            mloadprogressbar.setVisibility(VISIBLE);
        } else if (state == STATE.normal) {
            mloadtextview.setVisibility(VISIBLE);
            mloadtextview.setText(getResources().getString(R.string.listfooterview_normal));
        } else if (state == STATE.upmove) {
            mloadtextview.setVisibility(View.VISIBLE);
            mloadtextview.setText(getResources().getString(R.string.listfooterview_upmove));
        }
    }

    public void hide() {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = 0;
        mContentView.setLayoutParams(lp);
    }

    public void show() {
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.height = LayoutParams.WRAP_CONTENT;
        mContentView.setLayoutParams(lp);
    }

    public void setBottomMargin(int height) {
        Log.i(TAG, "setBottomMargin  height = " + height);
        if (height < 0) return;
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        lp.bottomMargin = height;
        mContentView.setLayoutParams(lp);
    }

    public int getBottomMargin() {
        Log.i(TAG, "getBottomMargin");
        LayoutParams lp = (LayoutParams) mContentView.getLayoutParams();
        return lp.bottomMargin;
    }

}
