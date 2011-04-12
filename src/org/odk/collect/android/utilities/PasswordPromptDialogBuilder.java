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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.widget.EditText;

public final class PasswordPromptDialogBuilder {
	public static final String TITLE = "Enter host password";
	public static final String PROMPT = "Enter password for ";
	public static final String ON = " on ";
	
	public interface OnOkListener {
		public void onOk(Object okListenerContext);
	}
	
	private final AlertDialog.Builder b;
	private final String userEmail;
	private final String host;
	private final EditText input;
	private final OnOkListener okListener;
	private final Object okListenerContext;
	
	public PasswordPromptDialogBuilder( Context ctxt, 
										String userEmail, 
										String host,
										OnOkListener okListener,
										Object okListenerContext) {
		b = new AlertDialog.Builder(ctxt);
		this.userEmail = userEmail;
		this.host = host;
		this.okListener = okListener;
		this.okListenerContext = okListenerContext;
		
		b.setTitle(TITLE);
		b.setMessage(PROMPT + userEmail + ON + host);
		input = new EditText(ctxt);
		input.setInputType( InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
		b.setView(input);
		b.setPositiveButton("OK", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String password = input.getText().toString();
				WebUtils.addCredentials(PasswordPromptDialogBuilder.this.userEmail, password, PasswordPromptDialogBuilder.this.host);
				PasswordPromptDialogBuilder.this.okListener.onOk(PasswordPromptDialogBuilder.this.okListenerContext);
			}
		});
	}

	public void show() {
		b.show();
	}
}
