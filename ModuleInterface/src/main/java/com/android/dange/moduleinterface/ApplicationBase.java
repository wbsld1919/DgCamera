package com.android.dange.moduleinterface;

import android.content.Context;

public interface ApplicationBase {
    void addModule(String uuid, String moduleName);
    void removeModule(String uuid, String moduleName);
    Context getContext();
    void runOnUiThread(Runnable action);
}
