package com.joongsoo.strider.client;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;
import android.support.v4.view.ViewPager;

import com.joongsoo.strider.client.Bluetooth.BluetoothConnection;
import com.joongsoo.strider.client.Bluetooth.BluetoothList;
import com.joongsoo.strider.client.Strider.stride_identify;
import com.joongsoo.strider.client.Fragment.TutorialFragment;
import com.joongsoo.strider.client.Strider.stride_profile;
import com.joongsoo.strider.client.Strider.stride_register;

import java.util.ArrayList;

/**
 * Created by joongsoo on 2016-10-21.
 */
public class Main extends FragmentActivity implements View.OnClickListener{

    //Main screen init
    static ViewMapper tutorialView;
    static ArrayList<TutorialFragment> flist = new ArrayList<TutorialFragment>();
    private static ViewPager mPager = null;

    // default : 0(register), 1(identify), 2(profile)
    private static int start_mode = 0;
    private BluetoothAdapter mBluetoothAdapter = null;

    // Message types sent from the BluetoothConnection Handler
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    // received message identifier
    private static final int MESSAGE_DEFAULT= 0;   // use lowercase
    private static final int MESSAGE_IDENTIFY= 1;   // use lowercase
    private static final int MESSAGE_SUCCESS= 2;   // use lowercase
    private static final int MESSAGE_FAILIURE= 3;   // use lowercase
    private static final int MESSAGE_PENDING= 4;   // use lowercase
    public static final String TOAST = "toast";

    // Key names received from the BluetoothConnection Handler
    public static final String DEVICE_NAME = "device_name";
    public static boolean STRIDER_IF_IDENTIFIED = false;
    public static String STRIDER_ID = "";
    public static String STRIDER_PARAMS = "";
    public static String STRIDER_PROFILE = "";

    public static BluetoothConnection mConnection = null;
    private String mConnectedDeviceName = null;         // Name of the connected device

    // Intent request codes
    private static final int REQUEST_CONNECT_DEVICE = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private static final String TAG = "STRIDER_MAIN";
    private static final boolean D = true;

    // singleton design
    private static Main instance = new Main();
    public Main() {}

