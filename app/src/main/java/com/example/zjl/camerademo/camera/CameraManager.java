package com.example.zjl.camerademo.camera;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by zjl on 18-1-3.
 * 管理Camera的各种状态
 */

public class CameraManager {

    private static final String TAG = CameraManager.class.getName();
    private static CameraManager sCameraManager;
    private final CameraConfigurationManager mConfigManager;

    //相机预览回调，预览的帧数据会被分发到这里，由注册的handler传递帧数据
    private PreviewCallback mPreviewCallback;
    //自动对焦回调，由注册的handler传递对焦回调信息
    private AutoFocusCallback mAutoFocusCallback;
    //拍照回调
    private PictureCallback mPictureCallback;
    private Camera mCamera;
    private boolean mInitialized;
    private boolean mPreviewing;

    private CameraManager() {
        this.mConfigManager = new CameraConfigurationManager();
        mPreviewCallback = new PreviewCallback(mConfigManager);
        mAutoFocusCallback = new AutoFocusCallback();
        mPictureCallback = new PictureCallback();
    }

    /**
     * Initializes this static object with the Context of the calling Activity.
     */
    public static void init() {
        if (sCameraManager == null) {
            sCameraManager = new CameraManager();
        }
    }

    /**
     * Gets the CameraManager singleton instance.
     *
     * @return A reference to the CameraManager singleton.
     */
    public static CameraManager get() {
        return sCameraManager;
    }

    /**
     * Opens the mCamera driver and initializes the hardware parameters.
     *
     * @param holder The surface object which the mCamera will draw preview frames into.
     * @throws IOException Indicates the mCamera driver failed to open.
     */
    public boolean openDriver(SurfaceHolder holder) throws IOException {
        if (mCamera == null) {
            try {
                mCamera = Camera.open();
                if (mCamera != null) {
                    // setParameters 是针对魅族MX5做的。MX5通过Camera.open()拿到的Camera 对象不为null
                    Camera.Parameters parameters = mCamera.getParameters();
                    mCamera.setParameters(parameters);
                    mCamera.setPreviewDisplay(holder);
                    if (!mInitialized) {
                        mInitialized = true;
                        //初始化相机参数
                        mConfigManager.initFromCameraParameters(mCamera);
                    }
                    mConfigManager.setDesiredCameraParameters(mCamera);
                    return true;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Closes the camera driver if still in use.
     */
    public boolean closeDriver() {
        if (mCamera != null) {
            try {
                Log.d(TAG, "......closeDriver......");
                mCamera.release();
                mInitialized = false;
                mPreviewing = false;
                mCamera = null;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;

    }

    /**
     * 打开或关闭闪光灯
     *
     * @param open 控制是否打开
     * @return 打开或关闭失败，则返回false。
     */
    public boolean setFlashLight(boolean open) {
        if (mCamera == null || !mPreviewing) {
            return false;
        }
        Camera.Parameters parameters = mCamera.getParameters();
        if (parameters == null) {
            return false;
        }
        List<String> flashModes = parameters.getSupportedFlashModes();
        // Check if camera flash exists
        if (null == flashModes || 0 == flashModes.size()) {
            // Use the screen as a flashlight (next best thing)
            return false;
        }
        String flashMode = parameters.getFlashMode();
        if (open) {
            if (Camera.Parameters.FLASH_MODE_TORCH.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_TORCH)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        } else {
            if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)) {
                return true;
            }
            // Turn on the flash
            if (flashModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
                return true;
            } else {
                return false;
            }
        }
    }

    /**
     * Asks the mCamera hardware to begin drawing preview frames to the screen.
     */
    public boolean startPreview() {
        if (mCamera != null && !mPreviewing) {
            try {
                mCamera.startPreview();
                mPreviewing = true;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Tells the mCamera to stop drawing preview frames.
     */
    public boolean stopPreview() {
        if (mCamera != null && mPreviewing) {
            try {
                // 停止预览时把callback移除.
                mCamera.setOneShotPreviewCallback(null);
                mCamera.stopPreview();
                mPreviewCallback.setHandler(null, 0);
                mAutoFocusCallback.setHandler(null, 0);
                mPictureCallback.setHandler(null, 0);
                mPreviewing = false;
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * 获取预览帧数据
     * A single preview frame will be returned to the handler supplied. The data will arrive as byte[] in the
     * message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively.
     *
     * @param handler The handler to send the message to.
     * @param message The what field of the message to be sent.
     */
    public void requestPreviewFrame(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mPreviewCallback.setHandler(handler, message);
            //这里设置的PreviewCallback回调只会执行一次，设置了这个回调之后，下次预览帧数据再显示在屏幕时，
            //就会触发PreviewCallback的回调方法，然后该callback就会被移除，便于调用者控制何时获取帧数据
            mCamera.setOneShotPreviewCallback(mPreviewCallback);
        }
    }

    /**
     * Asks the mCamera hardware to perform an autofocus.
     *
     * @param handler The Handler to notify when the autofocus completes.
     * @param message The message to deliver.
     */
    public void requestAutoFocus(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mAutoFocusCallback.setHandler(handler, message);
            // Log.d(TAG, "Requesting auto-focus callback");
            try {
                mCamera.autoFocus(mAutoFocusCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拍照
     * @param handler 由handler来传递拍照返回的数据
     */
    public void requestCapture(Handler handler, int message) {
        if (mCamera != null && mPreviewing) {
            mPictureCallback.setHandler(handler, message);
            mCamera.takePicture(new Camera.ShutterCallback() {
                public void onShutter() {
                    Log.d("CameraManager", "onShutter");
                }
            }, null, mPictureCallback);
            //调用takePicture方法之后，Camera会停止预览，设置预览标志位mPreviewing为false。
            mPreviewing = false;
        }
    }
}
