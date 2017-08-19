package org.odk.collect.android.adapters.model;

/**
 * Icon Menu Item representation
 */

public class IconMenuItem {

    private int imageResId;
    private int textResId;

    public IconMenuItem(int imageResId, int textResId) {
        this.imageResId = imageResId;
        this.textResId = textResId;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getTextResId() {
        return textResId;
    }
}
