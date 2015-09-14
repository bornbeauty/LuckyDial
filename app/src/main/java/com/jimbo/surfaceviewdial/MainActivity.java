package com.jimbo.surfaceviewdial;

import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class MainActivity extends AppCompatActivity {


    LuckyDial mDial;
    ImageView mSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mDial = (LuckyDial) findViewById(R.id.LuckyPan);
        mSwitch = (ImageView) findViewById(R.id.iv_swtich);

        mDial.setHandler(handler);

        mSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDial.isRunning()) { //处于停止状态
                    if (!mDial.isStoped()) {
                        mDial.stop(1);
                        mSwitch.setImageResource(R.mipmap.start);
                    }
                } else {
                    if (mDial.isStoped()) {
                        mSwitch.setImageResource(R.mipmap.stop);
                        mDial.start();
                    }
                }
            }
        });
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
    Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            //通过get()方法获得抽到了什么奖品
            return true;
        }
    });
}
