package org.odk.collect.android.preferences;

import android.content.Context;
import android.preference.CheckBoxPreference;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;

import org.odk.collect.android.utilities.ThemeUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A Checkbox preference that allows the title to have the disabled look when the
 * dependency it references gets disabled.
 */
public class ExtendedCheckboxPreference extends CheckBoxPreference {

    private Boolean shouldDisableDependents;

    @BindView(android.R.id.title)
    TextView title;
    @BindView(android.R.id.checkbox)
    CheckBox checkBox;

    public ExtendedCheckboxPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ExtendedCheckboxPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ExtendedCheckboxPreference(Context context) {
        super(context);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);

        ButterKnife.bind(this, view);

        //the title's color is only modified if the checkbox is disabled which means the parent
        //Server setting was disabled since shouldDisableDependents gets set in multiple places.
        if (!checkBox.isEnabled()) {
            if (!shouldDisableDependents) {
                ThemeUtils themeUtils = new ThemeUtils(getContext());

                if (themeUtils.isDarkTheme()) {
                    int darkEnabledSummaryColor = ContextCompat.getColor(getContext(), android.R.color.white);
                    title.setTextColor(darkEnabledSummaryColor);
                } else {
                    int lightEnabledSummaryColor = -16777216;
                    title.setTextColor(lightEnabledSummaryColor);
                }
            } else {
                title.setTextColor(ContextCompat.getColor(getContext(), android.R.color.secondary_text_dark));
            }
        }
    }

    @Override
    public boolean shouldDisableDependents() {
        shouldDisableDependents = super.shouldDisableDependents();
        return shouldDisableDependents;
    }
}
