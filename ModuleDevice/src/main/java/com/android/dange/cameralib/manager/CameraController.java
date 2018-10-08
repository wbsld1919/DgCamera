package com.android.dange.cameralib.manager;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.MeteringRectangle;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.Size;
import android.view.Surface;

import com.android.dange.cameralib.CaptureRequestSetting;
import com.android.dange.cameralib.ImageSaver;
import com.android.dange.cameralib.ParameterSetting2;
import com.android.dange.cameralib.PreviewCaptureCallback;
import com.android.dange.modulecommon.Util.CameraUtil;
import com.android.dange.modulecommon.Util.InterfaceUtil;
import com.android.dange.modulecommon.Util.LogUtil;
import com.android.dange.moduleinterface.ApplicationManager;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * CameraContoller for camera api2.
 *
 * @author dd-dange
 * @date 2018.10.03
 * */
public class CameraController {

    private static final String TAG = "CameraController";

    /**
     * lock to wait for cmera to be opened from {@link CameraDevice.StateCallback}
     * */
    private final Object mOpenCameraLock = new Object();
    private final Object mCreateCaptureSessionLock = new Object();
    private final Object mPreviewBuilderLock = new Object();

    private Context mContext = ApplicationManager.getInstance().getApplicationBase().getContext();

    private String mUuid;
    private String mCameraId;

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;

    private CameraManager mCameraManager;
    private CameraDevice mCameraDevice;
    private CameraCharacteristics mCharacteristics;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewBuilder;

    private PreviewCaptureCallback mPreviewCallback;
    private ExecutorService cachedThreadExecutor = Executors.newCachedThreadPool();

    private SurfaceTexture mPreviewSurfaceTexture;
    private Surface mPreviewSurface;

    private int mPreviewWidth;
    private int mPreviewHeight;

    private int mPictureWidth;
    private int mPictureHeight;

    private volatile boolean mFirstParameterSet;

    private InterfaceUtil.ImageDadaCallback mDataCallback;

    /**
     * An {@link android.media.ImageReader} that handles still image capture.
     * */
    private ImageReader mImageReader;

    private static final CameraController ourInstance = new CameraController();

    public static CameraController getInstance() {
        return ourInstance;
    }

    private CameraController() {
    }

