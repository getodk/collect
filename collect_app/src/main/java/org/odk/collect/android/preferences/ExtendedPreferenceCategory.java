package org.odk.collect.android.preferences;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Custom PreferenceCategory that allows the titleto be "greyed out"/disabled
 * in its entirety.
 */
public class ExtendedPreferenceCategory extends PreferenceCategory {

    @BindView(android.R.id.title)
    TextView title;

    @ColorInt
    private int enabledColor;

    private boolean isEnabled;
    private boolean colorsInitialized;

    public ExtendedPreferenceCategory(Context context) {
        super(context);
    }

    public ExtendedPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedPreferenceCategory(Context context, AttributeSet attrs,
                                      int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * Set the color that is gonna be used for the enabled state.
     * This is necessary because we are pulling the current color of the control
     * that will be based on the theme (light/dark) being used.
     */
    private void setupColorStates() {
        if (!colorsInitialized) {
            enabledColor = title.getCurrentTextColor();
            colorsInitialized = true;
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ButterKnife.bind(this, view);

        setupColorStates();

        if (isEnabled) {
            title.setTextColor(enabledColor);
        } else {
            title.setTextColor(ContextCompat.getColor(getContext(), android.R.color.secondary_text_dark));
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
    }
}