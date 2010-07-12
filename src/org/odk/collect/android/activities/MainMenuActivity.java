/*
 * Copyright (C) 2009 University of Washington
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

import org.odk.collect.android.R;
import org.odk.collect.android.database.FileDbAdapter;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.utilities.FileUtils;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ImageView.ScaleType;

import java.util.ArrayList;

/**
 * Responsible for displaying buttons to launch the major activities. Launches some activities based
 * on returns of others.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends Activity {

    // request codes for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;
    private static final int INSTANCE_CHOOSER = 1;
    private static final int INSTANCE_UPLOADER = 2;

    // menu options
    private static final int MENU_PREFERENCES = Menu.FIRST;
    
    // true if splash screen should be shown during onCreate
    private static boolean mShowSplash = true;

    // buttons
    private Button mEnterDataButton;
    private Button mManageFilesButton;
    private Button mSendDataButton;
    private Button mReviewDataButton;

    // counts for buttons
    private static int mSavedCount;
    private static int mCompletedCount;
    private static int mAvailableCount;
    private static int mFormsCount;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        displaySplash();
        setContentView(R.layout.main_menu);
        setTitle(getString(R.string.app_name) + " > " + getString(R.string.main_menu));

        
        // enter data button. expects a result.
        mEnterDataButton = (Button) findViewById(R.id.enter_data);
        mEnterDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // make sure we haven't added forms
                ArrayList<String> forms = FileUtils.getFilesAsArrayList(FileUtils.FORMS_PATH);
                if (forms != null) {
                    mFormsCount = forms.size();
                } else {
                    mFormsCount = 0;
                }

                if (mFormsCount == 0 && mAvailableCount == 0) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.no_items_error, getString(R.string.enter)),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(getApplicationContext(), FormChooserList.class);
                    startActivityForResult(i, FORM_CHOOSER);
                }

            }
        });

        // review data button. expects a result.
        mReviewDataButton = (Button) findViewById(R.id.review_data);
        mReviewDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if ((mSavedCount + mCompletedCount) == 0) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.no_items_error, getString(R.string.review)),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                    i.putExtra(FileDbAdapter.KEY_STATUS, FileDbAdapter.STATUS_COMPLETE);
                    startActivityForResult(i, INSTANCE_CHOOSER);
                }

            }
        });

        // send data button. expects a result.
        mSendDataButton = (Button) findViewById(R.id.send_data);
        mSendDataButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (mCompletedCount == 0) {
                    Toast.makeText(getApplicationContext(),
                        getString(R.string.no_items_error, getString(R.string.send)),
                        Toast.LENGTH_SHORT).show();
                } else {
                    Intent i = new Intent(getApplicationContext(), InstanceUploaderList.class);
                    startActivityForResult(i, INSTANCE_UPLOADER);
                }

            }
        });

        // manage forms button. no result expected.
        mManageFilesButton = (Button) findViewById(R.id.manage_forms);
        mManageFilesButton.setText(getString(R.string.manage_files));
        mManageFilesButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), FileManagerTabs.class);
                startActivity(i);
            }
        });
    }

    
    /**
     * displaySplash
     * 
     * Shows the splash screen if the mShowSplash member variable is true.
     * Otherwise a no-op.
     */
    void displaySplash() {
    	if ( ! mShowSplash ) return; 
    	
    	// fetch the splash screen Drawable
        Drawable image = null;
        try {
        	// attempt to load the configured default splash screen
    		BitmapDrawable bitImage = new BitmapDrawable( getResources(), 
    										FileUtils.SPLASH_SCREEN_FILE_PATH );
    		if ( bitImage.getBitmap() != null &&
    			 bitImage.getIntrinsicHeight() > 0 &&
    			 bitImage.getIntrinsicWidth() > 0 ) {
    			image = bitImage;
    		}
        }
        catch (Exception e) {
        	// TODO: log exception for debugging?
        }
        finally {
        	if ( image == null ) {
        		// fall-back to our resource if no file or SD card not accessible
        		image = getResources().getDrawable(R.drawable.opendatakit);
        	}
        }

        // create ImageView to hold the Drawable...
    	ImageView view = new ImageView(getApplicationContext());
    	// initialize it with Drawable and full-screen layout parameters
    	view.setImageDrawable(image);
    	int width = getWindowManager().getDefaultDisplay().getWidth();
    	int height = getWindowManager().getDefaultDisplay().getHeight();
    	FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams( width, height, 0 );
    	view.setLayoutParams(lp);
    	view.setScaleType(ScaleType.CENTER);
    	view.setBackgroundColor(Color.WHITE);

    	// and wrap the image view in a frame layout so that the 
    	// full-screen layout parameters are honored...
    	FrameLayout layout = new FrameLayout(getApplicationContext());
    	layout.addView(view);

    	// Create the toast and set the view to be that of the FrameLayout
    	Toast t = Toast.makeText(getApplicationContext(), "splash screen", Toast.LENGTH_SHORT);
    	t.setView(layout);
    	t.setGravity(Gravity.CENTER, 0, 0);
    	t.show();
    }

    /*
     * (non-Javadoc)
     * 
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();
        updateButtons();
    }

    /**
     * onStop
     * Re-enable the splash screen.
     */
    @Override
    protected void onStop() {
    	super.onStop();
    	mShowSplash = true;
    }

    /**
     * Upon return, check intent for data needed to launch other activities.
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_CANCELED) {
            return;
        }

        String formPath = null;
        Intent i = null;
        switch (requestCode) {
            // returns with a form path, start entry
            case FORM_CHOOSER:
                formPath = intent.getStringExtra(FormEntryActivity.KEY_FORMPATH);
                i = new Intent("org.odk.collect.android.action.FormEntry");
                i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);
                startActivity(i);
                break;
            // returns with an instance path, start entry
            case INSTANCE_CHOOSER:
                formPath = intent.getStringExtra(FormEntryActivity.KEY_FORMPATH);
                String instancePath = intent.getStringExtra(FormEntryActivity.KEY_INSTANCEPATH);
                i = new Intent("org.odk.collect.android.action.FormEntry");
                i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);
                i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instancePath);
                startActivity(i);
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }


    /**
     * Updates the button count and sets the text in the buttons.
     */
    private void updateButtons() {
        // create adapter
        FileDbAdapter fda = new FileDbAdapter();
        fda.open();

        // count for saved instances
        Cursor c =
            fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_INCOMPLETE);
        mSavedCount = c.getCount();
        c.close();

        // count for completed instances
        c = fda.fetchFilesByType(FileDbAdapter.TYPE_INSTANCE, FileDbAdapter.STATUS_COMPLETE);
        mCompletedCount = c.getCount();
        c.close();

        // count for downloaded forms
        ArrayList<String> forms = FileUtils.getFilesAsArrayList(FileUtils.FORMS_PATH);
        if (forms != null) {
            mFormsCount = forms.size();
        } else {
            mFormsCount = 0;
        }
        fda.close();

        mEnterDataButton.setText(getString(R.string.enter_data_button, mFormsCount));
        mSendDataButton.setText(getString(R.string.send_data_button, mCompletedCount));
        mReviewDataButton.setText(getString(R.string.review_data_button, mSavedCount
                + mCompletedCount));
    }


    private void createPreferencesMenu() {
        Intent i = new Intent(this, ServerPreferences.class);
        startActivity(i);
    }
 
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        menu.add(0, MENU_PREFERENCES, 0, getString(R.string.server_preferences)).setIcon(
            android.R.drawable.ic_menu_preferences);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_PREFERENCES:
                createPreferencesMenu();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
