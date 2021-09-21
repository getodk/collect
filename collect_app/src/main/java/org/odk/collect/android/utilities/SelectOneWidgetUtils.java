package org.odk.collect.android.utilities;

import androidx.annotation.Nullable;

import org.javarosa.core.model.FormIndex;
import org.javarosa.core.model.SelectChoice;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.data.helper.Selection;
import org.javarosa.form.api.FormEntryController;
import org.javarosa.form.api.FormEntryPrompt;
import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.JavaRosaException;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.widgets.items.ItemsWidget;
import org.odk.collect.android.widgets.items.SelectOneMinimalWidget;
import org.odk.collect.android.widgets.items.SelectOneWidget;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Function;

import timber.log.Timber;

public final class SelectOneWidgetUtils {

    private SelectOneWidgetUtils() {

    }

    //#4500 - Widget per view
    public static void checkFastExternalCascade(ItemsWidget caller) {
        //Exclude untested usage
        if (!(caller instanceof SelectOneWidget || caller instanceof SelectOneMinimalWidget)) {
            throw new IllegalArgumentException("This method is only tested for calls from " +
                    SelectOneWidget.class.getSimpleName() + " or " + SelectOneMinimalWidget.class.getSimpleName());
        }

        FormController fc = Collect.getInstance().getFormController();
        if (fc == null
                //In field list?
                || fc.indexIsInFieldList()
                //In unit test?
                || fc.getQuestionPrompt() == null) {
            return;
        }

        //Store current index, perform check, revert index
        FormIndex thenIndex = fc.getFormIndex();
        try {
            doCascadeCheck(fc);
        } catch (JavaRosaException e) {
            Timber.d(e);
        }
        fc.jumpToIndex(thenIndex);
    }

    //#4500 - Field lists
    public static void checkFastExternalCascadeInFieldList(FormEntryPrompt[] questionsAfterSave,
                                                           FormIndex lastChangedIndex) {
        //Quit immediately if no FEI questions
        boolean hasFastExternal = false;
        for (FormEntryPrompt question : questionsAfterSave) {
            hasFastExternal |= question.getFormElement()
                    .getAdditionalAttribute(null, "query") != null;
        }
        if (!hasFastExternal) {
            return;
        }
        FormController fc = Collect.getInstance().getFormController();
        if (fc == null) {
            return;
        }

        //Store current index, prepare for and perform check, revert index
        FormIndex thenIndex = fc.getFormIndex();
        fc.jumpToIndex(lastChangedIndex);
        try {
            doCascadeCheck(fc);
        } catch (JavaRosaException e) {
            Timber.d(e);
        }
        fc.jumpToIndex(thenIndex);
    }

    private static void doCascadeCheck(FormController fc) throws JavaRosaException {
        //'Macros'
        Function<FormEntryPrompt, String> getQuestionName = (question) ->
                question.getFormElement().getBind()
                        .getReference().toString()
                        .replaceAll(".+/([^/]+)$", "$1");

        BiPredicate<String, String> queryMatchesName = (query, name) ->
                query.matches(".*\\b" + name + "\\b.*");

        //Used across iterations
        String precedingMemberName = getQuestionName.apply(fc.getQuestionPrompt());
        Timber.i("change: %s", precedingMemberName);
        String skippedName = "123";

        //Loop until non-question
        while (fc.stepToNextScreenEvent() == FormEntryController.EVENT_QUESTION) {
            //Get question
            FormEntryPrompt question = fc.getQuestionPrompt();
            //Read name
            String questionName = getQuestionName.apply(question);
            //Check for query string
            String query = question.getFormElement()
                    .getAdditionalAttribute(null, "query");
            //No query?
            if (query == null) {
                //Remember name - could be first member of unrelated cascade
                skippedName = questionName;
                continue;
            }
            //Second member of unrelated cascade?
            if (queryMatchesName.test(query, skippedName)) {
                break;
            }
            //Reset anyway (could be about to be hidden or just unhidden)
            fc.saveAnswer(question.getIndex(), null);
            Timber.i("reset: %s", questionName);
            //Found next member of cascade?
            if (queryMatchesName.test(query, precedingMemberName)) {
                //Ready to carry on checking
                precedingMemberName = questionName;
            }
        }
    }

    public static @Nullable
    Selection getSelectedItem(@NotNull FormEntryPrompt prompt, List<SelectChoice> items) {
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
