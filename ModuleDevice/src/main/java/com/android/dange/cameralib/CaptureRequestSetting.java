package com.android.dange.cameralib;

import android.graphics.Rect;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.MeteringRectangle;

import com.android.dange.moduleinterface.Info.Constant;

public class CaptureRequestSetting {
    public static Integer sLockSetParameters = new Integer(0);

    public static boolean setSceneMode(CaptureRequest.Builder builder, int scene_mode) {
        synchronized (sLockSetParameters) {
            if (builder.get(CaptureRequest.CONTROL_SCENE_MODE) != null || builder.get(CaptureRequest.CONTROL_SCENE_MODE) != scene_mode) {
                if (scene_mode == CameraMetadata.CONTROL_SCENE_MODE_DISABLED) {
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
                } else {
                    builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_USE_SCENE_MODE);
                }
                builder.set(CaptureRequest.CONTROL_SCENE_MODE, scene_mode);
                return true;
            }
        }
        return false;
    }

    public static boolean setColorEffect(CaptureRequest.Builder builder, int color_effect) {
        synchronized (sLockSetParameters) {
            if (builder.get(CaptureRequest.CONTROL_EFFECT_MODE) != null || builder.get(CaptureRequest.CONTROL_EFFECT_MODE) != color_effect) {
                builder.set(CaptureRequest.CONTROL_EFFECT_MODE, color_effect);
                return true;
            }
            return false;
        }
    }

    public static boolean setWhiteBalance(CaptureRequest.Builder builder, int white_balance) {
        if (builder.get(CaptureRequest.CONTROL_AWB_MODE) == null || builder.get(CaptureRequest.CONTROL_AWB_MODE) != white_balance) {
            builder.set(CaptureRequest.CONTROL_AWB_MODE, white_balance);
            return true;
        }
        return false;
    }

    private static boolean setAntiBanding(CaptureRequest.Builder builder, int antibanding) {
        if (builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) == null || builder.get(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE) != antibanding) {
            builder.set(CaptureRequest.CONTROL_AE_ANTIBANDING_MODE, antibanding);
            return true;
        }
        return false;
    }

    public static boolean setAEMode(CaptureRequest.Builder builder, String flash_value) {
        switch (flash_value) {
            case Constant.FLASH_MODE_OFF:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case Constant.FLASH_MODE_AUTO:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_AUTO_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case Constant.FLASH_MODE_ON:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON_ALWAYS_FLASH);
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_OFF);
                break;
            case Constant.FLASH_MODE_TORCH:
                builder.set(CaptureRequest.CONTROL_AE_MODE, CameraMetadata.CONTROL_AE_MODE_ON);
                builder.set(CaptureRequest.FLASH_MODE, CameraMetadata.FLASH_MODE_TORCH);
                break;
        }
        return true;
    }

    /**
     * for zoom.
     * */
    public static void setCropRegion(CaptureRequest.Builder builder, Rect crop_region) {
        builder.set(CaptureRequest.SCALER_CROP_REGION, crop_region);
    }

    /**
     * exposure compensation.
     * */
    public static boolean setExposureCompensation(CaptureRequest.Builder builder, int ae_exposure_compensation) {
        if (builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION) == null
                || ae_exposure_compensation != builder.get(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION)) {
            builder.set(CaptureRequest.CONTROL_AE_EXPOSURE_COMPENSATION, ae_exposure_compensation);
            return true;
        }

        return false;
    }

    public static void setAFMode(CaptureRequest.Builder builder, String af_mode) {
        int focus_mode;

        switch (af_mode) {
            case "auto":
            case "locked":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_AUTO;
                break;
            case "infinity":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_OFF;
                break;
            case "manual":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_OFF;
                break;
            case "macro":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_MACRO;
                break;
            case "edof":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_EDOF;
                break;
            case "continuous-picture":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE;
                break;
            case "continuous_video":
                focus_mode = CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_VIDEO;
                break;
            default:
                return;
        }
    }

    /**
     * for manual focus.
     * */
    public static void setFocusDistance(CaptureRequest.Builder builder, float focus_distance) {
        builder.set(CaptureRequest.LENS_FOCUS_DISTANCE, focus_distance);
    }

    public static void setAutoExposureLock(CaptureRequest.Builder builder, boolean ae_lock) {
        builder.set(CaptureRequest.CONTROL_AE_LOCK, ae_lock);
    }

    public static void setAFRegions(CaptureRequest.Builder builder, MeteringRectangle[] af_regions, CameraCharacteristics characteristics) {
        if (af_regions != null && characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AF) > 0) {
            builder.set(CaptureRequest.CONTROL_AF_REGIONS, af_regions);
        }
    }

    public static void setAERegions(CaptureRequest.Builder builder, MeteringRectangle[] ae_regions, CameraCharacteristics characteristics) {
        if (ae_regions != null && characteristics.get(CameraCharacteristics.CONTROL_MAX_REGIONS_AE) > 0) {
            builder.set(CaptureRequest.CONTROL_AE_REGIONS, ae_regions);
        }
    }

    public static void setFaceDetectMode(CaptureRequest.Builder builder, int face_detect_mode) {
        builder.set(CaptureRequest.STATISTICS_FACE_DETECT_MODE, face_detect_mode);
    }
}
