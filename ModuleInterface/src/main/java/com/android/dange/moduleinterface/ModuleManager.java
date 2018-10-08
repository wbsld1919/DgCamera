package com.android.dange.moduleinterface;

import android.text.TextUtils;

import java.util.HashMap;

public class ModuleManager {

    private static final String TAG = "ModuleManager";

    public static final String MODULE_DEVICE = "module_device";
    public static final String MODULE_UI = "module_ui";
    public static final String MODULE_FILTER = "module_filter";

    private Integer mLockModuleList = new Integer(0);
    private HashMap<String, ModuleBase> mModuleList = new HashMap();

    private static Integer sLockModuleManager = new Integer(0);
    private static volatile ModuleManager sModuleManager = null;

    private ModuleManager() {
    }

    public static ModuleManager getInstance() {
        if (sModuleManager == null) {
            synchronized (sLockModuleManager) {
                if (sModuleManager == null) {
                    sModuleManager = new ModuleManager();
                }
            }
        }
        return sModuleManager;
    }

    public void addModule(String uuid, String moduleName, ModuleBase module) {
        if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(moduleName) || (module == null)) {
            return;
        }

        String mModuleName = getUuidModuleName(uuid, moduleName);
        synchronized (mLockModuleList) {
            mModuleList.put(mModuleName, module);
            module.init(uuid);
        }
    }

    public void removeModule(String uuid, String moduleName) {
        if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(moduleName)) {
            return;
        }

        String mModuleName = getUuidModuleName(uuid, moduleName);
        synchronized (mLockModuleList) {
            ModuleBase moduleBase = mModuleList.get(mModuleName);
            if (moduleBase != null) {
                moduleBase.unInit();
                mModuleList.remove(mModuleName);
            }
        }
    }

    public ModuleBase getModule(String uuid, String moduleName) {
        if (TextUtils.isEmpty(uuid) || TextUtils.isEmpty(moduleName)) {
            return null;
        }

        ModuleBase ret = null;
        String mModuleName = getUuidModuleName(uuid, moduleName);
        synchronized (mLockModuleList) {
            ret = mModuleList.get(mModuleName);
        }
        return ret;
    }

    private String getUuidModuleName(String uuid, String moduleName) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(moduleName);
        stringBuilder.append("_");
        stringBuilder.append(uuid);
        return stringBuilder.toString();
    }
}
