package com.android.dange.cameralib;

import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;

import com.android.dange.modulecommon.Util.InterfaceUtil;

/**
 * CaptureCallback for preview.
 *
 * @author dd-dange
 * @date 2018.10.03
 * */
public class PreviewCaptureCallback extends CameraCaptureSession.CaptureCallback {
    private static final String TAG = "PreviewCaptureCallback";

    private InterfaceUtil.AutoFocusCallback mFocusCallback;
    private InterfaceUtil.AutoFocusMoveCallback mContinousFocusCallback;

    private int mLastState = -1;

    public PreviewCaptureCallback() {
    }

    public void setAutoFocusCallback(InterfaceUtil.AutoFocusCallback cb) {
        mFocusCallback = cb;
    }

    public void setContinousFocusCallback(InterfaceUtil.AutoFocusMoveCallback cb) {
        mContinousFocusCallback = cb;
    }

    @Override
    public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
        super.onCaptureCompleted(session, request, result);

        // below is for auto focus.
        if (mFocusCallback != null) {
            final Integer mAFState = result.get(CaptureResult.CONTROL_AF_STATE);

            if (mAFState != mLastState) {
                if (mAFState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED
                        || mAFState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED) {
                    boolean focusSucceed = (mAFState == CaptureResult.CONTROL_AF_STATE_FOCUSED_LOCKED)
                            || (mAFState == CaptureResult.CONTROL_AF_STATE_NOT_FOCUSED_LOCKED);
                    mFocusCallback.onAutoFocus(focusSucceed);
                }
            }

            if (mAFState != null && mAFState != mLastState) {
                mLastState = mAFState;
            }
        }

        // below is for continous focus.
        if (mContinousFocusCallback != null) {
            final Integer mAFState = result.get(CaptureResult.CONTROL_AF_STATE);

            if (mAFState != mLastState) {
                if (mAFState == CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN
                        || mAFState == CaptureResult.CONTROL_AF_STATE_PASSIVE_FOCUSED) {
                    boolean focusStarted = (mAFState == CaptureResult.CONTROL_AF_STATE_PASSIVE_SCAN);
                    mContinousFocusCallback.onAutoFocusMoving(focusStarted);
                }
            }

            if (mAFState != null && mAFState != mLastState) {
                mLastState = mAFState;
            }
        }
    }
}
