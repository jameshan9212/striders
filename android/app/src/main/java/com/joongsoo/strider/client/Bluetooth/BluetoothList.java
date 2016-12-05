package com.joongsoo.strider.client.Bluetooth;

import java.util.Set;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

import com.joongsoo.strider.client.R;

public class BluetoothList extends Activity {

    // Debugging
    private static final String TAG = "BluetoothListActivity";

    private BluetoothAdapter mBtAdapter;
    private ArrayAdapter<String> mNewArrayAdapter;
    private ArrayAdapter<String> mPairedArrayAdapter;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Setup the window
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        setContentView(R.layout.devicelist);

        // if in case back off set the result for CANCELED
        setResult(Activity.RESULT_CANCELED);

        mNewArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
        mPairedArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);

        ListView newDeviceListView = (ListView) findViewById(R.id.new_devices);
        newDeviceListView.setAdapter(mNewArrayAdapter);
        newDeviceListView.setOnItemClickListener(mDeviceClickListener);

        ListView pairedDeviceListView = (ListView) findViewById(R.id.paired_devices);
        pairedDeviceListView.setAdapter(mPairedArrayAdapter);
        pairedDeviceListView.setOnItemClickListener(mDeviceClickListener);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);// Register for broadcasts when discovery has finished

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();
        //if there are paired devices
        if (pairedDevices.size() > 0){
            for(BluetoothDevice device : pairedDevices) {
                // add the name and addr.
                mPairedArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        } else {
            String noDevices = getResources().getText(R.string.none_paired).toString();
            mPairedArrayAdapter.add(noDevices);
        }
        doDiscovery();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if(mBtAdapter != null){
            mBtAdapter.cancelDiscovery();
        }

        this.unregisterReceiver(mReceiver);
    }

    private void doDiscovery() {
        Log.d(TAG, "doDiscovery()");

        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);

        // Turn on sub-title for new devices
        findViewById(R.id.title_new_devices).setVisibility(View.VISIBLE);

        if(mBtAdapter.isDiscovering()) {
            mBtAdapter.cancelDiscovery();
        }
        mBtAdapter.startDiscovery();
    }

    // The onLclick listener for all devices in the ListViews
    private OnItemClickListener mDeviceClickListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            // Cancel discovery because it's costly and we're about to connect
            mBtAdapter.cancelDiscovery();

            // Get the device MAC address, which is the last 17 chars in the View
            String info = ((TextView) v).getText().toString();
            String address = info.substring(info.length() - 17);

            // Create the result Intent and include the MAC address
            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            // Set result and finish this Activity
            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private final BroadcastReceiver mReceiver = new BroadcastReceiver(){

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device
                        = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                mNewArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                setTitle(R.string.select_device);
                if (mNewArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(R.string.none_found).toString();
                    mNewArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
