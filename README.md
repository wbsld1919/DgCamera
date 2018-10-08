# DgCamera
#### A range of functions for camera2.  

###### functions below will be added:  
- Basic camera functions such as id switch, preview size switch, taking photos, recording video, focus and so on.
- Filters.
- Basic OpenGL ES functions.
- Meng face.
- ...

Currently, only preview and the basic codes have been added.

#### Note:
All the relative sigle functions are packaged in Modules.  

For example, the concrete camera2 operation such as opening camera, preview   
start are in the ModuleDevice library. also the global interfaces are in  
the ModuleInterface library.   

If you want to add one library in you app. Firstly, you can add  

```
implementation project(':ModuleDevice')
```
in your build.gradle. and then in your application, use 

```
ApplicationManager.init(this);
```
the in your activity, use
```
// here the mUUid is for separating the diffrent app.
String mUuid = UUID.randomUUID().toString();

ApplicationBase mApplicationBase = ApplicationManager.getInstance().getApplicationBase();

mApplicationBase.addModule(getUuid(), ModuleManager.MODULE_DEVICE);

ModuleDeviceBase mModuleDeviceBase = (ModuleDeviceBase) ModuleManager.getInstance().getModule(getUuid(), ModuleManager.MODULE_DEVICE);

mModuleDeviceBase.open();
mModuleDeviceBase.setDisplaySurfaceTexture(...);
```
to effect the library.

