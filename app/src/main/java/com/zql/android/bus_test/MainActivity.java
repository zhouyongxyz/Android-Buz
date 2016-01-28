package com.zql.android.bus_test;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.util.TimeUtils;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;


import com.zql.android.buz.Buz;
import com.zql.android.buz.Handle;
import com.zql.android.buz.What;
import com.zql.android.buz.Zog;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {


    @What(handlerName = "MainActivityHandler")
    public static final int MESSAGE_FRESH_LIST = 100000;

    @Handle(name = "MainActivityHandler")
    public Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MESSAGE_FRESH_LIST:
                    Zog.tagd("  handle message with msg.what = " + msg.what + " arg1 = " + msg.arg1 + "  arg2 = " + msg.arg2 + "   obj = " + msg.obj);
                    break;
            }
        }
    };

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        try {
            Buz.getInstance().paste(this,this.getClass());
        } catch (Buz.BuzException e) {
            e.printStackTrace();
        }
        Buz.getInstance().launch();
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        button = (Button)findViewById(R.id.testBtn);
        final Random random = new Random();
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Zog.tagd(" on click");
                for(int i = 0;i<100;i++){
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while(true){
                                try {
                                    TimeUnit.SECONDS.sleep(1);
                                    Message msg = Message.obtain();
                                    msg.what = MESSAGE_FRESH_LIST;
                                    msg.arg1 = random.nextInt(1000);
                                    msg.arg2 = random.nextInt(1000);
                                    msg.obj = Thread.currentThread().getName();
                                    Buz.getInstance().create(msg);

                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        }
                    }, "thread-" + i);
                    thread.start();
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Buz.getInstance().free(this,this.getClass());
    }
}
