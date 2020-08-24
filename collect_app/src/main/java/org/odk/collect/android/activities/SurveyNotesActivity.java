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

public class SurveyNotesActivity extends CollectAbstractActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.survey_note);

        final Button sb = (Button) findViewById(R.id.save_button);
        sb.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FormController formController = Collect.getInstance().getFormController();
                EditText editText = (EditText) findViewById(R.id.survey_notes);
                formController.setSurveyNotes(editText.getText().toString());
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        FormController formController = Collect.getInstance().getFormController();
        EditText editText = (EditText) findViewById(R.id.survey_notes);

        if (formController != null) {
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

}
