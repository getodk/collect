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

package org.odk.collect.android.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.javarosawrapper.FormController;

import timber.log.Timber;

public class SurveyNotesActivity extends CollectAbstractActivity {

    private static final String COMMENT_TEXT = "commentText";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_note);

        final Button sb = findViewById(R.id.save_button);
        sb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FormController formController = Collect.getInstance().getFormController();
                EditText editText = findViewById(R.id.survey_notes);
                if(formController == null) {
                    Timber.e("formcontroller null");
                } else {
                    formController.setSurveyNotes(editText.getText().toString());
                }
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FormController formController = Collect.getInstance().getFormController();
        EditText editText = findViewById(R.id.survey_notes);

        String savedText = editText.getText().toString();

        if( savedText != null && savedText.length() > 0) {  // // restore
            editText.setText(savedText, TextView.BufferType.EDITABLE);
        } else if (formController != null) {
            String notes = formController.getSurveyNotes();
            String xpath = formController.getXPath(formController.getFormIndex());
            String qname = "";
            int qIdx = xpath.lastIndexOf('/');
            if (qIdx >= 0) {
                qname = xpath.substring(qIdx + 1);
            }
            if (qname.endsWith("[1]")) {
                qname = qname.substring(0, qname.length() - 3);
            }
            if (notes == null) {
                notes = "";
            }

            if (qname.length() > 0 && !notes.contains(qname) && notes.length() > 0) {
                notes += "\n\r\n\r[" + qname + "]\n\r";
            } else if (qname.length() > 0 && !notes.contains(qname)) {
                notes += "[" + qname + "]\n\r";
            }

            editText.setText(notes, TextView.BufferType.EDITABLE);
            int offset = notes.indexOf("[" + qname + "]") + qname.length() + 4;
            int nextComment = notes.indexOf('[', offset);
            int cursorLocn;
            if (nextComment >= 0) {
                cursorLocn = nextComment - 1;
            } else {
                cursorLocn = notes.length();
            }

            editText.setSelection(cursorLocn);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {

        EditText editText = findViewById(R.id.survey_notes);
        outState.putString(COMMENT_TEXT, editText.getText().toString());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle state) {
        super.onRestoreInstanceState(state);
        EditText editText = findViewById(R.id.survey_notes);
        editText.setText(state.getString(COMMENT_TEXT), TextView.BufferType.EDITABLE);
    }


}
