package com.wrbug.dumpdex;

import android.os.Build;

import com.wrbug.dumpdex.dump.LowSdkDump;
import com.wrbug.dumpdex.dump.OreoDump;
import com.wrbug.dumpdex.util.DeviceUtils;

import java.io.File;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * XposedInit
 *
 * @author wrbug
 * @since 2018/3/20
 */
public class XposedInit implements IXposedHookLoadPackage {

    public static void log(String txt) {

        XposedBridge.log("dumpdex-> " + txt);
    }

    public static void log(Throwable t) {
        if (!BuildConfig.DEBUG) {
            return;
        }
        XposedBridge.log(t);
    }

    @Override
    public void handleLoadPackage(final XC_LoadPackage.LoadPackageParam lpparam) {
        PackerInfo.Type type = PackerInfo.find(lpparam);

        RLog.saveToSDCard("Find:" + lpparam.packageName + ":" + type);

        if (type == null) {
            return;
        }
        final String packageName = lpparam.packageName;//"cn.mengbb8.live";//lpparam.packageName;
        if (lpparam.packageName.equals(packageName)) {
            String path = "/data/data/" + packageName + "/dump";
            File parent = new File(path);

            RLog.saveToSDCard(this.hashCode() + "Hook:" + packageName + ":" + path);
            if (!parent.exists() || !parent.isDirectory()) {
                parent.mkdirs();
            }
            log("sdk version:" + Build.VERSION.SDK_INT);
            if (DeviceUtils.isOreo()) {
                OreoDump.init(lpparam);
            } else {
                LowSdkDump.init(lpparam, type);
            }

        }
    }
}