    public static Main getInstance() {
        if ( instance == null )
            instance = new Main();
        return instance;
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        tutorialView = new ViewMapper();
        viewInit();
        ImageButton bluetooth_mode = (ImageButton)findViewById(R.id.bluetooth_mode);
        ImageButton start = (ImageButton)findViewById(R.id.start);

        bluetooth_mode.setOnClickListener(this);
        start.setOnClickListener(this);

        // exit
        bluetoothInit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!mBluetoothAdapter.isEnabled()){
            Intent enableBtIntent
                    = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (mConnection == null)
                setupConnection();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        if(D) Log.e(TAG, "--- ON RESUME ---");

        if (mConnection != null) {
            // Only if the state is STATE_NONE, do we know that we haven't started already
            if (mConnection.getState() == BluetoothConnection.STATE_NONE) {
                // Start the Bluetooth services
                mConnection.start();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mConnection != null)
            mConnection.stop();
        if(D) Log.e(TAG, "--- ON DESTROY ---");
    }

    private class ViewMapper{
        private ViewPager pagerTutorial = null;
        private ViewMapper() {
            pagerTutorial = (ViewPager) findViewById(R.id.Mfrag_1);
        }
    }

    public void viewInit(){
        flist.add(new com.joongsoo.strider.client.Fragment.Frag_main_1());
        flist.add(new com.joongsoo.strider.client.Fragment.Frag_main_2());
        flist.add(new com.joongsoo.strider.client.Fragment.Frag_main_3());
        tutorialView.pagerTutorial.setAdapter(new com.joongsoo.strider.client.Fragment.TutorialPagerAdapter(getSupportFragmentManager(), flist));

        tutorialView.pagerTutorial.setOnPageChangeListener(pageChangeListener);
        mPager = (ViewPager) findViewById(R.id.Mfrag_1);
        mPager.setAdapter(new com.joongsoo.strider.client.Fragment.TutorialPagerAdapter(getSupportFragmentManager(), flist));
    }

    PageChangeListener pageChangeListener = new PageChangeListener();

    private class PageChangeListener implements ViewPager.OnPageChangeListener {
        public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}

        public void onPageSelected(int position) {
            flist.get(position);

            switch(position){
                case 0:
                    start_mode =0;
                    break;
                case 1:
                    start_mode =1;
                    break;
                case 2:
                    start_mode =2;
                    break;
                default:
                    start_mode=0;
                    break;
            }
        }
        public void onPageScrollStateChanged(int state) {}
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.bluetooth_mode:
                Intent intent = new Intent(Main.this, BluetoothList.class);
                startActivityForResult(intent, REQUEST_CONNECT_DEVICE);
                break;

            case R.id.start:
                if(start_mode==0) {
                    // MODE : REGISTER
                    Intent intetn = new Intent(this, stride_register.class);
                    startActivity(intetn);

                    // MODE : IDENTIFY
                }else if(start_mode==1){
                    Intent intent2 = new Intent(this, stride_identify.class);
                    startActivity(intent2);

                    // MODE : PROFILE
                }else if(start_mode==2){
                    Intent intent3 = new Intent(this, stride_profile.class);
                    startActivity(intent3);
                }
                break;
        }
    }

    public void bluetoothInit(){
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if (mBluetoothAdapter == null){
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
    }

    private void setupConnection() {
        // Initialize the BluetoothConnection to perform bluetooth connections
        mConnection = new BluetoothConnection(this, mHandler);
    }

    private void ensureDiscoverable() {
        if(D) Log.d(TAG, "ensure discoverable");
        if (mBluetoothAdapter.getScanMode() !=
                BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    public void bluetoothTurnOn(){
        ImageButton bluetoothTurn = (ImageButton)findViewById(R.id.bluetooth_onoff);
        bluetoothTurn.setImageResource(R.drawable.bluetooth_on);
    }

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    if(D) Log.i(TAG, "MESSAGE_STATE_CHANGE: " + msg.arg1);
                    switch (msg.arg1) {
                        case BluetoothConnection.STATE_CONNECTED:
                           // mTitle.setText(R.string.title_connected_to);
                            //mTitle.append(mConnectedDeviceName);
                            Log.i(TAG, "connected:" + mConnectedDeviceName);

                            bluetoothTurnOn();

                            break;
                        case BluetoothConnection.STATE_CONNECTING:
                            //mTitle.setText(R.string.title_connecting);
                            Log.i(TAG, "connecting...");
                            break;
                        case BluetoothConnection.STATE_LISTEN:
                            Log.i(TAG, "listening...");
                        case BluetoothConnection.STATE_NONE:
                           // mTitle.setText(R.string.title_not_connected);
                            Log.i(TAG, "not connected...");
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    // if(D) Log.i(TAG, "--- Main --- mHandler MESSAGE_WRITE: " + msg.arg1);
                    byte[] writeBuf = (byte[]) msg.obj;
                    // construct a string from the buffer
                    String writeMsg = new String(writeBuf);
                    if(D) Log.i(TAG, "--- Main --- mHandler MESSAGE_WRITE: " + writeMsg);
                    break;
                case MESSAGE_READ:
                    if(D) Log.i(TAG, "--- Main --- mHandler MESSAGE_READ: " + msg.arg1);
                    if(D) Log.i(TAG, "--- Main --- mHandler MESSAGE_READ: " + msg.arg2);
                    byte[] readBuf = (byte[]) msg.obj;
                    // construct a string from the valid bytes in the buffer
                    String readMessage = new String(readBuf, 0, msg.arg1);
                    receiveMessageHandler(readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:
                    if(D) Log.i(TAG, "--- Main --- mHandler MESSAGE_NAME: " + msg.arg1);
                    // save the connected device's name
                    mConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(), "Connected to "
                            + mConnectedDeviceName, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (D)
            Log.d(TAG, "onActivityResult " + resultCode);

        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE:
                // When DeviceListActivity returns with a device to connect
                if (resultCode == Activity.RESULT_OK) {
                    // Get the device MAC address
                    String address = data.getExtras()
                            .getString(BluetoothList.EXTRA_DEVICE_ADDRESS);
                    // Get the BLuetoothDevice object
                    BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
                    // Attempt to connect to the device
                    mConnection.connect(device);
                }
                break;
            case REQUEST_ENABLE_BT:
                // When the request to enable Bluetooth returns
                if (resultCode == Activity.RESULT_OK) {
                    // Bluetooth is now enabled, so set up a chat session
                    setupConnection();
                } else {
                    // User did not enable Bluetooth or an error occured
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    finish();
                }
        }
    }
    public void sendMessage(String mMsg) {
        // Check that we're actually connected before trying anything
        if (mConnection.getState() != BluetoothConnection.STATE_CONNECTED) {
            Log.d(TAG, "sendMessage - mConnection != STATE_CONNECTED");
            return;
        }

        // Check that there's actually something to send
        if (mMsg.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = mMsg.getBytes();
            mConnection.write(send);
        }
    }

    public void receiveMessageHandler(String mMsg) {
        // Check that we're actually connected before trying anything
        Log.d(TAG, "mMsg: "+mMsg);
        String tokken[] = mMsg.split("`");
        int tag = Integer.parseInt(tokken[0]);

        switch(tag) {
            case MESSAGE_DEFAULT:
                Toast.makeText(getApplication(), tokken[1],Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_IDENTIFY:
                Toast.makeText(getApplication(), "ID: " + tokken[1],Toast.LENGTH_SHORT).show();
                STRIDER_IF_IDENTIFIED = true;
                this.STRIDER_PROFILE = tokken[1];
                this.STRIDER_ID = tokken[2];
                this.STRIDER_PARAMS = tokken[3];
                break;
            case MESSAGE_SUCCESS:
                Log.d(TAG, "MESSAGE_IDENTIFY_SUCCESS");
                stride_identify.getInstance().getIdentified(MESSAGE_SUCCESS);
                break;
            case MESSAGE_FAILIURE:
                Log.d(TAG, "MESSAGE_IDENTIFY_FAILIURE");
                stride_identify.getInstance().getIdentified(MESSAGE_FAILIURE);
                break;
            case MESSAGE_PENDING:
                Log.d(TAG, "MESSAGE_IDENTIFY_PENDING");
                stride_identify.getInstance().getIdentified(MESSAGE_PENDING);
                break;
        }
    }
}