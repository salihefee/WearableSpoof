/*
 * WearableSpoof
 * Copyright (C) 2023 Simon1511
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.simon1511.wearablespoof;

import android.app.admin.DevicePolicyManager;
import android.os.Build;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import io.github.libxposed.api.XposedModule;

public final class MainHook extends XposedModule {
    private static final String TAG = "WearableSpoof";
    private boolean initialized;

    @Override
    public void onPackageLoaded(PackageLoadedParam param) {
        // Boot-class hooks and Build fields affect the entire process. Only initialize for
        // its main Samsung package, never for packages loaded into another app's process.
        String packageName = param.getPackageName();
        if (!param.isFirstPackage() || initialized
                || !(packageName.startsWith("com.samsung.") || packageName.startsWith("com.sec."))) {
            return;
        }
        initialized = true;
        log(Log.INFO, TAG, "Applying compatibility fixes to " + packageName);

        hookConstantMethod(DevicePolicyManager.class, "getStorageEncryptionStatus",
                DevicePolicyManager.ENCRYPTION_STATUS_ACTIVE_PER_USER);
        try {
            Class<?> storageManager = Class.forName("android.os.storage.StorageManager", false,
                    param.getDefaultClassLoader());
            hookConstantMethod(storageManager, "isEncrypted", true);
        } catch (ClassNotFoundException | RuntimeException e) {
            log(Log.WARN, TAG, "StorageManager is unavailable; continuing with device spoofing", e);
        }

        spoofDevice();
    }

    private void hookConstantMethod(Class<?> owner, String name, Object result) {
        try {
            Method method = owner.getDeclaredMethod(name);
            hook(method).intercept(chain -> result);
        } catch (ReflectiveOperationException | RuntimeException e) {
            // Hidden APIs can differ between Android releases and ROMs. Each hook is optional.
            log(Log.WARN, TAG, "Cannot hook " + owner.getName() + "." + name, e);
        }
    }

    private void spoofDevice() {
        if ("samsung".equalsIgnoreCase(Build.MANUFACTURER)) {
            setBuildField("MANUFACTURER", "Google");
        }
        if ("samsung".equalsIgnoreCase(Build.BRAND)) {
            setBuildField("BRAND", "google");
        }
        setBuildField("DEVICE", "cheetah");
        setBuildField("PRODUCT", "cheetah");
        setBuildField("TAGS", "release-keys");
        setBuildField("TYPE", "user");
        if (Build.MODEL != null && Build.MODEL.startsWith("SM-")) {
            setBuildField("MODEL", "Pixel 7 Pro");
        }
        if (Build.FINGERPRINT != null && Build.FINGERPRINT.contains("samsung")) {
            // Keep the known Pixel identity together; this does not change the OS or patch level.
            setBuildField("FINGERPRINT",
                    "google/cheetah/cheetah:13/TQ2A.230505.002/9891397:user/release-keys");
        }
    }

    private void setBuildField(String name, String value) {
        try {
            Field field = Build.class.getDeclaredField(name);
            field.setAccessible(true);
            field.set(null, value);
        } catch (ReflectiveOperationException | RuntimeException e) {
            log(Log.WARN, TAG, "Cannot spoof Build." + name, e);
        }
    }
}
