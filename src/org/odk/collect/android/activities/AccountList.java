/*
 * Copyright 2011 Google Inc. All rights reserved.
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

import android.app.ListActivity;
import android.accounts.*;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.preferences.PreferencesActivity;

/**
 * Provides a popup listing of the accounts on the phone that we can authenticate against.
 * 
 * @author cswenson@google.com (Christopher Swenson)
 */
public class AccountList extends ListActivity {
    protected AccountManager accountManager;


    /**
     * Called to initialize the activity the first time it is run.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.google_account));
    }

    @Override
    protected void onStart() {
    	super.onStart();
		Collect.getInstance().getActivityLogger().logOnStart(this); 
    }
    
    @Override
    protected void onStop() {
		Collect.getInstance().getActivityLogger().logOnStop(this); 
    	super.onStop();
    }

    /**
     * Called when the activity is resumed.
     */
    @Override
    public void onResume() {
        super.onResume();
        accountManager = AccountManager.get(getApplicationContext());
        final Account[] accounts = accountManager.getAccountsByType("com.google");
        this.setListAdapter(new ArrayAdapter<Account>(this, R.layout.account_chooser, accounts) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View row;

                if (convertView == null) {
                    row =
                        ((LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE))
                                .inflate(R.layout.account_chooser, null);
                } else {
                    row = convertView;
                }
                TextView vw = (TextView) row.findViewById(android.R.id.text1);
                vw.setTextSize(Collect.getQuestionFontsize());
                SharedPreferences settings =
                    PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                String selected = settings.getString(PreferencesActivity.KEY_ACCOUNT, "");
                if (accounts[position].name.equals(selected)) {
                    vw.setBackgroundColor(Color.LTGRAY);
                } else {
                    vw.setBackgroundColor(Color.WHITE);
                }
                vw.setText(getItem(position).name);

                return row;
            }
        });
    }


    /**
     * When the user clicks an item, authenticate against that account.
     */
    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Account account = (Account) getListView().getItemAtPosition(position);
        SharedPreferences settings =
            PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        SharedPreferences.Editor editor = settings.edit();
        editor.remove(PreferencesActivity.KEY_AUTH);
        editor.putString(PreferencesActivity.KEY_ACCOUNT, account.name);
        editor.commit();

        Collect.getInstance().getActivityLogger().logAction(this, "select", account.name);
        Intent intent = new Intent(this, AccountInfo.class);
        intent.putExtra("account", account);
        startActivity(intent);
        finish();
    }
}
