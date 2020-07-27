package org.odk.collect.android.widgets;

import android.content.Context;
import android.widget.RadioButton;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.SelectOneData;
import org.javarosa.core.model.data.helper.Selection;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.adapters.SelectOneListAdapter;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.fragments.dialogs.SelectMinimalDialog;
import org.odk.collect.android.fragments.dialogs.SelectOneMinimalDialog;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayColor;

public class SelectOneMinimalWidget extends SelectMinimalWidget {
    private final SelectOneListAdapter adapter;

    public SelectOneMinimalWidget(Context context, QuestionDetails prompt) {
        super(context, prompt);
        adapter = new SelectOneListAdapter(items, getSavedSelectedValue(), null, getFormEntryPrompt(), getReferenceManager(), getAnswerFontSize(), getAudioHelper(), getPlayColor(getFormEntryPrompt(), themeUtils), getContext());
        updateAnswer();
    }

    @Override
    protected void showDialog() {
        SelectOneMinimalDialog dialog = new SelectOneMinimalDialog(adapter, WidgetAppearanceUtils.isFlexAppearance(getFormEntryPrompt()), WidgetAppearanceUtils.isAutocomplete(getFormEntryPrompt()));
        dialog.show(((FormEntryActivity) getContext()).getSupportFragmentManager(), SelectMinimalDialog.class.getName());
    }

    @Override
    protected AbstractSelectListAdapter getAdapter() {
        return adapter;
    }

    @Override
    public IAnswerData getAnswer() {
        Selection selectedItem = adapter.getSelectedItem();
        return selectedItem == null
                ? null
                : new SelectOneData(selectedItem);
    }

    @Override
    public void setChoiceSelected(int choiceIndex, boolean isSelected) {
        RadioButton button = new RadioButton(getContext());
        button.setTag(choiceIndex);
        button.setChecked(isSelected);

        adapter.onCheckedChanged(button, isSelected);
    }

    private String getSavedSelectedValue() {
        return getQuestionDetails().getPrompt().getAnswerValue() == null
                ? null
                : ((Selection) getQuestionDetails().getPrompt().getAnswerValue().getValue()).getValue();
    }
}
