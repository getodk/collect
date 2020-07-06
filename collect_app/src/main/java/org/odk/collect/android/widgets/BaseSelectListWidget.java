package org.odk.collect.android.widgets;

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
import org.odk.collect.android.utilities.SoftKeyboardUtils;
import org.odk.collect.android.utilities.WidgetAppearanceUtils;

import static org.odk.collect.android.analytics.AnalyticsEvents.AUDIO_QUESTION;
import static org.odk.collect.android.formentry.media.FormMediaUtils.getPlayableAudioURI;

public abstract class BaseSelectListWidget extends ItemsWidget {
    private static final String SEARCH_TEXT = "search_text";

    protected SelectListWidgetAnswerBinding binding;
    protected AbstractSelectListAdapter adapter;

    public BaseSelectListWidget(Context context, QuestionDetails questionDetails) {
        super(context, questionDetails);

        logAnalytics(questionDetails);
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = SelectListWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        if (isAutocomplete()) {
            setUpSearchBox();
        }
        return binding.getRoot();
    }

    @Override
    protected void saveState() {
        super.saveState();
        getState().putString(SEARCH_TEXT + getFormEntryPrompt().getIndex(), binding.choicesSearchBox.getText().toString());
    }

    protected boolean isFlex() {
        return WidgetAppearanceUtils.isFlexAppearance(getQuestionDetails().getPrompt());
    }

    protected boolean isAutocomplete() {
        return WidgetAppearanceUtils.isAutocomplete(getQuestionDetails().getPrompt());
    }

    protected int getNumOfColumns() {
        return WidgetAppearanceUtils.getNumberOfColumns(getQuestionDetails().getPrompt(), getContext());
    }

    protected void setUpSearchBox() {
        binding.choicesSearchBox.setVisibility(VISIBLE);
        binding.choicesSearchBox.setTextSize(TypedValue.COMPLEX_UNIT_DIP, getAnswerFontSize());
        setupChangeListener();
        String searchText = null;
        if (getState() != null) {
            searchText = getState().getString(SEARCH_TEXT + getFormEntryPrompt().getIndex());
        }
        if (searchText != null && !searchText.isEmpty()) {
            binding.choicesSearchBox.setText(searchText);
            Selection.setSelection(binding.choicesSearchBox.getText(), binding.choicesSearchBox.getText().toString().length());
        } else {
            doSearch("");
        }
    }

    private void setupChangeListener() {
        binding.choicesSearchBox.addTextChangedListener(new TextWatcher() {
            private String oldText = "";

            @Override
            public void afterTextChanged(Editable s) {
                if (!s.toString().equals(oldText)) {
                    doSearch(s.toString());
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

    private void doSearch(String searchStr) {
        if (adapter != null) {
            adapter.getFilter().filter(searchStr);
        }
    }

    @Override
    public void setFocus(Context context) {
        if (isAutocomplete()) {
            SoftKeyboardUtils.showSoftKeyboard(binding.choicesSearchBox);
        }
    }

    private void logAnalytics(QuestionDetails questionDetails) {
        if (items != null) {
            for (SelectChoice choice : items) {
                String audioURI = getPlayableAudioURI(questionDetails.getPrompt(), choice, getReferenceManager());

                if (audioURI != null) {
                    analytics.logEvent(AUDIO_QUESTION, "AudioChoice", questionDetails.getFormAnalyticsID());
                    break;
                }
            }
        }
    }
}
