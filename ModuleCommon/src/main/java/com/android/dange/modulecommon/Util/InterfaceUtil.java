package com.android.dange.modulecommon.Util;

public class InterfaceUtil {

    public interface AutoFocusCallback {
        void onAutoFocus(boolean focus);
    }

    public interface AutoFocusMoveCallback {
        void onAutoFocusMoving(boolean moving);
    }

    public interface ImageDadaCallback {
        void onPictureTaken(byte[] datas);
    }
}
