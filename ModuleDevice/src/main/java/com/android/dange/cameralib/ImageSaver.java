package com.android.dange.cameralib;

import android.media.Image;

import com.android.dange.modulecommon.Util.InterfaceUtil;

import java.nio.ByteBuffer;

/**
 * Transform the data from {@link Image} as byte[].
 * */
public class ImageSaver implements Runnable {

    /**
     * The jpeg image
     * */
    private final Image mImage;

    private InterfaceUtil.ImageDadaCallback mDataCallback;
    public ImageSaver(Image image, InterfaceUtil.ImageDadaCallback callback) {
        mImage = image;
        mDataCallback = callback;
    }

    @Override
    public void run() {
        if (mImage == null) {
            return;
        }

        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];

        if(mDataCallback != null) {
            mDataCallback.onPictureTaken(bytes);
        }

        mImage.close();
    }
}
