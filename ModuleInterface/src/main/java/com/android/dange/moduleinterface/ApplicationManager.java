package com.android.dange.moduleinterface;

public class ApplicationManager {
    private static final String TAG = "ApplicationManager";

    private ApplicationBase mApplicationBase;

    private static ApplicationManager sInstance;

    private ApplicationManager(ApplicationBase applicationBase) {
        mApplicationBase = applicationBase;
    }

    public static ApplicationManager getInstance() {
        return sInstance;
    }

    public static void init(ApplicationBase applicationBase) {
        sInstance = new ApplicationManager(applicationBase);
    }

    public ApplicationBase getApplicationBase() {
        return mApplicationBase;
    }
}
