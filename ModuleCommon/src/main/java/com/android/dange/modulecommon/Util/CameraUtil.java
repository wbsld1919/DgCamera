package com.android.dange.modulecommon.Util;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CaptureRequest;

public class CameraUtil {

    public static Rect getViewableRect(CaptureRequest.Builder previewBuilder, CameraCharacteristics characteristics) {
        Rect cropRect;
        if (previewBuilder != null) {
            cropRect = previewBuilder.get(CaptureRequest.SCALER_CROP_REGION);
            if (cropRect != null) {
                return cropRect;
            }
        }
        cropRect = characteristics.get(CameraCharacteristics.SENSOR_INFO_ACTIVE_ARRAY_SIZE);
        cropRect.left = 0;
        cropRect.top = 0;
        cropRect.right -= cropRect.left;
        cropRect.bottom -= cropRect.top;
        return cropRect;
    }
}
