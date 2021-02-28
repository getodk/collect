/*
 * Copyright (C) 2016 Smap Consulting
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
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.zxing.integration.android.IntentIntegrator;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.activities.NFCActivity;
import org.odk.collect.android.activities.ScannerWithFlashlightActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.widgets.interfaces.ButtonClickListener;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.utilities.WaitingForDataRegistry;

import static org.odk.collect.android.formentry.questions.WidgetViewUtils.createSimpleButton;
import static org.odk.collect.android.formentry.questions.WidgetViewUtils.getCenteredAnswerTextView;


/**
 * Widget that allows user to scan NFC Id;s and add them to the form.
 * 
 * @author Neil Penman (neilpenman@gmail.com)
 * Based on BarcodeWidget by Yaw Anokwa (yanokwa@gmail.com)
 */
public class NfcWidget extends QuestionWidget implements WidgetDataReceiver, ButtonClickListener {
	private Button mGetNfcButton;
	private TextView mStringAnswer;
    private NfcAdapter mNfcAdapter;
    public PendingIntent mNfcPendingIntent;
    public IntentFilter[] mNfcFilters;

	public NfcWidget(Context context, QuestionDetails questionDetails, WaitingForDataRegistry waitingForDataRegistry) {
		super(context, questionDetails);

		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
		params.setMargins(7, 5, 7, 5);

		// set button formatting
		mGetNfcButton = createSimpleButton(getContext(), View.generateViewId(), getFormEntryPrompt().isReadOnly(), getContext().getString(R.string.smap_read_nfc), getAnswerFontSize(), this);
        mGetNfcButton.setEnabled(!questionDetails.getPrompt().isReadOnly());

		// launch nfc capture intent on click
		mGetNfcButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {

                Intent i = new Intent(getContext(), NFCActivity.class);
                Collect.getInstance().getFormController()
                        .setIndexWaitingForData(getFormEntryPrompt().getIndex());
                ((Activity) getContext()).startActivityForResult(i,
                        ApplicationConstants.RequestCodes.NFC_CAPTURE);

			}
		});

		// set text formatting
		mStringAnswer = getCenteredAnswerTextView(getContext(), getAnswerFontSize());

		String s = questionDetails.getPrompt().getAnswerText();
		if (s != null) {
			mGetNfcButton.setText(getContext().getString(
					R.string.smap_replace_nfc));
			mStringAnswer.setText(s);
		}
		// finish complex layout
		//addView(mGetNfcButton);
		//addView(mStringAnswer);
        LinearLayout answerLayout = new LinearLayout(getContext());
        answerLayout.setOrientation(LinearLayout.VERTICAL);
        answerLayout.addView(mGetNfcButton);
        answerLayout.addView(mStringAnswer);
        addAnswerView(answerLayout);
	}

	@Override
	public void clearAnswer() {
		mStringAnswer.setText(null);
		mGetNfcButton.setText(getContext().getString(R.string.get_barcode));
	}

	@Override
	public IAnswerData getAnswer() {
		String s = mStringAnswer.getText().toString();
		if (s == null || s.equals("")) {
			return null;
		} else {
			return new StringData(s);
		}
	}

	/**
	 * Allows answer to be set externally in {@Link FormEntryActivity}.
	 */
    @Override
    public void setData(Object answer) {
        mStringAnswer.setText((String) answer);
        widgetValueChanged();
    }

	@Override
	public void setFocus(Context context) {
		// Hide the soft keyboard if it's showing.
		InputMethodManager inputManager = (InputMethodManager) context
				.getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		mStringAnswer.setOnLongClickListener(l);
		mGetNfcButton.setOnLongClickListener(l);
	}

	@Override
	public void cancelLongPress() {
		super.cancelLongPress();
		mGetNfcButton.cancelLongPress();
		mStringAnswer.cancelLongPress();
	}

    @Override
    public void onButtonClick(int buttonId) {

        Intent i = new Intent(getContext(), NFCActivity.class);
        Collect.getInstance().getFormController()
                .setIndexWaitingForData(getFormEntryPrompt().getIndex());
        ((Activity) getContext()).startActivityForResult(i,
                ApplicationConstants.RequestCodes.NFC_CAPTURE);
    }
}
