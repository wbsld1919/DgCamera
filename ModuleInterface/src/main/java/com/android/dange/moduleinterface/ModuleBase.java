package com.android.dange.moduleinterface;

public interface ModuleBase {
    String getUuid();

    void init(String uuid);

    void unInit();
}
