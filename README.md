# Unity Background Keeper

An LSPosed/Xposed module that keeps **Unity-engine games** (e.g. VRChat) usable
in the background on Android 16, which otherwise freezes/disconnects them.

Two features:

1. **Keep-alive** — neutralizes `UnityPlayer.pause()` so the game keeps running
   (engine + networking) when you leave it, instead of freezing. Works as a normal
   LSPosed module.
2. **Forced Picture-in-Picture for VRChat** — makes VRChat "support" PiP and
   auto-enter it when you press **Home**, so the game drops into a **floating
   window that stays visible** → it keeps rendering, playing audio, and staying
   connected (this is the reliable way to "use it in the background"). *Requires
   installing the module as a system app — see below.*

## Install

### Keep-alive only (any LSPosed setup)
1. Install the APK (see **Releases**).
2. In **LSPosed**: enable the module, set **Scope** to your game(s), reboot.
   Done — the game no longer freezes when backgrounded.

### + Picture-in-Picture (VRChat)
PiP needs a hook in `system_server`, which only loads if the module's code is
**readable at early boot** — i.e. installed as a **system app** at
`/system_ext/app/UnityBackgroundKeeper/`. A normal `/data` install can't do this
(it's encrypted-locked when `system_server` starts), and KernelSU **magic-mount** is
too late (PackageManager has already scanned). Working ways to get it there:

- **Bake into your ROM** (if you build it) — drop the APK in `system_ext/app/`.
  Cleanest and permanent; survives everything. Recommended if you can.
- **`adb remount`** (userdebug build / verity disabled) — `adb disable-verity`,
  reboot, `adb remount`, then copy the APK into
  `/system_ext/app/UnityBackgroundKeeper/` and `chcon u:object_r:system_file:s0` it,
  reboot. **This is the method actually verified on the test device** (OnePlus 13,
  crDroid Android 16, KernelSU Next).

> **OverlayFS / `hybrid_mount` — tested, did NOT work here.** In theory a KSU
> OverlayFS metamodule (e.g. `hybrid_mount`) should mount the APK to `/system_ext`
> early enough for PackageManager, avoiding `adb remount`. In practice, on a device
> that *already* has an `adb remount` OverlayFS on `/system_ext`, swapping the KSU
> metamodule from magic-mount to `hybrid_mount` (OverlayFS) **bootlooped** — two
> OverlayFS layers over the same partitions conflict. Switching the global mount
> backend also affects every other module, so it's risky. If you go this route,
> remove the adb-remount overlay first (re-enable dm-verity) and expect to recover
> from a bootloop. Not recommended unless you know what you're doing.

After the APK is a system app: enable the module, scope it to **System Framework +
your game**, grant the PiP app-op
(`appops set com.vrchat.mobile.playstore PICTURE_IN_PICTURE allow`), reboot.
Press Home in a world → VRChat floats in a PiP window.

## Recommended scope

| Scope | Keep-alive | PiP |
|-------|:---:|:---:|
| The game (e.g. `com.vrchat.mobile.playstore`) | ✅ | ✅ |
| **System Framework** | — | ✅ |

## Honest limitations

- **Muting just one app's audio isn't possible** for VRChat — its voice/TV use a
  native AAudio path with no Java handle and no OSC control; only **global Media
  volume** silences it. PiP keeps it audible; turn Media volume down for quiet.
- It can't render while *truly hidden* (no surface) — that's why PiP (visible) is
  the approach.

## Building

Source under `src/`, compiled against the **real Xposed API** (`libs/api-82.jar`,
provided by LSPosed at runtime, **not bundled** — bundling guessed API stubs is the
classic reason modules silently fail with `NoSuchMethodError`/`VerifyError`).
`build.sh` shows the compile → d8 → aapt2 → sign pipeline (point `ANDROID_JAR` /
`BUILD_TOOLS` / `JAVA` at your SDK).

## License

MIT — see [LICENSE](LICENSE).
