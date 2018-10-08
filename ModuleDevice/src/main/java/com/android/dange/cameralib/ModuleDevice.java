package com.android.dange.cameralib;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Size;

import com.android.dange.cameralib.manager.CameraController;
import com.android.dange.modulecommon.Util.InterfaceUtil;
import com.android.dange.modulecommon.Util.LogUtil;
import com.android.dange.moduleinterface.ModuleDeviceBase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ModuleDevice implements ModuleDeviceBase {

    private ExecutorService cachedThreadExecutor = Executors.newCachedThreadPool();

    private String mUuid;
    private CameraController mCameraController;

    @Override
    public void init(String uuid) {
        mUuid = uuid;
        mCameraController = CameraController.getInstance();
    }

    @Override
    public void unInit() {

    }

    @Override
    public void open() {
        cachedThreadExecutor.execute(new Runnable() {
            @Override
            public void run() {
                LogUtil.v("begin to open camera");
                mCameraController.open(mUuid, 0 + "");
            }
        });
    }

    @Override
    public void startPreview() {

    }

    @Override
    public void stopPreview() {

    }

    @Override
    public void release() {
        if (mCameraController != null) {
            mCameraController.release();
        }
    }

    @Override
    public void switchCamera() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void refreshSize() {

    }

    @Override
    public void takePicture(InterfaceUtil.ImageDadaCallback imageDadaCallback, int i) {

    }

    @Override
    public void setDisplaySurfaceTexture(SurfaceTexture surfaceTexture, int w, int h) {
        if (mCameraController != null) {
            mCameraController.setSurfaceTexture(surfaceTexture, mPreviewCallback);
        }
    }

    @Override
    public void applyFocusCapabilities(boolean mAeLockSupported, boolean mAeAwbLock, boolean mAwbLockSupported, String mFocusMode, boolean mFocusAreaSupported, MeteringRectangle[] focusAreas, MeteringRectangle meterAreas) {

    }

    @Override
    public void autoFocus(InterfaceUtil.AutoFocusCallback autoFocusCallback) {

    }

    @Override
    public void cancelAutoFocus() {

    }

    @Override
    public CameraCharacteristics getCharacteristicts() {
        return null;
    }

    @Override
    public CaptureRequest.Builder getPreviewBuilder() {
        return null;
    }

    @Override
    public Size getPictureSize() {
        return null;
    }

    @Override
    public Size getPreviewSize() {
        return null;
    }

    @Override
    public Rect getViewableRect() {
        return null;
    }

    @Override
    public void setAutoFocusMoveCallback(InterfaceUtil.AutoFocusMoveCallback autoFocusMoveCallback) {

    }

    @Override
    public void startFaceDetection() {

    }

    @Override
    public void stopFaceDetection() {

    }

    @Override
    public void updateAllParameters() {

    }

    @Override
    public void updateParameters(String paraType, String value) {

    }

    @Override
    public void updatePreviewPictureSize() {

    }

    @Override
    public String getUuid() {
        return null;
    }

    private PreviewCaptureCallback mPreviewCallback = new PreviewCaptureCallback() {
        @Override
        public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };
}
