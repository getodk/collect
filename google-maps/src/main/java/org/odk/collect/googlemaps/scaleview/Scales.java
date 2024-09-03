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
