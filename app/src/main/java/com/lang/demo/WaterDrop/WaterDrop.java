package com.lang.demo.WaterDrop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

/**
 * Created by android on 10/24/15.
 */
public class WaterDrop extends Activity {
    private final String TAG = "WaterDrop";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(new WaterDropView(this));
    }

}
