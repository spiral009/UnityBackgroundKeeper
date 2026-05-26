package com.unitybgkeeper;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Info-only screen. There are no controls — the module is always on once
 *  enabled and scoped in LSPosed. */
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final float d = getResources().getDisplayMetrics().density;
        final int pad = (int) (16 * d);

        ScrollView scroll = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(pad, pad, pad, pad);
        scroll.addView(root);

        TextView title = new TextView(this);
        title.setText("Unity Background Keeper");
        title.setTextSize(24);
        title.setPadding(0, 0, 0, pad);
        root.addView(title);

        TextView info = new TextView(this);
        info.setText("Keeps Unity games (e.g. VRChat) running while you switch to another "
                + "app, so they don't freeze or disconnect.\n\n"
                + "Setup:\n"
                + "1. In LSPosed, enable this module.\n"
                + "2. Set its Scope to the game(s) you want (e.g. VRChat).\n"
                + "3. Reboot once.\n\n"
                + "That's it — it's always on while enabled. There's nothing to configure here.\n\n"
                + "Notes:\n"
                + "• Keeping a game running in the background uses more battery.\n"
                + "• On return the view may need a quick screen rotation to repaint.\n"
                + "• In-game voice/audio is handled by the game's own (native) code and "
                + "can't be controlled from here.");
        root.addView(info);

        setContentView(scroll);
    }
}
