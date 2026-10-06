package win.fantest.touchguard;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    private TextView status;
    private Button primary;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int pad = dp(24);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(pad, dp(56), pad, pad);
        root.setBackgroundColor(Color.WHITE);

        TextView title = new TextView(this);
        title.setText("Touch Guard");
        title.setTextSize(30);
        title.setTextColor(Color.BLACK);
        title.setGravity(Gravity.CENTER);
        title.setTypeface(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD);
        root.addView(title, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        status = new TextView(this);
        status.setTextSize(16);
        status.setTextColor(Color.DKGRAY);
        status.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        statusParams.topMargin = dp(18);
        root.addView(status, statusParams);

        primary = new Button(this);
        primary.setAllCaps(false);
        primary.setTextSize(17);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(58)
        );
        buttonParams.topMargin = dp(32);
        root.addView(primary, buttonParams);

        setContentView(root);
        refreshState();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshState();
    }

    private void refreshState() {
        if (TouchGuardService.isReady()) {
            status.setText("Ready");
            primary.setText("Disable touch input");
            primary.setOnClickListener(v -> {
                if (TouchGuardService.enableGuard()) {
                    moveTaskToBack(true);
                }
            });
        } else {
            status.setText("Accessibility service required");
            primary.setText("Enable accessibility service");
            primary.setOnClickListener(v ->
                    startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            );
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }
}
