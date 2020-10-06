package org.odk.collect.android.widgets.items;

import android.app.Activity;
import android.content.Context;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.View;

import org.javarosa.core.model.SelectChoice;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.adapters.AbstractSelectListAdapter;
import org.odk.collect.android.databinding.SelectListWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.listeners.SelectItemClickListener;
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;
import org.odk.collect.android.widgets.interfaces.MultiChoiceWidget;

import static org.odk.collect.android.analytics.AnalyticsEvents.PROMPT;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;

public abstract class BaseSelectListWidget extends ItemsWidget implements MultiChoiceWidget, SelectItemClickListener {
    private static final String SEARCH_TEXT = "search_text";

    SelectListWidgetAnswerBinding binding;
    protected AbstractSelectListAdapter recyclerViewAdapter;

    public BaseSelectListWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);
        logAnalytics(questionDetails);
        binding.choicesRecyclerView.initRecyclerView(setUpAdapter(), WidgetAppearanceUtils.isFlexAppearance(getQuestionDetails().getPrompt()));
        restoreSavedSearchText();
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectListWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        if (WidgetAppearanceUtils.isAutocomplete(getQuestionDetails().getPrompt())) {
            setUpSearchBox();
        }
        return binding.getRoot();
    }

    @Override
    protected void saveState() {
        super.saveState();
        getState().putString(SEARCH_TEXT + getFormEntryPrompt().getIndex(), binding.choicesSearchBox.getText().toString());
    }

    @Override
    public void setFocus(Context context) {
        if (WidgetAppearanceUtils.isAutocomplete(getQuestionDetails().getPrompt())) {
            SoftKeyboardUtils.showSoftKeyboard(binding.choicesSearchBox);
        }
    }

    @Override
    public void clearAnswer() {
        recyclerViewAdapter.clearAnswer();
        widgetValueChanged();
    }

    @Override
    public int getChoiceCount() {
        return recyclerViewAdapter.getItemCount();
    }

    private void setUpSearchBox() {
        binding.choicesSearchBox.setVisibility(View.VISIBLE);
        binding.choicesSearchBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        binding.choicesSearchBox.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    recyclerViewAdapter.getFilter().filter(s.toString());
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                oldText = s.toString();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });
    }

    private void restoreSavedSearchText() {
        String searchText = null;
        if (getState() != null) {
            searchText = getState().getString(SEARCH_TEXT + getFormEntryPrompt().getIndex());
        }
        if (searchText != null && !searchText.isEmpty()) {
            binding.choicesSearchBox.setText(searchText);
            Selection.setSelection(binding.choicesSearchBox.getText(), binding.choicesSearchBox.getText().toString().length());
        }
    }

    private void logAnalytics(QuestionDetails questionDetails) {
        if (items != null) {
            for (SelectChoice choice : items) {
                String audioURI = getPlayableAudioURI(questionDetails.getPrompt(), choice, getReferenceManager());

                if (audioURI != null) {
                    analytics.logEvent(PROMPT, "AudioChoice", questionDetails.getFormAnalyticsID());
                    break;
                }
            }
        }
    }

    protected abstract AbstractSelectListAdapter setUpAdapter();
}
