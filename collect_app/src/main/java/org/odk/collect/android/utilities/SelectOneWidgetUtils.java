package org.odk.collect.android.utilities;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.items.ItemsWidget;
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget;
import org.odk.collect.android.widgets.items.SelectOneWidget;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Supplier;

import timber.log.Timber;

public class SelectOneWidgetUtils {

    private SelectOneWidgetUtils() {

    }

    private static final String BAD_NAME = "123";

    //'Macro'
    private static final BiPredicate<String, String> queryMatchesName = (query, name) -> {
        return query.matches(".*\\b" + name + "\\b.*");
    };

    public static void checkFastExternalCascade(ItemsWidget caller) {
        //Exclude untested usage
        if (!(caller instanceof SelectOneWidget || caller instanceof SelectOneMinimalWidget)) {
            throw new IllegalArgumentException("This method is only tested for calls from " +
                    SelectOneWidget.class.getSimpleName() + " or " + SelectOneMinimalWidget.class.getSimpleName());
        }

        FormController fc = Collect.getInstance().getFormController();
        if (fc == null //Impossible?
                || fc.indexIsInFieldList(fc.getFormIndex()) //In field list?
                || fc.getQuestionPrompt() == null) { //In unit test?
            return;
        }

        //Mini method
        Supplier<String> getQuestionName = () -> {
            String raw = fc.getQuestionPrompt().getFormElement()
                    .getBind().getReference().toString();
            return raw.replaceAll(".+/([^/]+)$", "$1");
        };

        try {
            //Remember where we started
            FormIndex startIndex = fc.getFormIndex();
            //Used across iterations
            String precedingMemberName = getQuestionName.get();
            String skippedName = BAD_NAME;
            //Loop until non-question
            while (fc.stepToNextScreenEvent() == FormEntryController.EVENT_QUESTION) {
                //Get question
                FormEntryPrompt question = fc.getQuestionPrompt();
                //Read name
                String questionName = getQuestionName.get();
                //Check for query string
                String query = question.getFormElement()
                        .getAdditionalAttribute(null, "query");
                //No query?
                if (query == null) {
                    //Remember name for later - could be first member of next cascade
                    skippedName = questionName;
                    continue;
                }
                //Second member of next cascade?
                if (queryMatchesName.test(query, skippedName)) {
                    break;
                }
                //Reset anyway (could be about to be hidden)
                fc.saveAnswer(question.getIndex(), null);
                //Found next member of cascade?
                if (queryMatchesName.test(query, precedingMemberName)) {
                    //Ready to carry on looking
                    precedingMemberName = questionName;
                }
            }

            //Back to start
            fc.jumpToIndex(startIndex);

        } catch (JavaRosaException e) {
            Timber.d(e);
        }
    }

    public static @Nullable Selection getSelectedItem(@NotNull FormEntryPrompt prompt, List<SelectChoice> items) {
        IAnswerData answer = prompt.getAnswerValue();
        if (answer == null) {
            return null;
        } else if (answer instanceof SelectOneData) {
            return (Selection) answer.getValue();
        } else if (answer instanceof StringData) { // Fast external itemset
            for (SelectChoice item : items) {
                if (answer.getValue().equals(item.selection().xmlValue)) {
                    return item.selection();
                }
            }
            return null;
        }
        return null;
    }
}
