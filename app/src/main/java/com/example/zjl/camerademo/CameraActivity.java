package com.example.zjl.camerademo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.zjl.camerademo.camera.CameraManager;
import com.example.zjl.camerademo.camera.CaptureHandler;

import java.io.IOException;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback{
    private static final String TAG = "CameraActivity";
    private static final int REQUEST_PERMISSIONS = 1;

    private SurfaceView mSurfaceView;
    private FrameLayout mContainer;
    private ImageButton takePictureBtn;
    private CaptureHandler mCaptureHandler;
    private boolean mHasSurface;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        turnonFullscreen();
        setContentView(R.layout.activity_camera);
        initViews();
        initData();
    }

    private void turnonFullscreen() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        int flag = WindowManager.LayoutParams.FLAG_FULLSCREEN;
        Window myWindow = this.getWindow();
        myWindow.setFlags(flag, flag);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //清除透明状态栏的灰色阴影,且设置为亮色主题
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        getWindow().setStatusBarColor(Color.TRANSPARENT);
    }

    private void initViews() {
        mHasSurface = false;
        mContainer = (FrameLayout) findViewById(R.id.container);
        takePictureBtn = (ImageButton) findViewById(R.id.take_pic_btn);
        takePictureBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 18-1-2 拍照
                if (mCaptureHandler != null) {
                    mCaptureHandler.startCapture();
                }
            }
        });
    }

    private void initData() {
        CameraManager.get().init();
    }


    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //初始化Camera
            initCamera();
        } else {
            ActivityCompat.requestPermissions(this, new String[] { Manifest.permission.CAMERA }, REQUEST_PERMISSIONS);
        }
    }

    private void initCamera() {
        if (mSurfaceView == null) {
            mSurfaceView = new SurfaceView(this);
            mSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mContainer.addView(mSurfaceView, 0);
        }
        mSurfaceView.setVisibility(View.VISIBLE);
        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        Surface surface = surfaceHolder.getSurface();
        boolean isValid = surface == null ? false:surface.isValid();
        if (mHasSurface && isValid) {
            initCamera(surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            //设置Surface的type，该参数表示，由Camera为Surface提供帧数据。
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
    }

    private void initCamera(SurfaceHolder surfaceHolder) {
        try {
            //1. 连接相机
            if (!CameraManager.get().openDriver(surfaceHolder)) {
                Log.d(TAG, "Camera被占用，可能是上次Camera没有被释放");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (mCaptureHandler == null) {
            mCaptureHandler = new CaptureHandler(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //断开Camera连接
        if (mCaptureHandler != null) {
            try {
                //停止预览
                mCaptureHandler.quitSynchronously();
                mCaptureHandler = null;//下次进入重新初始化
                //释放相机
                CameraManager.get().closeDriver();
            } catch (Exception e) {
                //关闭摄像头失败情况下，最好退出Activity，否则下次初始化相机的时候，会提示相机占用
                finish();
            }

        }
    }

    public void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.e(TAG, "surface created...holder=" + holder);
        mHasSurface = true;
        initCamera(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.e(TAG, "surface destroyed...holder=" + holder);
        mHasSurface = false;
    }
}
