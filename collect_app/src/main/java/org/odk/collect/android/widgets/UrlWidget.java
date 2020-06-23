/*
 * Copyright (C) 2013 Nafundi LLC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.databinding.UrlWidgetAnswerBinding;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.CustomTabHelper;
import org.odk.collect.android.utilities.ToastUtils;

@SuppressLint("ViewConstructor")
public class UrlWidget extends QuestionWidget {

    private final CustomTabHelper customTabHelper;
    private UrlWidgetAnswerBinding binding;

    public UrlWidget(Context context, QuestionDetails questionDetails, CustomTabHelper customTabHelper) {
        super(context, questionDetails);
        this.customTabHelper = customTabHelper;
    }

    @Override
    protected View onCreateAnswerView(Context context, FormEntryPrompt prompt, int answerFontSize) {
        binding = UrlWidgetAnswerBinding.inflate(((Activity) context).getLayoutInflater());
        View answerView = binding.getRoot();

        if (prompt.isReadOnly()) {
            binding.urlButton.setVisibility(GONE);
        } else {
            binding.urlButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
            binding.urlButton.setOnClickListener(v -> onButtonClick());
        }

        binding.urlAnswerText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, answerFontSize);
        binding.urlAnswerText.setText(prompt.getAnswerText());

        return answerView;
    }

    @Override
    public void clearAnswer() {
        ToastUtils.showShortToast("URL is readonly");
    }

    @Override
    public IAnswerData getAnswer() {
        String answerText = binding.urlAnswerText.getText().toString();
        return !answerText.isEmpty()
                ? new StringData(answerText)
                : null;
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        binding.urlButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        binding.urlButton.cancelLongPress();
        binding.urlAnswerText.cancelLongPress();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (customTabHelper.getServiceConnection() != null) {
            getContext().unbindService(customTabHelper.getServiceConnection());
        }
    }

    public void onButtonClick() {
        if (!isUrlEmpty(binding.urlAnswerText)) {
            customTabHelper.bindCustomTabsService(getContext(), null);
            customTabHelper.openUri(getContext(), getUri());
        } else {
            ToastUtils.showShortToast("No URL set");
        }
    }

    protected UrlWidgetAnswerBinding getBinding() {
        return binding;
    }

    private boolean isUrlEmpty(TextView stringAnswer) {
        return stringAnswer == null || stringAnswer.getText() == null
                || stringAnswer.getText().toString().isEmpty();
    }

    private Uri getUri() {
        return Uri.parse(binding.urlAnswerText.getText().toString());
    }
}