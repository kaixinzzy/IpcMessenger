package com.zzy.ipcmessenger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.zzy.service.ServiceT;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private boolean isConnected = false;
    private Messenger messenger = null;

    public final MyHandler myHandler = new MyHandler();
    public static final int msg_what = 1;
    public static class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case msg_what:
                    Log.i(TAG,"收到服务端信息-------"+msg.getData().get("key"));
                    break;
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isConnected) {
            connectService();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (isConnected) {
            unbindService(mServiceConnection);
            isConnected = false;
        }
    }

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "service connected");
            isConnected = true;
            messenger = new Messenger(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "service disconnected");
            isConnected = false;
            messenger = null;

        }
    };

    // 连接Service
    private void connectService() {
        Intent intent = new Intent();
        intent.setAction("com.zzy.messenger");
        intent.setPackage("com.zzy.ipcmessenger");// 设置应用包名，不然android5.0以后隐式启动服务报异常
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onClick(View v) {
        if (!isConnected) {
            connectService();
            return;
        }
        if (messenger == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.send:// 向Service发送消息
                Message msg = Message.obtain(null, ServiceT.msg_what);
                Bundle bundle = new Bundle();
                bundle.putString("key", "我是Client");
                msg.setData(bundle);
                // Service回复消息用
                msg.replyTo = new Messenger(myHandler);
                try {
                    messenger.send(msg);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
