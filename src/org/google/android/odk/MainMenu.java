/*
 * Copyright (C) 2009 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package org.google.android.odk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

import org.javarosa.core.JavaRosaServiceProvider;
import org.javarosa.core.services.IService;
import org.javarosa.core.services.UnavailableServiceException;

import java.util.Vector;

/**
 * Responsible for displaying buttons to launch the major activities. Also
 * launches some activities based on returns of others.
 * 
 * @author Carl Hartung
 */
public class MainMenu extends Activity {

    private static final String t = "MainMenu";

    // The request code for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;


    /**
     * Create View with buttons to launch activities.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        
        super.onCreate(savedInstanceState);

        setContentView(R.layout.mainmenu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        Button chooseform = (Button) findViewById(R.id.chooseform);
        chooseform.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormChooser.class);
                startActivityForResult(i, FORM_CHOOSER);
            }
        });

        Button manageforms = (Button) findViewById(R.id.manageform);
        manageforms.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FormManager.class);
                startActivity(i);
            }
        });

        Button senddata = (Button) findViewById(R.id.senddata);
        senddata.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Toast.makeText(getApplicationContext(), "Sorry, not implemented...",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Upon return, check intent for data needed to launch other activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (resultCode == RESULT_CANCELED) {
            // The request was canceled, so do nothing.
            return;
        }

        Bundle extras = intent.getExtras();

        switch (requestCode) {
            // if form chooser returns with a form name, start entry
            case FORM_CHOOSER:
                String formPath = extras.getString(SharedConstants.FORMPATH_KEY);
                Intent i = new Intent(this, FormEntry.class);
                i.putExtra(SharedConstants.FORMPATH_KEY, formPath);
                startActivity(i);
                break;
        }
    }
}
