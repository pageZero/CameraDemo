package com.example.zjl.camerademo.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

/**
 * Created by zjl on 18-1-3.
 */

public class PreviewCallback implements Camera.PreviewCallback {
    private static final String TAG = PreviewCallback.class.getName();

    private final CameraConfigurationManager mConfigManager;
    private Handler mPreviewHandler;
    private int mPreviewMessageWhat;

    PreviewCallback(CameraConfigurationManager manager) {
        mConfigManager = manager;
    }

    void setHandler(Handler previewHandler, int previewMessageWhat) {
        mPreviewHandler = previewHandler;
        mPreviewMessageWhat = previewMessageWhat;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //预览回调，data是预览数据，可以用于扫码识别
        //获取相机预览尺寸
        Camera.Size cameraResolution = mConfigManager.getCameraResolution();
        if (mPreviewHandler != null) {
            Message message =
                    mPreviewHandler.obtainMessage(mPreviewMessageWhat, cameraResolution.width, cameraResolution.height,
                            data);
            message.sendToTarget();
            mPreviewHandler = null;
        } else {
            Log.v(TAG, "Got preview callback, but no handler for it.");
        }
    }
}
