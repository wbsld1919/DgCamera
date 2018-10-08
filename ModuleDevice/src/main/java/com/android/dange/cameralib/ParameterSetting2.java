package com.android.dange.cameralib;

import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;

public class ParameterSetting2 {
    private static final ParameterSetting2 ourInstance = new ParameterSetting2();

    public static ParameterSetting2 getInstance() {
        return ourInstance;
    }

    private ParameterSetting2() {
    }

    public void firstParameterSettingAfterOpen(String mUuid, CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_AF_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
    }

    public void updateAllParameters(String mUuid, CaptureRequest.Builder builder) {
    }

    public boolean updateParameters(String parameterType, String value, CaptureRequest.Builder builder) {
/*        if (parameterType.equalsIgnoreCase(ParameterInfo.KEY_FLASH)) {
            return CaptureRequestSetting.setAEMode(builder, value);
        }
        if (parameterType.equalsIgnoreCase(ParameterInfo.KEY_STATIC_FILTER)) {
            return CaptureRequestSetting.setColorEffect(builder, ParameterTransformUtil.staticFilterString2Index(value));
        }*/
        return false;
    }
}
