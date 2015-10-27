package com.lang.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.lang.demo.Bezier.BezierActivity;
import com.lang.demo.WaterDrop.WaterDropActivity;

public class MainActivity extends Activity implements View.OnClickListener {
    private final String TAG = "Main";

    private Button button1;
    private Button button2;

    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init();
    }

    private void init(){
        initButtons();
    }

    private void initButtons(){
        button1 = (Button)findViewById(R.id.button1);
        button1.setOnClickListener(this);
        button1.setText("贝塞尔曲线");

        button2 = (Button)findViewById(R.id.button2);
        button2.setOnClickListener(this);
        button2.setText("水滴特效");

        intent = new Intent(this, WaterDropActivity.class);
        startActivity(intent);
    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1){
            intent = new Intent(this, BezierActivity.class);
            startActivity(intent);
        } else if (v.getId() == R.id.button2){
            intent = new Intent(this, WaterDropActivity.class);
            startActivity(intent);
        }
    }
}
