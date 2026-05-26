package com.unitybgkeeper;

import android.app.Activity;
import android.app.PictureInPictureParams;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;

/**
 * Unity Background Keeper.
 *
 * - Keep-alive (works as a normal LSPosed module, scope = the game): neutralizes
 *   UnityPlayer.pause() so a Unity game keeps running in the background instead of
 *   freezing/disconnecting.
 * - Forced Picture-in-Picture for VRChat (needs the module installed as a SYSTEM
 *   app so its system_server hook loads at boot — see README): makes VRChat
 *   "support" PiP and auto-enter it on Home, so the game drops into a floating
 *   window that stays visible (renders + audio + connected).
 *
 * Scope: the game(s) you want; add System Framework too for the PiP feature.
 * Compiled against the REAL Xposed API (provided by LSPosed at runtime, NOT bundled).
 */
public class MainHook implements IXposedHookLoadPackage {

    private static final String TAG = "UnityBGKeeper";
    private static final String VRCHAT = "com.vrchat.mobile.playstore";

    @Override
    public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
        // system_server: allow VRChat to use Picture-in-Picture.
        if ("android".equals(lpparam.packageName)) {
            try {
                Class<?> ar = XposedHelpers.findClass(
                        "com.android.server.wm.ActivityRecord", lpparam.classLoader);
                XposedBridge.hookAllMethods(ar, "supportsPictureInPicture", new XC_MethodHook() {
                    @Override
                    protected void beforeHookedMethod(MethodHookParam p) {
                        try {
                            String pkg = (String) XposedHelpers.getObjectField(
                                    p.thisObject, "packageName");
                            if (VRCHAT.equals(pkg)) p.setResult(Boolean.TRUE);
                        } catch (Throwable ignore) { }
                    }
                });
                XposedBridge.log(TAG + ": PiP enabled for VRChat in system_server");
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": failed to enable PiP: " + t);
            }
            return;
        }

        if ("system".equals(lpparam.packageName)) return;

        Class<?> unityPlayer;
        try {
            unityPlayer = XposedHelpers.findClass("com.unity3d.player.UnityPlayer",
                    lpparam.classLoader);
        } catch (Throwable t) {
            return; // not a standard Unity app
        }
        XposedBridge.log(TAG + ": active in " + lpparam.packageName);

        // Keep the engine running in the background / PiP.
        try {
            XposedBridge.hookAllMethods(unityPlayer, "pause", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam p) { p.setResult(null); }
            });
            XposedBridge.log(TAG + ": UnityPlayer.pause() neutralized");
        } catch (Throwable t) {
            XposedBridge.log(TAG + ": pause hook failed: " + t);
        }

        // VRChat: auto-enter PiP on Home (floating window stays visible).
        if (VRCHAT.equals(lpparam.packageName)) {
            try {
                Class<?> activity = XposedHelpers.findClass(
                        "android.app.Activity", lpparam.classLoader);
                XposedBridge.hookAllMethods(activity, "onResume", new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(MethodHookParam p) {
                        try {
                            ((Activity) p.thisObject).setPictureInPictureParams(
                                    new PictureInPictureParams.Builder()
                                            .setAutoEnterEnabled(true).build());
                        } catch (Throwable ignore) { }
                    }
                });
                XposedBridge.log(TAG + ": auto-PiP armed for VRChat");
            } catch (Throwable t) {
                XposedBridge.log(TAG + ": auto-PiP arm failed: " + t);
            }
        }
    }
}
