package org.odk.collect.android.preferences;

import android.content.Context;
import android.preference.EditTextPreference;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.odk.collect.android.utilities.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Custom EditTextPreference that allows the title and summary to be "greyed out"/disabled
 * in its entirety.
 */
public class ExtendedEditTextPreference extends EditTextPreference {

    @BindView(android.R.id.title)
    TextView title;
    @BindView(android.R.id.summary)
    TextView summary;

    @ColorInt
    private int enabledTitleColor;
    @ColorInt
    private int disabledColor;

    private boolean colorsInitialized;
    private boolean isEnabled;


    public ExtendedEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendedEditTextPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedEditTextPreference(Context context) {
        super(context);
    }

    /**
     * Sets the colors that are going be used for each state once.
     * This is necessary because we are pulling the current colors of the control
     * that will be based on the theme (light/dark) being used.
     */
    private void setupColorStates() {
        if (!colorsInitialized) {
            enabledTitleColor = title.getCurrentTextColor();

            disabledColor = ContextCompat.getColor(getContext(), android.R.color.secondary_text_dark);
            colorsInitialized = true;
        }
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        ButterKnife.bind(this, view);

        setupColorStates();

        if (isEnabled) {
            title.setTextColor(enabledTitleColor);

            ThemeUtils themeUtils = new ThemeUtils(getContext());

            if (themeUtils.isDarkTheme()) {
                int darkEnabledSummaryColor = -1275068417;
                summary.setTextColor(darkEnabledSummaryColor);
            } else {
                int lightEnabledSummaryColor = 1979711488;
                summary.setTextColor(lightEnabledSummaryColor);
            }
        } else {
            title.setTextColor(disabledColor);
            summary.setTextColor(disabledColor);
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
    }
}