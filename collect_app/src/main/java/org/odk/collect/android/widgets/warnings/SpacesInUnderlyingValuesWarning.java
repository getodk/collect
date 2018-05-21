package org.odk.collect.android.widgets.warnings;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UnderlyingValuesConcat;

import java.util.List;

public class SpacesInUnderlyingValuesWarning {

    private final SpacesInUnderlyingValues valuesChecker = new SpacesInUnderlyingValues();
    private final UnderlyingValuesConcat formatter = new UnderlyingValuesConcat();

    public void renderWarningIfNecessary(List<SelectChoice> items, LinearLayout answerLayout) {

        valuesChecker.check(items);

        if (valuesChecker.hasInvalidValues()) {
            answerLayout.addView(createWarning(valuesChecker.getInvalidValues(), answerLayout.getContext()));
        }
    }

    private View createWarning(List<SelectChoice> invalidValues, Context context) {
        TextView warning = new TextView(context);

        warning.setText(warning.getContext().getResources().getString(
                invalidValues.size() > 1 ? R.string.invalid_space_in_answer_plural : R.string.invalid_space_in_answer_singular,
                formatter.asString(invalidValues)));

        warning.setPadding(10, 10, 10, 10);
        return warning;
    }

    public static class SpacesInUnderlyingValues {

        private List<SelectChoice> invalidValues = Lists.newArrayList();
        private boolean checked = false;

        public void check(List<SelectChoice> items) {
            invalidValues = FluentIterable
                    .from(items)
                    .filter(item -> item.getValue() != null && item.getValue().contains(" "))
                    .toList();
            checked = true;
        }

        public boolean hasInvalidValues() {
            checkInitialization();
            return !invalidValues.isEmpty();
        }

        public List<SelectChoice> getInvalidValues() {
            checkInitialization();
            return invalidValues;
        }

        private void checkInitialization() {
            if (!checked) {
                throw new IllegalStateException("check() must be called before other methods first");
            }
        }
    }

}