    /**
     * This is a callback object for the {@link ImageReader}.
     * "onImageAvailable" will be called when
     *   1. A still image is ready to be saved.
     *   2. Preview data would be obtained to do sth else.
     */
    private final ImageReader.OnImageAvailableListener mOnImageAvailableListener = new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            mBackgroundHandler.post(new ImageSaver(reader.acquireNextImage(), mDataCallback));
        }
    };

    /**
     * CaptureCallback for taking picture. after onImageAvailable in {@link ImageReader.OnImageAvailableListener}.
     * "onCaptureCompleted" will be called.
     * */
    private CameraCaptureSession.CaptureCallback captureCallback
            = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            startPreview();
        }
    };

    /**
     * {@link CameraDevice.StateCallback} is called when {@link CameraDevice} changes its state.
     * */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {

            LogUtil.v("camera is opened");

            // this is called when the camera is opened.
            try {
                mCameraDevice = cameraDevice;

                mCharacteristics = mCameraManager.getCameraCharacteristics(mCameraId);

                // note, this won't start the preview yet, but we create
                // the previewBuilder in order to start setting camera patameters.
                synchronized (mPreviewBuilderLock) {
                    if (mPreviewBuilder == null) {
                        createPreviewRequest();
                    }
                }

                // if any parameter should be set before preview start.
                // try to do it here.
                ParameterSetting2.getInstance().firstParameterSettingAfterOpen(mUuid, mPreviewBuilder);
                updateCameraSize();

                mFirstParameterSet = true;

            } catch (CameraAccessException e) {
                e.printStackTrace();
            }

            synchronized (mOpenCameraLock) {
                mOpenCameraLock.notifyAll();
            }

            // Two situations:
            // 1. when onPause and onResume right now, onSurfaceTextureAvailable in CameraTextureView may not run
            // and we should reuse the surfaceTexture.
            // 2. when setSurfaceTexture come before firstParameter is set, and we should startpreview after
            // first parameter is set.
            if (mPreviewSurfaceTexture != null && mPreviewCallback != null) {
                cachedThreadExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        startPreview();
                    }
                });
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice camera) {
            // TODO
        }

        @Override
        public void onError(@NonNull CameraDevice camera, int error) {
            // TODO
        }
    };

    /**
     * {@link CameraCaptureSession.}
     * */
    private final CameraCaptureSession.StateCallback captureSessionStateCallback = new CameraCaptureSession.StateCallback() {

        @Override
        public void onConfigured(@NonNull CameraCaptureSession session) {
            LogUtil.v("CameraCaptureSession is configured");
            if (mCameraDevice == null) {
                synchronized (mCreateCaptureSessionLock) {
                    mCreateCaptureSessionLock.notifyAll();
                }
                return;
            }

            mCaptureSession = session;
            Surface surface = mPreviewSurface;
            mPreviewBuilder.addTarget(surface);

            setRepeatingRequest();

            synchronized (mCreateCaptureSessionLock) {
                mCreateCaptureSessionLock.notifyAll();
            }
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
            synchronized (mCreateCaptureSessionLock) {
                mCreateCaptureSessionLock.notifyAll();
            }
        }
    };

    /**
     * uuid should set each time when opening camera.
     * because CameraCotroller is a still class.
     * */
    public void open(String uuid, String cameraid) {
        mUuid = uuid;

        mCameraId = cameraid;

        mFirstParameterSet = false;

        startBackgroundThreadIfNeed();

        mCameraManager = (CameraManager) mContext.getSystemService(Context.CAMERA_SERVICE);

        try {
            mCameraManager.openCamera(mCameraId, mStateCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        catch (SecurityException e) {
            e.printStackTrace();
        }
        catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

        // need to wait until camera is opened.
        synchronized (mOpenCameraLock) {
            try {
                // wait util mStateCallback calls notifyAll().
                mOpenCameraLock.wait();
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void createPreviewRequest() {
        if (mCameraDevice == null) {
            return;
        }

        try {
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThreadIfNeed() {
        if (mBackgroundHandler == null) {
            mBackgroundThread = new HandlerThread("CameraBackground");
            mBackgroundThread.start();
            mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
        }
    }

    /**
     * preview and picture share the same Capture session.
     * */
    private void createCaptureSession() {
        if (mPreviewBuilder == null) {
            throw new RuntimeException();
        }

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if (mPreviewSurfaceTexture != null) {
            if (mPreviewWidth != 0 && mPreviewHeight != 0) {
                // need to set the texture size.
                // just as Parameter.setPreviewSize.
                mPreviewSurfaceTexture.setDefaultBufferSize(mPreviewWidth, mPreviewHeight);
            }

            if (mPictureWidth != 0 && mPictureHeight != 0) {
                createPictureImageReader();
            }

            // also need to create a new surface for the texture, in case the size has changed
            if (mPreviewSurface != null) {
                mPreviewBuilder.removeTarget(mPreviewSurface);
            }

            mPreviewSurface = new Surface(mPreviewSurfaceTexture);

            // surfaces need to be filled with data.
            List<Surface> surfaces = Arrays.asList(mPreviewSurface, mImageReader.getSurface());

            try {
                mCameraDevice.createCaptureSession(surfaces, captureSessionStateCallback, mBackgroundHandler);
            }
            catch (CameraAccessException e) {
                e.printStackTrace();
            }

            // need to wait until CaptureSession is created
            synchronized (mCreateCaptureSessionLock) {
                try {
                    // wait util capturesessionStateCallback calls notifyAll()
                    mCreateCaptureSessionLock.wait();
                }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * set the repeating request.
     * apply the setting for each frame.
     * */
    private void setRepeatingRequest() {
        LogUtil.v("setRepeatingRequest");
        try {
            mCaptureSession.setRepeatingRequest(mPreviewBuilder.build(), mPreviewCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * only capture a frame.
     * notify the difference from {@link CameraController#setRepeatingRequest()}
     * */
    private void capture(CaptureRequest request, PreviewCaptureCallback captureCallback) {
        if (mCameraDevice == null || mCaptureSession == null) {
            return;
        }

        try {
            mCaptureSession.capture(request, captureCallback, this.mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void setSurfaceTexture(SurfaceTexture surface, PreviewCaptureCallback captureCallback) {
        LogUtil.v("setSurfaceTexture surface = " + surface + ", mFirstParameterSet = " + mFirstParameterSet);
        mPreviewSurfaceTexture = surface;
        mPreviewCallback = captureCallback;
        if (surface == null || captureCallback == null || !mFirstParameterSet) {
            return;
        }
        startPreview();
    }

    public void startPreview() {
        LogUtil.v("startPreview");
        if (mCameraDevice == null) {
            return;
        }

        synchronized (mPreviewBuilderLock) {
            if (mPreviewBuilder == null) {
                createPreviewRequest();
            }
        }

        if (mCaptureSession != null) {
            setRepeatingRequest();
            return;
        }

        createCaptureSession();
    }

    public void stopPreview() {
        mFirstParameterSet = false;
        if (mCameraDevice == null || mCaptureSession == null) {
            return;
        }

        try {
            try {
                mCaptureSession.stopRepeating();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                // got this as a google play exception
                // we still call close() below, as i has no effect if captureSession is already closed.
            }
            // although stopRepeating() alone will pause the preview, seems better to close captureSession altogether
            // this allows the app to make changes such as changing the picture size
            mCaptureSession.close();
            mCaptureSession = null;
        }
        catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    public void release() {
        mFirstParameterSet = false;

        if (mCaptureSession != null) {
            mCaptureSession.close();
            mCaptureSession = null;
        }

        mPreviewBuilder = null;
        mCharacteristics = null;

        if (mCameraDevice != null) {
            mCameraDevice.close();
            mCameraDevice = null;
        }

        if (mBackgroundThread != null) {
            mBackgroundThread.quitSafely();
            try {
                mBackgroundThread.join();
                mBackgroundThread = null;
                mBackgroundHandler = null;
            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void updatePreviewPictureSize() {
        updateCameraSize();
    }

    public void updateParameters(String parameterType, String value) {
        if (ParameterSetting2.getInstance().updateParameters(parameterType, value, mPreviewBuilder)) {
            setRepeatingRequest();
        }
    }

    public void updateAllParameters() {
        ParameterSetting2.getInstance().updateAllParameters(mUuid, mPreviewBuilder);
    }

    private void createPictureImageReader() {
        if (mCaptureSession != null) {
            // can only call this when captureSession not created - as the surface of the imageReader we
            // create has to match the surface we pass to the captureSession

            // throw as RuntimeException, as this is a programming error.
            throw new RuntimeException();
        }

        closePictureImageReader();

        // set picture size, just like Paramter.setPictureSize.
        mImageReader = ImageReader.newInstance(mPictureWidth, mPictureHeight, ImageFormat.JPEG, 1);

        mImageReader.setOnImageAvailableListener(mOnImageAvailableListener, mBackgroundHandler);
    }

    private void closePictureImageReader() {
        if (mImageReader != null) {
            mImageReader.close();
            mImageReader = null;
        }
    }

    public void takePicture(InterfaceUtil.ImageDadaCallback callback, int jpetRotation) {
        mDataCallback = callback;

        if (mCameraDevice == null) return;

        // CaptureRequest.Builder for taking picture.
        // which is different from previewBuilder.
        final CaptureRequest.Builder captureRequestBuilder;

        try {
            captureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);

            captureRequestBuilder.addTarget(mImageReader.getSurface());

            // in api2, parameters set for the preview are not effect for taking picture.
            // so here we should set the parameters which are the same as the preview.
            // TODO

            captureRequestBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpetRotation);
            CaptureRequest mCaptureRequest = captureRequestBuilder.build();

            // stop preview before taking picture.
            // need to stop preview before capture(as done in Camera2Basic;
            // otherwise we get bugs such as flash remaining on after taking a photo with flash)
            // but don't do this in video mode - if we're taking photos snapshots while video recording.
            mCaptureSession.stopRepeating();
            mCaptureSession.abortCaptures();

            mCaptureSession.capture(mCaptureRequest, captureCallback, mBackgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void updateCameraSize() {
        // TODO
        mPreviewWidth = 1280;
        mPreviewHeight = 960;

        mPictureWidth = 1280;
        mPictureHeight = 960;
    }

    public void cancelAutoFocus() {
        if (mPreviewBuilder == null
                || mCameraDevice == null
                || mCaptureSession == null
                || mPreviewCallback == null) {
            return;
        }

        mPreviewCallback.setAutoFocusCallback(null);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_CANCEL);
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);

        setRepeatingRequest();
    }

    public void applyFocusCapabilities(boolean mAeLockSupported, boolean mAeAwbLock, boolean mAwbLockSupported, String mFocusMode,
                                       boolean mFocusAreaSupported, MeteringRectangle[] focusAreas, MeteringRectangle[] meterAreas) {
        if (mPreviewBuilder == null || mCameraDevice == null || mPreviewCallback == null) {
            return;
        }

        CaptureRequestSetting.setAFRegions(mPreviewBuilder, focusAreas, mCharacteristics);
        CaptureRequestSetting.setAERegions(mPreviewBuilder, meterAreas, mCharacteristics);
        CaptureRequestSetting.setAFMode(mPreviewBuilder, mFocusMode);
        setRepeatingRequest();
    }

    public void autoFocus(InterfaceUtil.AutoFocusCallback cb) {
        if (mPreviewBuilder == null ||mPreviewCallback == null) {
            return;
        }

        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_START);
        mPreviewCallback.setAutoFocusCallback(cb);
        capture(mPreviewBuilder.build(), mPreviewCallback);

        // ensure set back to idle
        mPreviewBuilder.set(CaptureRequest.CONTROL_AE_PRECAPTURE_TRIGGER, CameraMetadata.CONTROL_AF_TRIGGER_IDLE);
    }

    public void setAutoFocusMoveCallback(InterfaceUtil.AutoFocusMoveCallback callback) {
        if (mPreviewBuilder == null ||mPreviewCallback == null) {
            return;
        }

        mPreviewCallback.setContinousFocusCallback(callback);
    }

    public Size getPreviewSize() {
        if (mCameraDevice == null) {
            throw new RuntimeException();
        }

        if (mPreviewWidth == 0 || mPreviewHeight == 0) {
            throw new RuntimeException();
        }

        return new Size(mPreviewWidth, mPreviewHeight);
    }

    public Size getPictureSize() {
        if (mCameraDevice == null) {
            throw new RuntimeException();
        }

        if (mPictureWidth == 0 || mPictureHeight == 0) {
            throw new RuntimeException();
        }

        return new Size(mPictureWidth, mPictureHeight);
    }

    public Rect getViewableRect() {
        return CameraUtil.getViewableRect(mPreviewBuilder, mCharacteristics);
    }

    public CameraCharacteristics getCharacteristics() {
        return mCharacteristics;
    }

    public CaptureRequest.Builder getPreviewBuilder() {
        return mPreviewBuilder;
    }
}
