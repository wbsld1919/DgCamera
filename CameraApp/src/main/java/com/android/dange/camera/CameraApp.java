package com.android.dange.camera;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.android.dange.cameralib.ModuleDevice;
import com.android.dange.modulecommon.Util.LogUtil;
import com.android.dange.modulefilter.ModuleFilter;
import com.android.dange.moduleinterface.ApplicationBase;
import com.android.dange.moduleinterface.ApplicationManager;
import com.android.dange.moduleinterface.ModuleManager;
import com.android.dange.moduleui.ModuleUI;

public class CameraApp extends Application implements ApplicationBase {

    private static CameraApp sApp = null;

    @Override
    public void onCreate() {
        super.onCreate();

        LogUtil.v("app oncreate");

        sApp = this;

        ApplicationManager.init(this);
    }

    @Override
    public void addModule(String uuid, String moduleName) {
        switch (moduleName) {
            case ModuleManager.MODULE_DEVICE:
                ModuleManager.getInstance().addModule(uuid, moduleName, new ModuleDevice());
                break;
            case ModuleManager.MODULE_UI:
                ModuleManager.getInstance().addModule(uuid, moduleName, new ModuleUI());
                break;
            case ModuleManager.MODULE_FILTER:
                ModuleManager.getInstance().addModule(uuid, moduleName, new ModuleFilter());
                break;
        }
    }

    @Override
    public void removeModule(String uuid, String moduleName) {
        ModuleManager.getInstance().removeModule(uuid, moduleName);
    }

    @Override
    public Context getContext() {
        return getApplicationContext();
    }

    @Override
    public void runOnUiThread(Runnable action) {
        if (Thread.currentThread() != Looper.getMainLooper().getThread()) {
            new Handler(Looper.getMainLooper()).post(action);
        } else {
            action.run();
        }
    }
}
