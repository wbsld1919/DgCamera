package com.android.dange.moduleinterface;

import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;
import android.util.Size;

import com.android.dange.modulecommon.Util.InterfaceUtil;

public interface ModuleDeviceBase extends ModuleBase {
    void open();

    void startPreview();

    void stopPreview();

    void release();

    void switchCamera();

    void onDestroy();

    void onPause();

    void refreshSize();

    void takePicture(InterfaceUtil.ImageDadaCallback imageDadaCallback, int i);

    void setDisplaySurfaceTexture(SurfaceTexture surfaceTexture, int w, int h);

    void applyFocusCapabilities(boolean mAeLockSupported, boolean mAeAwbLock, boolean mAwbLockSupported, String mFocusMode,
                                       boolean mFocusAreaSupported, MeteringRectangle[] focusAreas, MeteringRectangle meterAreas);

    void autoFocus(InterfaceUtil.AutoFocusCallback autoFocusCallback);

    void cancelAutoFocus();

    CameraCharacteristics getCharacteristicts();

    CaptureRequest.Builder getPreviewBuilder();

    Size getPictureSize();

    Size getPreviewSize();

    Rect getViewableRect();

    void setAutoFocusMoveCallback(InterfaceUtil.AutoFocusMoveCallback autoFocusMoveCallback);

    void startFaceDetection();

    void stopFaceDetection();

    void updateAllParameters();

    void updateParameters(String paraType, String value);

    void updatePreviewPictureSize();
}
