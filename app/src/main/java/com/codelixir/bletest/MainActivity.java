package com.codelixir.bletest;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.idevicesinc.sweetblue.BleDevice;
import com.idevicesinc.sweetblue.BleDeviceState;
import com.idevicesinc.sweetblue.BleManager;
import com.idevicesinc.sweetblue.BleManagerConfig;
import com.idevicesinc.sweetblue.BleManagerState;
import com.idevicesinc.sweetblue.BleScanMode;
import com.idevicesinc.sweetblue.utils.Interval;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class MainActivity extends AppCompatActivity {

    BleManager mgr;
    private ListView mListView;
    private Button mStartScan;
    private Button mStopScan;
    private ScanAdaptor mAdaptor;
    private ArrayList<BleDevice> mDevices;

    private final String TAG = getClass().getSimpleName().toString();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mListView = (ListView) findViewById(R.id.listView);
        mDevices = new ArrayList<>(0);
        mAdaptor = new ScanAdaptor(this, mDevices);
        mListView.setAdapter(mAdaptor);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final BleDevice device = mDevices.get(position);
                device.setListener_State(new BleDevice.StateListener() {
                    @Override
                    public void onEvent(StateEvent e) {
                        if (e.didEnter(BleDeviceState.CONNECTED)) {
                            Log.d(TAG, "BleDeviceState.CONNECTED");
                            //device.bond();
                            UUID[] uuids = device.getAdvertisedServices();
                            for (UUID uuid : uuids) {
                                Log.d(TAG, "Service:" + uuid.toString());
                            }
                        }
                        if (e.didEnter(BleDeviceState.BONDING)) {
                            Log.d(TAG, "BleDeviceState.BONDING");
                            //device.
                        }
                        if (e.didEnter(BleDeviceState.UNBONDED)) {
                            Log.d(TAG, "BleDeviceState.UNBONDED");
                            //device.
                        }
                        if (e.didEnter(BleDeviceState.BONDED)) {
                            Log.d(TAG, "BleDeviceState.BONDED");
                            //device.
                        }
                        if (e.didEnter(BleDeviceState.DISCOVERING_SERVICES)) {
                            Log.d(TAG, "BleDeviceState.DISCOVERING_SERVICES");
                        }
                        if (e.didEnter(BleDeviceState.SERVICES_DISCOVERED)) {
                            Log.d(TAG, "BleDeviceState.SERVICES_DISCOVERED");

                        }
                        Log.d(TAG, "device:" + e.device().getName_normalized());
                        Log.d(TAG, "device.onEvent:" + e.enterMask() + " " + e.exitMask());
                    }
                });
                Log.d(TAG, "device.connect()");
                device.connect();
            }
        });
        mListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                BleDevice device = mDevices.get(position);
                if (device.is(BleDeviceState.CONNECTED)) {
                    Log.d(TAG, "device.disconnect()");
                    device.disconnect();
                    return true;
                }
                return false;
            }
        });

        mStartScan = (Button) findViewById(R.id.startScan);
        mStartScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDevices.clear();
                mAdaptor.notifyDataSetChanged();
                mgr.startScan();
            }
        });
        mStopScan = (Button) findViewById(R.id.stopScan);
        mStopScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgr.stopScan();
            }
        });

        BleManagerConfig config = new BleManagerConfig();
        config.loggingEnabled = true;
        config.scanMode = BleScanMode.POST_LOLLIPOP;
        mgr = BleManager.get(this, config);
        mgr.setListener_State(new BleManager.StateListener() {
            @Override
            public void onEvent(StateEvent event) {
                if (event.didEnter(BleManagerState.ON)) {
                    Log.d(TAG, "BleManagerState.ON");
                    mStartScan.setEnabled(true);
                } else if (event.didEnter(BleManagerState.SCANNING)) {
                    Log.d(TAG, "BleManagerState.SCANNING");
                    mStartScan.setEnabled(false);
                    mStopScan.setEnabled(true);
                } else if (event.didExit(BleManagerState.SCANNING)) {
                    Log.d(TAG, "BleManagerState.EXITSCANNING");
                    mStartScan.setEnabled(true);
                    mStopScan.setEnabled(false);
                }
            }
        });
        mgr.setListener_Discovery(new BleManager.DiscoveryListener() {
            @Override
            public void onEvent(BleManager.DiscoveryListener.DiscoveryEvent e) {
                if (e.was(BleManager.DiscoveryListener.LifeCycle.DISCOVERED)) {
                    Log.d(TAG, "LifeCycle.DISCOVERED)");
                    mDevices.add(e.device());
                    mAdaptor.notifyDataSetChanged();
                } else if (e.was(BleManager.DiscoveryListener.LifeCycle.REDISCOVERED)) {
                    Log.d(TAG, "LifeCycle.REDISCOVERED)");
                }
            }
        });

        if (mgr.is(BleManagerState.OFF)) {
            mStartScan.setEnabled(false);
            mgr.turnOn();
        } else {
            mStartScan.setEnabled(true);
        }
    }

    private class ScanAdaptor extends ArrayAdapter<BleDevice> {

        private List<BleDevice> mDevices;


        public ScanAdaptor(Context context, List<BleDevice> objects) {
            super(context, R.layout.scan_listitem_layout, objects);
            mDevices = objects;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder v;
            if (convertView == null) {
                convertView = View.inflate(getContext(), R.layout.scan_listitem_layout, null);
                v = new ViewHolder();
                v.name = (TextView) convertView.findViewById(R.id.name);
                v.rssi = (TextView) convertView.findViewById(R.id.rssi);
                convertView.setTag(v);
            } else {
                v = (ViewHolder) convertView.getTag();
            }
            v.name.setText(mDevices.get(position).toString());
            //v.rssi.setText(String.valueOf(mDevices.get(position).getRssi()));
            return convertView;
        }

    }

    private static class ViewHolder {
        private TextView name;
        private TextView rssi;
    }
}
