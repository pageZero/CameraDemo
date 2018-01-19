package com.example.zjl.camerademo.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by zjl on 18-1-4.
 */

public class PictureCallback implements Camera.PictureCallback {
    private static final String TAG = PictureCallback.class.getName();

    private Handler mPictureHandler;
    private int mPictureMessageWhat;

    void setHandler(Handler pictureHandlerHandler, int pictureMessageWhat) {
        this.mPictureHandler = pictureHandlerHandler;
        this.mPictureMessageWhat = pictureMessageWhat;
    }

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
        if (mPictureHandler != null) {
            Message msg = mPictureHandler.obtainMessage(mPictureMessageWhat, data);
            msg.sendToTarget();
            mPictureHandler = null;
        } else {
            Log.v(TAG, "picture taken, but no handler for it");
        }
    }
}
