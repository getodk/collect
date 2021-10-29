/*
 * Copyright (C) 2012 University of Washington
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

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;

import android.annotation.SuppressLint;
import android.content.Context;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.externaldata.ExternalAppsUtils;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.widgets.utilities.StringRequester;
import org.odk.collect.android.widgets.utilities.StringWidgetUtils;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import java.io.Serializable;

/**
 * Launch an external app to supply an integer value. If the app
 * does not launch, enable the text area for regular data entry.
 * <p>
 * See {@link org.odk.collect.android.widgets.ExStringWidget} for usage.
 */
@SuppressLint("ViewConstructor")
public class ExIntegerWidget extends ExStringWidget {

    public ExIntegerWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry, StringRequester stringRequester) {
        super(context, questionDetails, waitingForDataRegistry, stringRequester);
        StringWidgetUtils.adjustEditTextAnswerToIntegerWidget(answerText, questionDetails.getPrompt());
    }

    @Override
    protected Serializable getAnswerForIntent()  {
        return StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(getFormEntryPrompt().getAnswerValue());
    }

    @Override
    protected int getRequestCode() {
        return RequestCodes.EX_INT_CAPTURE;
    }

    @Override
    public IAnswerData getAnswer() {
        return StringWidgetUtils.getIntegerData(answerText.getText().toString(), getFormEntryPrompt());
    }

    @Override
    public void setData(Object answer) {
        IntegerData integerData = ExternalAppsUtils.asIntegerData(answer);
        answerText.setText(integerData == null ? null : integerData.getValue().toString());
        widgetValueChanged();
    }
}
