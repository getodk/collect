/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.utilities;

import org.odk.collect.android.R;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.widget.EditText;

/**
 * Simple builder for an Alert dialog that will prompt for a password for the given user on a
 * specific host. The OK action will set the credentials for the user in the http context and then
 * call a listener callback passing a context parameter.
 * 
 * @author mitchellsundt@gmail.com
 */
public final class PasswordPromptDialogBuilder {

    public interface OnOkListener {
        public void onOk(Object okListenerContext);
    }

    private final AlertDialog.Builder b;
    private final String userEmail;
    private final String host;
    private final EditText input;
    private final OnOkListener okListener;
    private final Object okListenerContext;


    public PasswordPromptDialogBuilder(Context ctxt, String userEmail, String host,
            OnOkListener okListener, Object okListenerContext) {
        b = new AlertDialog.Builder(ctxt);
        this.userEmail = userEmail;
        this.host = host;
        this.okListener = okListener;
        this.okListenerContext = okListenerContext;

        b.setTitle(ctxt.getString(R.string.enter_host_password));
        b.setMessage(ctxt.getString(R.string.enter_password, userEmail, host));
        input = new EditText(ctxt);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        b.setView(input);
        b.setPositiveButton(ctxt.getString(R.string.ok), new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String password = input.getText().toString();
                WebUtils.addCredentials(PasswordPromptDialogBuilder.this.userEmail, password,
                    PasswordPromptDialogBuilder.this.host);
                PasswordPromptDialogBuilder.this.okListener
                        .onOk(PasswordPromptDialogBuilder.this.okListenerContext);
            }
        });
    }


    public void show() {
        b.show();
    }
}
