package com.example.zjl.camerademo.camera;

import android.os.Handler;
import android.os.Message;

import com.example.zjl.camerademo.CameraActivity;
import com.example.zjl.camerademo.R;
import com.example.zjl.camerademo.SaveImageThread;

/**
 * Created by zjl on 18-1-3.
 * 处理调用相机过程中的各种message
 */

public class CaptureHandler extends Handler {
    private static final String TAG = CaptureHandler.class.getName();

    private final CameraActivity mActivity;
    private final SaveImageThread mSaveImageThread;
    private State mState;

    public CaptureHandler(CameraActivity activity) {
        this.mActivity = activity;
        mSaveImageThread = new SaveImageThread("save-image-thread", mActivity);
        mSaveImageThread.start();
        mSaveImageThread.initHandler(mSaveImageThread.getLooper());
        mState = State.SUCCESS;
        // Start ourselves capturing previews and decoding.
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case R.id.auto_focus:
                if (mState == State.PREVIEW) {
                    CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
                }
                break;
            case R.id.take_picture_success:
                mState = State.SUCCESS;
                Message saveMessage = mSaveImageThread.getHandler().obtainMessage(R.id.save_image, msg.obj);
                mSaveImageThread.getHandler().sendMessage(saveMessage);
                restartPreviewAndDecode();
                break;
        }
    }

    public void quitSynchronously() {
        mState = State.DONE;
        CameraManager.get().stopPreview();
        Message quit = Message.obtain(mSaveImageThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            mSaveImageThread.join();
        } catch (InterruptedException e) {
            // continue
        }
    }

    public void restartPreviewAndDecode() {
        if (mState != State.PREVIEW) {
            CameraManager.get().startPreview();
            mState = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(null, 0);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

    public void startCapture() {
        if (mState == State.PREVIEW) {
            CameraManager.get().requestCapture(this, R.id.take_picture_success);
        }
    }

    private enum State {
        PREVIEW, SUCCESS, DONE
    }
}
