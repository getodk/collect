package org.odk.collect.android.wassan.app;

import android.graphics.drawable.GradientDrawable;
import android.graphics.Color;
import java.util.Random;

public class Utils {

    public static GradientDrawable getRandomGradientDrawable() {
        Random random = new Random();

        // Generate two random colors
        int color1 = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));
        int color2 = Color.rgb(random.nextInt(256), random.nextInt(256), random.nextInt(256));

        // Create a gradient drawable with the two colors
        GradientDrawable gradientDrawable = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM, new int[]{color1, color2});

        // Set the corner radius
        gradientDrawable.setCornerRadius(16f);

        return gradientDrawable;
    }
}
