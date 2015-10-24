package com.lang.demo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.lang.demo.Bezier.Bezier;
import com.lang.demo.WaterDrop.WaterDrop;

public class Main extends Activity implements View.OnClickListener {
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
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.button1){
            intent = new Intent(this, Bezier.class);
            startActivity(intent);
        } else if (v.getId() == R.id.button2){
            intent = new Intent(this, WaterDrop.class);
            startActivity(intent);
        }
    }
}
