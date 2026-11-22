# WearableSpoof

## What is WearableSpoof?
WearableSpoof is a module for LSPosed that lets you use Samsung apps (for example Wearable/Gear, SmartThings, ...) on Samsung phones running AOSP ROMs.
Usually, when trying to install Samsung apps on a Samsung phone running an AOSP ROM (for example LineageOS), you would
be greeted by an error message saying that you're not allowed to run this app on a modified phone or simply a force closing app.
This module bypasses their checks by spoofing all device-specific properties to ones from the Pixel 7 Pro and lets you run these apps anyway.

## IMPORTANT
This module won't magically let you run all Samsung apps on your phone (for example Samsung Camera). ONLY ones that can normally be installed on
non-Samsung phones too should and will most likely work without issues.

## Requirements and compatibility

- Android 10 (API 29) or newer on a Samsung phone running an AOSP ROM.
- A rooted device with an active Xposed framework implementing **libxposed API 102 or newer**.
  This version uses the [modern libxposed API](https://github.com/libxposed/api).
  Legacy XposedBridge-only frameworks and frameworks supporting only API 100/101 cannot load it.
- Google Play services may be required by the Samsung apps themselves.

Version 1.3 migrates away from the legacy Xposed entry point. Check your framework's API
support before upgrading from 1.2. The module keeps the existing Pixel 7 Pro spoof profile;
it does not change your actual Android version, security patch level, or device certification.

## How to install

1. Install and activate a compatible Xposed framework using its own installation instructions.
2. Install the WearableSpoof APK from the [releases section](https://github.com/Simon1511/WearableSpoof/releases).
3. Install Galaxy Wearable, SmartThings, and any watch/band plugins you need.
4. In your framework's manager, enable WearableSpoof and select the Samsung apps that need it.
   Galaxy Wearable and SmartThings are included as recommended scope. Add each installed
   watch/band plugin manually; the scope remains editable.
5. Restart the selected apps (or reboot). Repeat the scope step whenever a new plugin is installed.

If using Magisk/Zygisk, do not exclude the selected apps from injection with an enforced
DenyList. Enable the module only for the Samsung apps that need it, not Android system
processes or Google Play services. Packages outside `com.samsung.*` and `com.sec.*` are ignored.

## Building

Use JDK 17 and an Android SDK containing platform 37 (`platforms;android-37.0`) and
Build Tools 36.0.0 or newer. Set `ANDROID_HOME` to the SDK location or set `sdk.dir` in
an untracked `local.properties` file. Gradle is provided by the wrapper.

```sh
./gradlew assembleDebug assembleRelease lint lintRelease
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Release builds
are unsigned; sign them with your own existing release key before distribution. Updating
an installed APK requires the same signing key as the previous installation.

Build and lint checks do not verify framework injection or Samsung app behavior. Before
releasing, test on a supported rooted device: confirm the module is recognized, enable
Galaxy Wearable and its plugin, restart both, and complete pairing. Check the framework
logs for `WearableSpoof` warnings and verify an app outside the module's scope is unaffected.

## FAQ and issues
### The wearable app asks me to download the plugin from the Play Store
After the installation finishes, open the LSPosed Manager and add the app to the WearableSpoof module. Reboot afterwards. You might need to clear cache and data of the plugin app too.

### I get "Connection error" during setup
Go into your phone's Settings -> Apps -> All Apps -> Enable "show system apps" -> Search for "Play Services" -> Permissions -> Allow permissions for "Nearby devices".

### The plugin app on the Play Store says "Not supported on this device"
Check whether the Play Store shows "Device certified" in Settings. This module only changes
values inside selected Samsung app processes; it cannot change Play Store compatibility,
Play Integrity verdicts, or server-side eligibility checks.
