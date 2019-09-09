package com.okfunc.bouncylayout;

import android.content.Context;
import android.graphics.Color;
import android.util.DisplayMetrics;

public class BouncyConfig {
    public static final int DEFAULT_SCROLL_DURATION = 900;
    public static final int DEFAULT_GAP_HEIGHT = 800;
    public static final int DEFAULT_GAP_LIMIT = 300;

    public int scrollDuration;
    public int gapHeight;
    public int gapLimit;

    public BouncyGapLayout createHeaderView(Context context) {
        BouncyGapLayout header = createDefaultGapLayout(context);
        return header;
    }

    public BouncyGapLayout createFooterView(Context context) {
        BouncyGapLayout footer = createDefaultGapLayout(context);
        return footer;
    }

    private BouncyGapLayout createDefaultGapLayout(Context context) {
        return new BouncyGapLayout(context);
    }

    public static BouncyConfig createDefault(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        BouncyConfig config = new BouncyConfig();
        config.gapHeight = dp2px(DEFAULT_GAP_HEIGHT, dm);
        config.gapLimit = dp2px(DEFAULT_GAP_LIMIT, dm);
        config.scrollDuration = DEFAULT_SCROLL_DURATION;
        return config;
    }

    public static int dp2px(float dp, DisplayMetrics dm) {
        return (int) (dp * dm.density + 0.5f);
    }
}
