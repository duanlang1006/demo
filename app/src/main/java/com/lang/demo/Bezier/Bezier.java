package com.lang.demo.Bezier;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by lang on 10/23/15.
 */
public class Bezier extends Activity {
    private final String TAG = "Bezier";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new AutoBezier(this));
//        setContentView(new MySurefaceView(this));
//        setContentView(new DrawingWithoutBezier(this));
//        setContentView(new DrawingWithBezier(this));
    }

}
