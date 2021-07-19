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
import java.util.function.Function;
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

    public static void checkFastExternalCascadeInFieldList(FormIndex lastChangedIndex,
                                                           FormEntryPrompt[] questionsAfterSave) {
        //Quit immediately if no FEI questions
        boolean hasFastExternal = false;
        for (FormEntryPrompt question : questionsAfterSave) {
            hasFastExternal |= question.getFormElement()
                    .getAdditionalAttribute(null, "query") == null;
        }
        if (!hasFastExternal) {
            return;
        }
        FormController fc = Collect.getInstance().getFormController();
        //Formality
        if (fc == null) {
            return;
        }
        //Find the index in the field list
        FormIndex seekIndex = lastChangedIndex;
        FormIndex nextLevel;
        int offset = 1;
        for (; (nextLevel = seekIndex.getNextLevel()) != null; offset++) {
            seekIndex = nextLevel;
        }
        //Read and remember its name, used across iterations
        String matchIndexName = "(\\w+) .+";
        String precedingMemberName = seekIndex.getReference()
                .getSubReference(offset).toShortString()
                .replaceAll(matchIndexName, "$1");
        //Also used across iterations
        String skippedName = BAD_NAME;

        //Mini method
        Function<FormEntryPrompt, String> getQuestionName = q ->
                q.getQuestion().getBind().getReference().toString()
                        .replaceAll(".+/([^/]+)$", "$1");

        //Find first question after updated
        int questionAt = 0;
        String questionName;
        for (; questionAt < questionsAfterSave.length; questionAt++) {
            questionName = getQuestionName.apply(questionsAfterSave[questionAt]);
            if (questionName.equals(precedingMemberName)) {
                break;
            }
        }

        //Check each subsequent question
        for (questionAt++; questionAt < questionsAfterSave.length; questionAt++) {
            //Get question
            FormEntryPrompt question = questionsAfterSave[questionAt];
            //Read name
            questionName = getQuestionName.apply(question);
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
            //Reset anyway (could have been unhidden)
            try {
                fc.saveAnswer(question.getIndex(), null);
            } catch (JavaRosaException e) {
                Timber.d(e);
            }
            //Found next member of cascade?
            if (queryMatchesName.test(query, precedingMemberName)) {
                //Ready to carry on looking
                precedingMemberName = questionName;
            }
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
