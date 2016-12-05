package com.joongsoo.strider.client.Strider;

import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.joongsoo.strider.client.Main;
import com.joongsoo.strider.client.R;

/*
 * Created by joongsoo on 2016-10-21.
 */
public class stride_profile extends Activity {

    // received message identifier
    private static final int MESSAGE_DEFAULT= 0;   // use lowercase
    private static final int MESSAGE_IDENTIFY= 1;   // use lowercase
    private static final int MESSAGE_SUCCESS= 2;   // use lowercase
    private static final int MESSAGE_FAILIURE= 3;   // use lowercase
    private static final int MESSAGE_PENDING= 4;   // use lowercase
    private static final String TAG = "STRIDER_PROFILE";

    static Main main;
    public static String STRIDER_ID = "";
    public static String STRIDER_PARAMS = "";
    public static String STRIDER_PROFILE = "";

    static ImageView iv_profile;
    static TextView tv_profile;
    static TextView tv_id;
    static TextView tv_params;

    // singleton design
    private static stride_profile instance = new stride_profile();
    public stride_profile() {}

    public static stride_profile getInstance() {
        if ( instance == null )
            instance = new stride_profile();
        return instance;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)

    public void onCreate(Bundle savedInstaceState) {
        super.onCreate(savedInstaceState);
        Log.d(TAG, "--- onCreate() --- ");
        setContentView(R.layout.strider_profile);

        iv_profile = (ImageView) findViewById(R.id.iv_profile_profile);
        tv_profile = (TextView) findViewById(R.id.tv_profile_profile);
        tv_id = (TextView) findViewById(R.id.tv_profile_id);
        tv_params = (TextView) findViewById(R.id.tv_profile_params);

        main = Main.getInstance();
        if(main.STRIDER_IF_IDENTIFIED) {
            this.STRIDER_PROFILE = main.STRIDER_PROFILE;
            this.STRIDER_ID = main.STRIDER_ID;
            this.STRIDER_PARAMS = main.STRIDER_PARAMS;
            getIdentified(STRIDER_PROFILE, STRIDER_ID, STRIDER_PARAMS);
        }
    }

    protected void onResume() {
        super.onResume();
        Log.d(TAG, "--- onResume() --- ");
        /*
        get profile
         */
        this.STRIDER_PROFILE = main.STRIDER_PROFILE;
        this.STRIDER_ID = main.STRIDER_ID;
        this.STRIDER_PARAMS = main.STRIDER_PARAMS;
        getIdentified(STRIDER_PROFILE, STRIDER_ID, STRIDER_PARAMS);
    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "--- onPause() --- ");
    }

    public void getIdentified(String profile, String id, String params) {
//        iv_profile.setImageResource(R.drawable.iv_profile);
        tv_profile.setText("PROFILE: " + profile);
        tv_id.setText("ID: " + id);
        tv_params.setText("PARAMS: " + params);
    }
}