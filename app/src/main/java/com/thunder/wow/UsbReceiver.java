package com.thunder.wow;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by CHENQIAO on 2017/7/25 14:06.
 * E-mail: mrjctech@gmail.com
 */

public class UsbReceiver extends BroadcastReceiver {

    protected static final String TAG = "UsbReceiver";
    Activity mAcytivity;
    private IntentFilter filter = new IntentFilter();

    public UsbReceiver() {
    }

    public UsbReceiver(Context context) {
        mAcytivity = (Activity) context;
        filter.addAction(Intent.ACTION_MEDIA_CHECKING);
        filter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        filter.addAction(Intent.ACTION_MEDIA_EJECT);
        filter.addAction(Intent.ACTION_MEDIA_REMOVED);
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);
        filter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        filter.addDataScheme("file");
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        String action = intent.getAction();
        Log.i(TAG, "SDMountReceiver.....     action:" + action);

        String path = intent.getDataString();
        String newPath = path.substring("file://".length(), path.length());
        if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
            Log.i(TAG, "ACTION_MEDIA_MOUNTED......   path:" + newPath);

            ((MainActivity)mAcytivity).notifyImg(newPath);
        } else if (action.equals(Intent.ACTION_MEDIA_REMOVED)) {
            Log.i(TAG, "ACTION_MEDIA_REMOVED.....   path:" + newPath);
        }else if(action.equals("android.intent.action.MEDIA_UNMOUNTED")){
            Log.i(TAG, "MEDIA_UNMOUNTED.....   path:" + newPath);
        }
    }
}
