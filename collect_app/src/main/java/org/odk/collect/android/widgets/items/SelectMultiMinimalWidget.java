package org.odk.collect.android.widgets.items;

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectMultiData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.fragments.dialogs.SelectMultiMinimalDialog;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.HtmlUtils;
import org.odk.collect.android.utilities.Appearances;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;
import org.odk.collect.android.widgets.warnings.SpacesInUnderlyingValuesWarning;

import java.util.ArrayList;
import java.util.List;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

@SuppressLint("ViewConstructor")
public class SelectMultiMinimalWidget extends SelectMinimalWidget {
    private List<Selection> selectedItems;

    public SelectMultiMinimalWidget(Context context, QuestionDetails prompt, WaitingForDataRegistry waitingForDataRegistry) {
        super(context, prompt, waitingForDataRegistry);
        selectedItems = getFormEntryPrompt().getAnswerValue() == null
                ? new ArrayList<>() :
                (List<Selection>) getFormEntryPrompt().getAnswerValue().getValue();
        updateAnswerLabel();
        SpacesInUnderlyingValuesWarning
                .forQuestionWidget(this)
                .renderWarningIfNecessary(items);
    }

    @Override
    protected void showDialog() {
        int numColumns = Appearances.getNumberOfColumns(getFormEntryPrompt(), screenUtils);
        boolean noButtonsMode = Appearances.isCompactAppearance(getFormEntryPrompt()) || Appearances.isNoButtonsAppearance(getFormEntryPrompt());

        SelectMultiMinimalDialog dialog = new SelectMultiMinimalDialog(new ArrayList<>(selectedItems),
                Appearances.isFlexAppearance(getFormEntryPrompt()),
                Appearances.isAutocomplete(getFormEntryPrompt()), getContext(), items,
                getFormEntryPrompt(), getReferenceManager(),
                getPlayColor(getFormEntryPrompt(), themeUtils), numColumns, noButtonsMode);

        DialogUtils.showIfNotShowing(dialog, SelectMinimalDialog.class, ((FormEntryActivity) getContext()).getSupportFragmentManager());
    }

    @Override
    public IAnswerData getAnswer() {
        return selectedItems.isEmpty()
                ? null
                : new SelectMultiData(selectedItems);
    }

    @Override
    public void clearAnswer() {
        selectedItems = new ArrayList<>();
        super.clearAnswer();
    }

    @Override
    public void setData(Object answer) {
        selectedItems = (List<Selection>) answer;
        updateAnswerLabel();
        widgetValueChanged();
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        if (isSelected) {
            selectedItems.add(items.get(choiceIndex).selection());
        } else {
            selectedItems.remove(items.get(choiceIndex).selection());
        }
    }

    private void updateAnswerLabel() {
        if (selectedItems.isEmpty()) {
            binding.answer.setText(R.string.select_answer);
        } else {
            StringBuilder builder = new StringBuilder();
            for (Selection selectedItem : selectedItems) {
                builder.append(getFormEntryPrompt().getSelectItemText(selectedItem));
                if (selectedItems.size() - 1 > selectedItems.indexOf(selectedItem)) {
                    builder.append(", ");
                }
            }
            binding.answer.setText(HtmlUtils.textToHtml(builder.toString()));
        }
    }
}
