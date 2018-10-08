package com.android.dange.camera;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.TextureView;

import com.android.dange.modulecommon.Util.AppUtil;
import com.android.dange.modulecommon.Util.LogUtil;
import com.android.dange.moduleinterface.ApplicationBase;
import com.android.dange.moduleinterface.ApplicationManager;
import com.android.dange.moduleinterface.ModuleDeviceBase;
import com.android.dange.moduleinterface.ModuleManager;
import com.android.dange.moduleui.preview.AutoFitTextureView;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener {
    private static final String TAG = "Cam_Activity";

    private ExecutorService cachedThreadExecutor = Executors.newCachedThreadPool();

    private ApplicationBase mApplicationBase;

    private AutoFitTextureView mTextureView;
    private ModuleDeviceBase mModuleDeviceBase;

    private String mUuid;

    private volatile boolean mHasOpened;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LogUtil.v();

        mUuid = UUID.randomUUID().toString();

        LogUtil.v("mUuid = " + mUuid);

        mApplicationBase = ApplicationManager.getInstance().getApplicationBase();
        mApplicationBase.addModule(getUuid(), ModuleManager.MODULE_DEVICE);
        mApplicationBase.addModule(getUuid(), ModuleManager.MODULE_UI);

        getModuleDeviceBase().open();
        mHasOpened = true;

        setContentView(R.layout.activity_main);

        AppUtil.init(this);

        mTextureView = findViewById(R.id.texture_view);
        mTextureView.setSurfaceTextureListener(this);
        mTextureView.setAspectRatio(3, 4);
    }

    private String getUuid() {
        return mUuid;
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mHasOpened) {
            getModuleDeviceBase().open();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getModuleDeviceBase().release();
        mHasOpened = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mApplicationBase != null) {
            mApplicationBase.removeModule(mUuid, ModuleManager.MODULE_FILTER);
            mApplicationBase.removeModule(mUuid, ModuleManager.MODULE_DEVICE);
            mApplicationBase.removeModule(mUuid, ModuleManager.MODULE_UI);
        }
    }

    public ModuleDeviceBase getModuleDeviceBase() {
        if (mModuleDeviceBase == null || mModuleDeviceBase.getUuid() != this.mUuid) {
            mModuleDeviceBase = (ModuleDeviceBase) ModuleManager.getInstance().getModule(getUuid(), ModuleManager.MODULE_DEVICE);
        }
        return mModuleDeviceBase;
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        LogUtil.v();
        getModuleDeviceBase().setDisplaySurfaceTexture(surface, width, height);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        LogUtil.v();
        getModuleDeviceBase().setDisplaySurfaceTexture(surface, width, height);
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        LogUtil.v();
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        LogUtil.v();
    }
}
