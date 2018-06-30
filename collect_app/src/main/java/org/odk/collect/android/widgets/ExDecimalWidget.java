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

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.external.ExternalAppsUtils;

import java.text.NumberFormat;
import java.util.Locale;

import static org.odk.collect.android.utilities.ApplicationConstants.RequestCodes;


/**
 * Launch an external app to supply a decimal value. If the app
 * does not launch, enable the text area for regular data entry.
 * <p>
 * See {@link org.odk.collect.android.widgets.ExStringWidget} for usage.
 *
 * @author mitchellsundt@gmail.com
 */
public class ExDecimalWidget extends ExStringWidget {

    public ExDecimalWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        answer.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);

        // only allows numbers and no periods
        answer.setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        answer.setFilters(fa);

        Double d = getDoubleAnswerValue();

        if (d != null) {
            // truncate to 15 digits max in US locale
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            String formattedValue = nf.format(d);
            answer.setText(formattedValue);

            Selection.setSelection(answer.getText(), answer.getText().length());
        }
    }

    private Double getDoubleAnswerValue() {
        IAnswerData dataHolder = getFormEntryPrompt().getAnswerValue();
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Integer) {
                    d = (double) (Integer) dataValue;
                } else {
                    d = (Double) dataValue;
                }
            }
        }
        return d;
    }

    @Override
    protected void fireActivity(Intent i) throws ActivityNotFoundException {
        i.putExtra("value", getDoubleAnswerValue());
        Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchIntent",
                i.getAction(), getFormEntryPrompt().getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                RequestCodes.EX_DECIMAL_CAPTURE);
    }


    @Override
    public IAnswerData getAnswer() {
        String s = answer.getText().toString();
        if (s.equals("")) {
            return null;
        } else {
            try {
                return new DecimalData(Double.valueOf(s));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }


    /**
     * Allows answer to be set externally in {@link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
        DecimalData decimalData = ExternalAppsUtils.asDecimalData(answer);
        this.answer.setText(decimalData == null ? null : decimalData.getValue().toString());
    }
}
