package org.odk.collect.android.widgets.warnings;

import android.content.Context;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.Lists;
import org.javarosa.core.model.SelectChoice;
import org.odk.collect.android.R;
import org.odk.collect.android.utilities.UnderlyingValuesConcat;
import org.odk.collect.android.widgets.QuestionWidget;

import java.util.List;

public class SpacesInUnderlyingValuesWarning {

    private final UnderlyingValuesChecker valuesChecker;
    private final WarningRenderer warningRenderer;

    public static SpacesInUnderlyingValuesWarning forQuestionWidget(QuestionWidget questionWidget) {
        WarningRenderer renderer = new RenderIntoQuestionWidget(questionWidget);
        UnderlyingValuesChecker valuesChecker = new SpacesInUnderlyingValues();
        return new SpacesInUnderlyingValuesWarning(valuesChecker, renderer);
    }

    @VisibleForTesting
    SpacesInUnderlyingValuesWarning(UnderlyingValuesChecker valuesChecker, WarningRenderer warningRenderer) {
        this.valuesChecker = valuesChecker;
        this.warningRenderer = warningRenderer;
    }


    public void renderWarningIfNecessary(List<SelectChoice> items) {
        valuesChecker.check(items);

        if (valuesChecker.hasInvalidValues()) {
            warningRenderer.render(valuesChecker.getInvalidValues());
        }
    }

    interface WarningRenderer {
        void render(List<SelectChoice> items);
    }

    private static class RenderIntoQuestionWidget implements WarningRenderer {

        private QuestionWidget questionWidget;
        private final WarningTextCreator warningCreator;

        RenderIntoQuestionWidget(QuestionWidget questionWidget) {
            this.questionWidget = questionWidget;
            warningCreator = new SpacesInUnderlyingValuesTextCreator();
        }

        @Override
        public void render(List<SelectChoice> invalidItems) {
            questionWidget.showWarning(warningCreator.create(invalidItems, questionWidget.getContext()));
        }
    }

    interface UnderlyingValuesChecker {
        void check(List<SelectChoice> items);

        boolean hasInvalidValues();

        List<SelectChoice> getInvalidValues();
    }

    public static class SpacesInUnderlyingValues implements UnderlyingValuesChecker {

        private List<SelectChoice> invalidValues = Lists.newArrayList();
        private boolean checked = false;

        @Override
        public void check(List<SelectChoice> items) {
            invalidValues = FluentIterable
                    .from(items)
                    .filter(item -> item.getValue() != null && item.getValue().contains(" "))
                    .toList();
            checked = true;
        }

        @Override
        public boolean hasInvalidValues() {
            checkInitialization();
            return !invalidValues.isEmpty();
        }

        @Override
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

    public interface WarningTextCreator {
        String create(List<SelectChoice> invalidValues, Context context);
    }

    private static class SpacesInUnderlyingValuesTextCreator implements WarningTextCreator {

        private UnderlyingValuesConcat formatter = new UnderlyingValuesConcat();

        @Override
        public String create(List<SelectChoice> invalidValues, Context context) {
            return context.getResources().getString(
                    invalidValues.size() > 1 ? R.string.invalid_space_in_answer_plural : R.string.invalid_space_in_answer_singular,
                    formatter.asString(invalidValues));
        }
    }
}
