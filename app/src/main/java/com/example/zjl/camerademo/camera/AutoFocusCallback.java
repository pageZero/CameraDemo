package com.example.zjl.camerademo.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by zjl on 18-1-3.
 * 相机自动对焦回调
 */

public class AutoFocusCallback implements Camera.AutoFocusCallback{
    private static final String TAG = AutoFocusCallback.class.getName();
    private static final long AUTO_FACUS_INTERVAL_MS = 1500L;

    private Handler mAutoFocusHandler;
    private int mAutoFocusMessageWhat;

    void setHandler(Handler autoFocusHandler, int autoFocusMessageWhat) {
        this.mAutoFocusHandler = autoFocusHandler;
        this.mAutoFocusMessageWhat = autoFocusMessageWhat;
    }
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (mAutoFocusHandler != null) {
            //由handler发出自动对焦的回调状态
            Message msg = mAutoFocusHandler.obtainMessage(mAutoFocusMessageWhat, success);
            mAutoFocusHandler.sendMessageDelayed(msg, AUTO_FACUS_INTERVAL_MS);
            mAutoFocusHandler = null;
        } else {
            Log.v(TAG, "Got auto-focus callback, but no handler for it");
        }
    }
}
