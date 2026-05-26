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
   connected (this is the reliable way to "use it in the background").

## Install

Just an ordinary LSPosed module — **no system app, no `adb remount`, no ro2rw, no
KernelSU mount, no root-partition changes.**

1. Install the APK like any app (see **Releases**, or `adb install`).
2. Open **LSPosed** → enable the module → set **Scope** to:
   - **your game** (e.g. `com.vrchat.mobile.playstore`) — for keep-alive + auto-PiP, and
   - **System Framework** — for the PiP enablement (the `system_server` hook).
3. **Reboot** (the System Framework hook is applied at boot).
4. If PiP doesn't engage, make sure VRChat is allowed PiP:
   `appops set com.vrchat.mobile.playstore PICTURE_IN_PICTURE allow` (usually allowed by default).

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
