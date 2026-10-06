package win.fantest.touchguard;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import java.util.ArrayDeque;
import java.util.Deque;

public class TouchGuardService extends AccessibilityService {
    private static final int REQUIRED_PRESSES = 6;
    private static final long UNLOCK_WINDOW_MS = 3000L;

    private static volatile TouchGuardService instance;

    private final Deque<Long> volumeDownPresses = new ArrayDeque<>();
    private WindowManager windowManager;
    private View touchShield;
    private boolean guardEnabled = false;

    public static boolean isReady() {
        return instance != null;
    }

    public static boolean enableGuard() {
        TouchGuardService service = instance;
        return service != null && service.enableTouchShield();
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);

        // Safety rule: never restore a previous blocked state after service restart,
        // process death, package update, or device reboot.
        guardEnabled = false;
        volumeDownPresses.clear();
        removeTouchShield();
    }

    private boolean enableTouchShield() {
        if (guardEnabled) {
            return true;
        }

        if (windowManager == null) {
            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        }
        if (windowManager == null) {
            return false;
        }

        View shield = new View(this);
        shield.setBackgroundColor(Color.TRANSPARENT);
        shield.setClickable(true);
        shield.setFocusable(false);
        shield.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
        shield.setOnTouchListener((v, event) -> true);
        shield.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
        );

        int flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                flags,
                PixelFormat.TRANSLUCENT
        );
        params.gravity = Gravity.TOP | Gravity.START;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            params.setFitInsetsTypes(0);
        }

        try {
            windowManager.addView(shield, params);
            touchShield = shield;
            volumeDownPresses.clear();
            guardEnabled = true;
            return true;
        } catch (RuntimeException e) {
            touchShield = null;
            guardEnabled = false;
            return false;
        }
    }

    private void disableGuard() {
        guardEnabled = false;
        volumeDownPresses.clear();
        removeTouchShield();
    }

    private void removeTouchShield() {
        View shield = touchShield;
        touchShield = null;
        if (shield != null && windowManager != null) {
            try {
                windowManager.removeViewImmediate(shield);
            } catch (RuntimeException ignored) {
                // Window may already have been removed by the system.
            }
        }
    }

    @Override
    protected boolean onKeyEvent(KeyEvent event) {
        if (!guardEnabled) {
            return super.onKeyEvent(event);
        }

        if (event.getKeyCode() != KeyEvent.KEYCODE_VOLUME_DOWN) {
            return super.onKeyEvent(event);
        }

        // Consume Volume Down while guarded so the unlock sequence does not
        // alter media/ringer volume.
        if (event.getAction() == KeyEvent.ACTION_DOWN && event.getRepeatCount() == 0) {
            long now = SystemClock.uptimeMillis();

            while (!volumeDownPresses.isEmpty()
                    && now - volumeDownPresses.peekFirst() > UNLOCK_WINDOW_MS) {
                volumeDownPresses.removeFirst();
            }

            volumeDownPresses.addLast(now);

            if (volumeDownPresses.size() >= REQUIRED_PRESSES) {
                disableGuard();
            }
        }

        return true;
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // No UI inspection is required.
    }

    @Override
    public void onInterrupt() {
        disableGuard();
    }

    @Override
    public boolean onUnbind(android.content.Intent intent) {
        disableGuard();
        instance = null;
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        disableGuard();
        instance = null;
        super.onDestroy();
    }
}
