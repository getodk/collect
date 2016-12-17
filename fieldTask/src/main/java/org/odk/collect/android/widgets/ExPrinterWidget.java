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

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.Toast;


/**
 * <p>Use the ODK Sensors framework to print data to a connected printer.</p>
 *
 * <p>The default button text is "Print Label"
 *
 * <p>You may override the button text and the error text that is
 * displayed when the app is missing by using jr:itext() values. The
 * special itext form values are 'buttonText' and 'noPrinterErrorString',
 * respectively.</p>
 *
 * <p>To use via XLSForm, specify a 'note' type with a 'calculation' that defines
 * the data to be printed and with an 'appearance' as described below.
 *
 * <p>Within the XForms XML, to use this widget, define an appearance on the
 * &lt;input/&gt; tag that begins "printer:" and then contains the intent
 * action to launch. That intent starts the printer app. The data to print
 * is sent via a broadcast intent to intentname.data The printer then pops
 * a UI to initiate the actual printing (or change the destination printer).
 * </p>
 *
 * <p>Implementation-wise, this widget is an ExStringWidget that is read-only.</p>
 *
 * <p>The ODK Sensors Zebra printer uses this appearance (intent):</p>
 * <pre>
 * "printer:org.opendatakit.sensors.ZebraPrinter"
 * </pre>
 *
 * <p>The data that is printed should be defined in the calculate attribute
 * of the bind. The structure of that string is a &lt;br&gt; separated list
 * of values consisting of:</p>
 * <ul><li>numeric barcode to emit (optional)</li>
 * <li>string qrcode to emit (optional)</li>
 * <li>text line 1 (optional)</li>
 * <li>additional text line (repeat as needed)</li></ul>
 *
 * <p>E.g., if you wanted to emit a barcode of 123, a qrcode of "mycode" and
 * two text lines of "line 1" and "line 2", you would define the calculate
 * as:</p>
 *
 * <pre>
 *  &lt;bind nodeset="/printerForm/printme" type="string" readonly="true()"
 *     calculate="concat('123','&lt;br&gt;','mycode','&lt;br&gt;','line 1','&lt;br&gt;','line 2')" /&gt;
 * </pre>
 *
 * <p>Depending upon what you supply, the printer may print just a
 * barcode, just a qrcode, just text, or some combination of all 3.</p>
 *
 * <p>Despite using &lt;br&gt; as a separator, the supplied Zebra
 * printer does not recognize html.</p>
 *
 * <pre>
 * &lt;input appearance="ex:change.uw.android.TEXTANSWER" ref="/printerForm/printme" &gt;
 * </pre>
 * <p>or, to customize the button text and error strings with itext:
 * <pre>
 *      ...
 *      &lt;bind nodeset="/printerForm/printme" type="string" readonly="true()" calculate="concat('&lt;br&gt;',
 *       /printerForm/some_text ,'&lt;br&gt;Text: ', /printerForm/shortened_text ,'&lt;br&gt;Integer: ',
 *       /printerForm/a_integer ,'&lt;br&gt;Decimal: ', /printerForm/a_decimal )"/&gt;
 *      ...
 *      &lt;itext&gt;
 *        &lt;translation lang="English"&gt;
 *          &lt;text id="printAnswer"&gt;
 *            &lt;value form="short"&gt;Print your label&lt;/value&gt;
 *            &lt;value form="long"&gt;Print your label&lt;/value&gt;
 *            &lt;value form="buttonText"&gt;Print now&lt;/value&gt;
 *            &lt;value form="noPrinterErrorString"&gt;ODK Sensors Zebra Printer is not installed!
 *             Please install ODK Sensors Framework and ODK Sensors Zebra Printer from Google Play.&lt;/value&gt;
 *          &lt;/text&gt;
 *        &lt;/translation&gt;
 *      &lt;/itext&gt;
 *    ...
 *    &lt;input appearance="printer:org.opendatakit.sensors.ZebraPrinter" ref="/form/printme"&gt;
 *      &lt;label ref="jr:itext('printAnswer')"/&gt;
 *    &lt;/input&gt;
 * </pre>
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class ExPrinterWidget extends QuestionWidget implements IBinaryWidget {

    private Button mLaunchIntentButton;

    public ExPrinterWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);

        String appearance = prompt.getAppearanceHint();
        String[] attrs = appearance.split(":");
        final String intentName = (attrs.length < 2 || attrs[1].length() == 0) ? "org.opendatakit.sensors.ZebraPrinter" : attrs[1];
        final String buttonText;
        final String errorString;
    	String v = mPrompt.getSpecialFormQuestionText("buttonText");
    	buttonText = (v != null) ? v : context.getString(R.string.launch_printer);
    	v = mPrompt.getSpecialFormQuestionText("noPrinterErrorString");
    	errorString = (v != null) ? v : context.getString(R.string.no_printer);

        // set button formatting
        mLaunchIntentButton = new Button(getContext());
        mLaunchIntentButton.setId(QuestionWidget.newUniqueId());
        mLaunchIntentButton.setText(buttonText);
        mLaunchIntentButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mLaunchIntentButton.setPadding(20, 20, 20, 20);
        mLaunchIntentButton.setLayoutParams(params);

        mLaunchIntentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                	Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
                	firePrintingActivity(intentName);
                } catch (ActivityNotFoundException e) {
                	Collect.getInstance().getFormController().setIndexWaitingForData(null);
                    Toast.makeText(getContext(),
                    		errorString, Toast.LENGTH_SHORT)
                            .show();
                }
            }
        });

        // finish complex layout
        addView(mLaunchIntentButton);
    }

    protected void firePrintingActivity(String intentName) throws ActivityNotFoundException {

        String s = mPrompt.getAnswerText();

       	Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchPrinter",
       			intentName, mPrompt.getIndex());
       	Intent i = new Intent(intentName);
       	((Activity) getContext()).startActivity(i);

       	String[] splits;
       	if ( s != null ) {
       		splits = s.split("<br>");
       	} else {
       		splits = null;
       	}

    	Bundle printDataBundle = new Bundle();

    	String e;
    	if (splits != null) {
    		if ( splits.length >= 1 ) {
    			e = splits[0];
    			if ( e.length() > 0) {
    				printDataBundle.putString("BARCODE", e);
    			}
    		}
    		if ( splits.length >= 2 ) {
    			e = splits[1];
    			if ( e.length() > 0) {
    				printDataBundle.putString("QRCODE", e);
    			}
    		}
    		if ( splits.length > 2 ) {
	    		String[] text = new String[splits.length-2];
	    		for ( int j = 2 ; j < splits.length ; ++j ) {
	    			e = splits[j];
	    			text[j-2] = e;
	    		}
				printDataBundle.putStringArray("TEXT-STRINGS", text);
    		}
    	}

    	//send the printDataBundle to the activity via broadcast intent
    	Intent bcastIntent = new Intent(intentName + ".data");
    	bcastIntent.putExtra("DATA", printDataBundle);
    	((Activity) getContext()).sendBroadcast(bcastIntent);
    }

    @Override
    public void clearAnswer() {
    }


    @Override
    public IAnswerData getAnswer() {
    	return mPrompt.getAnswerValue();
    }


    /**
     * Allows answer to be set externally in {@Link FormEntryActivity}.
     */
    @Override
    public void setBinaryData(Object answer) {
    	Collect.getInstance().getFormController().setIndexWaitingForData(null);
    }

    @Override
    public void setFocus(Context context) {
        // focus on launch button
        mLaunchIntentButton.requestFocus();
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return mPrompt.getIndex().equals(Collect.getInstance().getFormController().getIndexWaitingForData());
    }

	@Override
	public void cancelWaitingForBinaryData() {
    	Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.isAltPressed() == true) {
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mLaunchIntentButton.setOnLongClickListener(l);
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLaunchIntentButton.cancelLongPress();
    }


}
