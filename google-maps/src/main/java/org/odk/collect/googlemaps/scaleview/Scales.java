/*
 * This file includes code from MapScaleView (https://github.com/pengrad/MapScaleView),
 * licensed under the Apache License, Version 2.0.
 */
package org.odk.collect.googlemaps.scaleview;

import androidx.annotation.Nullable;

class Scales {
    private final Scale top;
    private final Scale bottom;

    Scales(Scale top, Scale bottom) {
        this.top = top;
        this.bottom = bottom;
    }

    @Nullable
    Scale top() {
        return top;
    }

    @Nullable
    Scale bottom() {
        return bottom;
    }

    float maxLength() {
        return Math.max(top != null ? top.length() : 0, bottom != null ? bottom.length() : 0);
    }
}
