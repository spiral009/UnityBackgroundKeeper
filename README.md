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
**readable at early boot** — i.e. installed as a **system app**. A normal `/data`
install can't do this (it's encrypted-locked when `system_server` starts), and
KernelSU **magic-mount** is too late (PackageManager has already scanned). You need
the APK at `/system_ext/app/UnityBackgroundKeeper/` via one of:

- **Bake into your ROM** (if you build it) — drop the APK in `system_ext/app/`. Cleanest/permanent.
- **KernelSU + OverlayFS** (e.g. the `hybrid_mount` module, or KSU's overlayfs mode) —
  a KSU module that **overlayfs**-mounts the APK to `/system_ext/app/...`. OverlayFS
  mounts early enough for PackageManager (magic-mount does not).
- **`adb remount`** (userdebug / verity disabled) — `adb remount`, then copy the APK
  into `/system_ext/app/UnityBackgroundKeeper/` (context `u:object_r:system_file:s0`), reboot.

Then: enable the module, scope it to **System Framework + your game**, grant the
PiP app-op (`appops set com.vrchat.mobile.playstore PICTURE_IN_PICTURE allow`), reboot.
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
- **The on-screen touch buttons can't be hidden** — they're Unity-rendered, and
  VRChat shows them regardless of a connected controller (verified). Burn-in: use
  lower brightness / your ROM's pixel-shift.
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
