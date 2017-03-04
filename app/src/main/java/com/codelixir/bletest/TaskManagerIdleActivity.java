package com.codelixir.bletest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;

import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;

public class TaskManagerIdleActivity extends Activity
{
    BleManager bleManager;

    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        BleManagerConfig config = new BleManagerConfig();

        bleManager = BleManager.get(this, config);

        handler = new Handler();
    }

    BleManager getBleManager()
    {
        return bleManager;
    }

    Handler getHandler()
    {
        return handler;
    }
}
