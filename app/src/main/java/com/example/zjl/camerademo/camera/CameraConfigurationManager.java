package com.example.zjl.camerademo.camera;

import android.hardware.Camera;
import android.util.Log;

import com.example.zjl.camerademo.utils.ScreenUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by zjl on 18-1-3.
 * Camera配置信息管理，如预览配置
 */

public class CameraConfigurationManager {
    private static final String TAG = CameraConfigurationManager.class.getName();
    private static final int TEN_DESIRED_ZOOM = 10;
    private static final int DESIRED_SHARPNESS = 30;

    private static final Pattern COMMA_PATTERN = Pattern.compile(",");

    private Camera.Size mCameraResolution;//预览尺寸
    private Camera.Size mPictureResolution;//画面尺寸

    private static int findBestMotZoomValue(CharSequence stringValues, int tenDesiredZoom) {
        int tenBestValue = 0;
        for (String stringValue : COMMA_PATTERN.split(stringValues)) {
            stringValue = stringValue.trim();
            double value;
            try {
                value = Double.parseDouble(stringValue);
            } catch (NumberFormatException nfe) {
                return tenDesiredZoom;
            }
            int tenValue = (int) (10.0 * value);
            if (Math.abs(tenDesiredZoom - value) < Math.abs(tenDesiredZoom - tenBestValue)) {
                tenBestValue = tenValue;
            }
        }
        return tenBestValue;
    }

    //初始化相机参数
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        mCameraResolution = findCloselySize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                parameters.getSupportedPreviewSizes());
        Log.e(TAG, "Setting preview size: " + mCameraResolution.width + "-" + mCameraResolution.height);
        mPictureResolution = findCloselySize(ScreenUtils.getScreenWidth(), ScreenUtils.getScreenHeight(),
                parameters.getSupportedPictureSizes());
        Log.e(TAG, "Setting picture size: " + mPictureResolution.width + "-" + mPictureResolution.height);
    }

    /**
     * 设置相机参数
     * @param camera
     */
    public void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        parameters.setPreviewSize(mCameraResolution.width, mCameraResolution.height);
        parameters.setPictureSize(mPictureResolution.width, mPictureResolution.height);
        setZoom(parameters);
        camera.setDisplayOrientation(90);
        camera.setParameters(parameters);
    }

    Camera.Size getCameraResolution() {
        return mCameraResolution;
    }

    private void setZoom(Camera.Parameters parameters) {

        String zoomSupportedString = parameters.get("zoom-supported");
        if (zoomSupportedString != null && !Boolean.parseBoolean(zoomSupportedString)) {
            return;
        }

        int tenDesiredZoom = TEN_DESIRED_ZOOM;

        String maxZoomString = parameters.get("max-zoom");
        if (maxZoomString != null) {
            try {
                int tenMaxZoom = (int) (10.0 * Double.parseDouble(maxZoomString));
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Bad max-zoom: " + maxZoomString);
            }
        }

        String takingPictureZoomMaxString = parameters.get("taking-picture-zoom-max");
        if (takingPictureZoomMaxString != null) {
            try {
                int tenMaxZoom = Integer.parseInt(takingPictureZoomMaxString);
                if (tenDesiredZoom > tenMaxZoom) {
                    tenDesiredZoom = tenMaxZoom;
                }
            } catch (NumberFormatException nfe) {
                Log.e(TAG, "Bad taking-picture-zoom-max: " + takingPictureZoomMaxString);
            }
        }

        String motZoomValuesString = parameters.get("mot-zoom-values");
        if (motZoomValuesString != null) {
            tenDesiredZoom = findBestMotZoomValue(motZoomValuesString, tenDesiredZoom);
        }

        String motZoomStepString = parameters.get("mot-zoom-step");
        if (motZoomStepString != null) {
            try {
                double motZoomStep = Double.parseDouble(motZoomStepString.trim());
                int tenZoomStep = (int) (10.0 * motZoomStep);
                if (tenZoomStep > 1) {
                    tenDesiredZoom -= tenDesiredZoom % tenZoomStep;
                }
            } catch (NumberFormatException nfe) {
                // continue
            }
        }

        // Set zoom. This helps encourage the user to pull back.
        // Some devices like the Behold have a zoom parameter
        // if (maxZoomString != null || motZoomValuesString != null) {
        // parameters.set("zoom", String.valueOf(tenDesiredZoom / 10.0));
        // }
        if (parameters.isZoomSupported()) {
            Log.e(TAG, "max-zoom:" + parameters.getMaxZoom());
            parameters.setZoom(parameters.getMaxZoom() / 10);
        } else {
            Log.e(TAG, "Unsupported zoom.");
        }

        // Most devices, like the Hero, appear to expose this zoom parameter.
        // It takes on values like "27" which appears to mean 2.7x zoom
        // if (takingPictureZoomMaxString != null) {
        // parameters.set("taking-picture-zoom", tenDesiredZoom);
        // }
    }

    /**
     * 通过对比得到与画面宽高比最接近的尺寸（如果有相同尺寸，优先选择），避免预览画面被拉伸或压缩
     *
     * @param surfaceWidth 需要被进行对比的原宽
     * @param surfaceHeight 需要被进行对比的原高
     * @param preSizeList 需要对比的预览尺寸列表
     * @return 得到与原宽高比例最接近的尺寸
     */
    private Camera.Size findCloselySize(int surfaceWidth, int surfaceHeight, List<Camera.Size> preSizeList) {
        //对备选预览尺寸列表，以SizeComparator的规范进行排序
        Collections.sort(preSizeList, new SizeComparator(surfaceWidth, surfaceHeight));
        return preSizeList.get(0);
    }

    /**
     * 预览尺寸与给定的宽高尺寸比较器。首先比较宽高的比例，在宽高比相同的情况下，根据宽和高的最小差进行比较。
     */
    private static class  SizeComparator implements Comparator<Camera.Size> {

        private final int width;
        private final int height;
        private final float ratio;

        SizeComparator(int width, int height) {
            //保证宽大于高
            if (width < height) {
                this.width = height;
                this.height = width;
            } else {
                this.width = width;
                this.height = height;
            }
            this.ratio = (float) this.height / this.width;
        }

        @Override
        public int compare(Camera.Size size1, Camera.Size size2) {
            int width1 = size1.width;
            int height1 = size1.height;
            int width2 = size2.width;
            int height2 = size2.height;

            //计算两个size的宽高比，与给定 尺寸宽高比最接近的排序在前
            float ratio1 = Math.abs((float) height1 / width1 - ratio);
            float ratio2 = Math.abs((float) height2 / width2 - ratio);
            int result = Float.compare(ratio1, ratio2);
            if (result != 0) {
                return result;
            } else {
                int minGap1 = Math.abs(width - width1) + Math.abs(height - height1);
                int minGap2 = Math.abs(width - width2) + Math.abs(height - height2);
                return minGap1 - minGap2;
            }
        }
    }


}
