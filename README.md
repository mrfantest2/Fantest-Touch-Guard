# Fantest Touch Guard

Native Android touch-input guard designed for Samsung Galaxy S25.

## Behavior

- Uses an Android Accessibility Service.
- A transparent accessibility overlay consumes screen touches.
- There is no on-screen unlock hint, countdown, toast, or persistent notification.
- Six distinct Volume Down presses within 3 seconds remove the touch shield.
- Holding Volume Down does not count as multiple presses.
- Volume Down events used by the unlock sequence are consumed while guarded.
- Any service restart, process death, package update, or device reboot starts unguarded as a safety rule.
- Power/Side key behavior is not modified.

## First-time setup

1. Install the APK.
2. Open Touch Guard.
3. Enable **Touch Guard Control** in Android Accessibility settings.
4. Return to the app and tap **Disable touch input**.

## Build

GitHub Actions builds the debug APK from `.github/workflows/android.yml`.

Package: `win.fantest.touchguard`
