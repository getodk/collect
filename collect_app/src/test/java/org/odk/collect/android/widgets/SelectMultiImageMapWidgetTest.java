package org.odk.collect.android.widgets;

import androidx.annotation.NonNull;

import com.google.common.collect.ImmutableList;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.formentry.questions.QuestionDetails;

import java.util.List;

public class SelectMultiImageMapWidgetTest extends SelectImageMapWidgetTest<SelectMultiImageMapWidget, SelectMultiData> {
    @NonNull
    @Override
    public SelectMultiImageMapWidget createWidget() {
        return new SelectMultiImageMapWidget(activity, new QuestionDetails(formEntryPrompt, "formAnalyticsID"));
    }

    @NonNull
    @Override
    public SelectMultiData getNextAnswer() {
        List<SelectChoice> selectChoices = getSelectChoices();

        int selectedIndex = Math.abs(random.nextInt()) % selectChoices.size();
        SelectChoice selectChoice = selectChoices.get(selectedIndex);

        Selection selection = new Selection(selectChoice);
        return new SelectMultiData(ImmutableList.of(selection));
    }
}
