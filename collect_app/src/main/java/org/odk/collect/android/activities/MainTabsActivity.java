/*
 * Copyright (C) 2011 Cloudtec Pty Ltd
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

/**
 * Responsible for managing the tabs on the main screen.
 * 
 * @author Neil Penman 
 */

package org.odk.collect.android.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.listeners.NFCListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.preferences.AdminPreferencesActivity;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.tasks.NdefReaderTask;
import org.odk.collect.android.utilities.CompatibilityUtils;
import org.odk.collect.android.R;
import org.odk.collect.android.listeners.TaskDownloaderListener;
import org.odk.collect.android.loaders.TaskEntry;
import org.odk.collect.android.taskModel.NfcTrigger;
import org.odk.collect.android.tasks.DownloadTasksTask;
import org.odk.collect.android.utilities.ManageForm;
import org.odk.collect.android.utilities.Utilities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class MainTabsActivity extends TabActivity implements 
		TaskDownloaderListener,
        NFCListener,
		InstanceUploaderListener,
		FormDownloaderListener{

    private static final String TAG = "MainTabsActivity";
    private AlertDialog mAlertDialog;
    private static final int PROGRESS_DIALOG = 1;
    private static final int ALERT_DIALOG = 2;
	private static final int PASSWORD_DIALOG = 3;
    
 // request codes for returning chosen form to main menu.
    private static final int FORM_CHOOSER = 0;
    private static final int INSTANCE_UPLOADER = 2;
    
    private static final int MENU_PREFERENCES = Menu.FIRST;
	private static final int MENU_ADMIN = Menu.FIRST + 1;
    private static final int MENU_ENTERDATA = Menu.FIRST + 2;
    private static final int MENU_MANAGEFILES = Menu.FIRST + 3;
    private static final int MENU_SENDDATA = Menu.FIRST + 4;
    private static final int MENU_GETTASKS = Menu.FIRST + 5;
    private static final int MENU_GETFORMS = Menu.FIRST + 6;

	private NfcAdapter mNfcAdapter;		// NFC
	public NdefReaderTask mReadNFC;
    public ArrayList<NfcTrigger> nfcTriggersList;   // nfcTriggers (geofence should have separate list)
    public ArrayList<NfcTrigger> nfcTriggersMap;    // nfcTriggers (geofence should have separate list)
    public PendingIntent mNfcPendingIntent;
    public IntentFilter[] mNfcFilters;

    private String mProgressMsg;
    private String mAlertMsg;
    private ProgressDialog mProgressDialog;  
    public DownloadTasksTask mDownloadTasks;
	private Context mContext;
	private SharedPreferences mAdminPreferences;
    private Thread locnThread = null;
	
	private TextView mTVFF;
	private TextView mTVDF;

    private MainTabsListener listener = null;
    boolean listenerRegistered = false;
    private static List<TaskEntry> mTasks = null;
    //private static List<TaskEntry> mMapTasks = null;  Disable map tab
    private static SharedPreferences settings = null;
    private TabHost tabHost = null;
    
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    

        // must be at the beginning of any activity that can be called from an external intent
        try {
            Collect.createODKDirs();
        } catch (RuntimeException e) {
            createErrorDialog(e.getMessage(), true);
            return;
        }
        
	    setContentView(R.layout.main_tabs);

	    Resources res = getResources();  // Resource object to get Drawables
	    tabHost = getTabHost();  // The activity TabHost
	    TabHost.TabSpec spec;  
	    Intent intent;  

		tabHost.setBackgroundColor(Color.WHITE);
		tabHost.getTabWidget().setBackgroundColor(Color.DKGRAY);
		
	    // Initialise a TabSpec and intent for each tab and add it to the TabHost
	    intent = new Intent().setClass(this, MainListActivity.class);    
	    spec = tabHost.newTabSpec("taskList").setIndicator(getString(R.string.smap_taskList)).setContent(intent);
	    tabHost.addTab(spec);

        // Add listener
        listener = new MainTabsListener(this);

	    /*
	     * Initialise a Map tab
	     */
        Log.i(TAG, "Creating Maps Activity");
	    intent = new Intent().setClass(this, MapsActivity.class);
	    spec = tabHost.newTabSpec("taskMap").setIndicator(getString(R.string.smap_taskMap)).setContent(intent);
	    tabHost.addTab(spec);

		// hack to set font size
		LinearLayout ll = (LinearLayout) tabHost.getChildAt(0);
		TabWidget tw = (TabWidget) ll.getChildAt(0);

		int fontsize = Collect.getQuestionFontsize();

		ViewGroup rllf = (ViewGroup) tw.getChildAt(0);
		mTVFF = getTextViewChild(rllf);
		if (mTVFF != null) {
			mTVFF.setTextSize(fontsize);
			mTVFF.setTextColor(Color.WHITE);
			mTVFF.setPadding(0, 0, 0, 6);
		}

		ViewGroup rlrf = (ViewGroup) tw.getChildAt(1);
		mTVDF = getTextViewChild(rlrf);
		if (mTVDF != null) {
			mTVDF.setTextSize(fontsize);
			mTVDF.setTextColor(Color.WHITE);
			mTVDF.setPadding(0, 0, 0, 6);
		}

        /*
		 * NFC
		 */
        boolean authorised = false;
        if (settings == null) {
            settings = PreferenceManager.getDefaultSharedPreferences(this);
        }

        if (settings.getBoolean(PreferencesActivity.KEY_STORE_LOCATION_TRIGGER, true)) {
            mNfcAdapter = NfcAdapter.getDefaultAdapter(this);
            authorised = true;
        } else {
            Toast.makeText(
                    MainTabsActivity.this,
                    getString(R.string.smap_nfc_not_authorised),
                    Toast.LENGTH_SHORT).show();
        }

        if(authorised) {
            if (mNfcAdapter == null) {
                Toast.makeText(
                        MainTabsActivity.this,
                        getString(R.string.smap_nfc_not_available),
                        Toast.LENGTH_SHORT).show();
            } else if (!mNfcAdapter.isEnabled()) {
                Toast.makeText(
                        MainTabsActivity.this,
                        getString(R.string.smap_nfc_not_enabled),
                        Toast.LENGTH_SHORT).show();
            } else {
                /*
                 * Set up NFC adapter
                 */

                // Pending intent
                Intent nfcIntent = new Intent(getApplicationContext(), getClass());
                nfcIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                mNfcPendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, nfcIntent, 0);

                // Filter
                IntentFilter filter = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
                mNfcFilters = new IntentFilter[]{
                        filter
                };


                Toast.makeText(
                        MainTabsActivity.this,
                        getString(R.string.smap_nfc_is_available),
                        Toast.LENGTH_SHORT).show();

            }
        }
    }
	
	private TextView getTextViewChild(ViewGroup viewGroup) {
		for (int i = 0; i < viewGroup.getChildCount(); i++) {
			View view = viewGroup.getChildAt(i);
			if (view instanceof TextView) {
				return (TextView) view;
			}
		}
		return null;
	}
	
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

		CompatibilityUtils.setShowAsAction(
				menu.add(0, MENU_ENTERDATA, 0, R.string.enter_data).setIcon(
						android.R.drawable.ic_menu_edit),
						MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_GETTASKS, 1, R.string.smap_get_tasks).setIcon(
                        android.R.drawable.ic_menu_rotate),
                MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_PREFERENCES, 2, R.string.server_preferences).setIcon(
                        android.R.drawable.ic_menu_preferences),
                MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_GETFORMS, 3, R.string.get_forms).setIcon(
                        android.R.drawable.ic_input_add),
                MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_SENDDATA, 4, R.string.send_data).setIcon(
                        android.R.drawable.ic_menu_send),
                MenuItem.SHOW_AS_ACTION_IF_ROOM);
		
		CompatibilityUtils.setShowAsAction(
                menu.add(0, MENU_MANAGEFILES, 5, R.string.manage_files).setIcon(
                        android.R.drawable.ic_delete),
                MenuItem.SHOW_AS_ACTION_IF_ROOM);
	
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_ENTERDATA:
            	processEnterData();
            	return true;
            case MENU_PREFERENCES:
            	createPreferencesMenu();
                return true;
            case MENU_GETFORMS:
            	processGetForms();	
            	return true;
            case MENU_SENDDATA:
            	processSendData();
            	return true;
            case MENU_GETTASKS:
                processGetTask();	
                return true;
            case MENU_MANAGEFILES:
            	processManageFiles();
            	return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    
    private void createErrorDialog(String errorMsg, final boolean shouldExit) {
        mAlertDialog = new AlertDialog.Builder(this).create();
        mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
        mAlertDialog.setMessage(errorMsg);
        DialogInterface.OnClickListener errorListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                switch (i) {
                    case DialogInterface.BUTTON1:
                        if (shouldExit) {
                            finish();
                        }
                        break;
                }
            }
        };
        mAlertDialog.setCancelable(false);
        mAlertDialog.setButton(getString(R.string.ok), errorListener);
        mAlertDialog.show();
    }
    
    /*
     * Process menu options
     */
    public void createPreferencesMenu() {
        Intent i = new Intent(this, PreferencesActivity.class);
        startActivity(i);
    }
    
    private void processEnterData() {
    	Intent i = new Intent(getApplicationContext(), org.odk.collect.android.activities.FormChooserList.class);
        startActivityForResult(i, FORM_CHOOSER);
    }
    
    // Get new forms
    private void processGetForms() {   
    	
		Collect.getInstance().getActivityLogger().logAction(this, "downloadBlankForms", "click");
		Intent i = new Intent(getApplicationContext(), FormDownloadList.class);
		startActivity(i);
    }
    
    // Send data
    private void processSendData() {
    	Intent i = new Intent(getApplicationContext(), org.odk.collect.android.activities.InstanceUploaderList.class);
        startActivityForResult(i, INSTANCE_UPLOADER);
    }
    
    // Get tasks from the task management server
    private void processGetTask() {   
    	
    	mProgressMsg = getString(R.string.smap_synchronising);	
    	showDialog(PROGRESS_DIALOG);
        mDownloadTasks = new DownloadTasksTask();
        mDownloadTasks.setDownloaderListener(this, mContext);
        mDownloadTasks.execute();
    }
    
	/*
	 * Download task methods
	 */
    @Override
	public void progressUpdate(String progress) {
		mProgressMsg = progress;
		mProgressDialog.setMessage(mProgressMsg);		
	}
	
    private void processManageFiles() {
    	Intent i = new Intent(getApplicationContext(), org.odk.collect.android.activities.FileManagerTabs.class);
        startActivity(i);
    }
    
    /*
	 */
	public void taskDownloadingComplete(HashMap<String, String> result) {
		
		Log.i(TAG, "Complete - Send intent");

        // Refresh task list
    	Intent intent = new Intent("refresh");
	    LocalBroadcastManager.getInstance(getApplication()).sendBroadcast(intent);
	    
		try {
            dismissDialog(PROGRESS_DIALOG);
            removeDialog(PROGRESS_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }
		try {
			dismissDialog(ALERT_DIALOG);
            removeDialog(ALERT_DIALOG);
        } catch (Exception e) {
            // tried to close a dialog not open. don't care.
        }

		if(result != null) {
	        StringBuilder message = new StringBuilder();
	        Set<String> keys = result.keySet();
	        Iterator<String> it = keys.iterator();
	
	        while (it.hasNext()) {
	            String key = it.next();
	            if(key.equals("err_not_enabled")) {
	            	message.append(this.getString(R.string.smap_tasks_not_enabled));
	            } else if(key.equals("err_no_tasks")) {
	            	// No tasks is fine, in fact its the most common state
	            	//message.append(this.getString(R.string.smap_no_tasks));
	            } else {	
	            	message.append(key + " - " + result.get(key) + "\n\n");
	            }
	        }
	
	        mAlertMsg = message.toString().trim();
	        if(mAlertMsg.length() > 0) {
	        	showDialog(ALERT_DIALOG);
	        } 
	        
		} 
	}
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        

    	if(resultCode == RESULT_OK) {
	        switch (requestCode) {
	            // returns with a form path, start entry
	            case 10:
	            	if (intent.hasExtra("status")) {
	            		String status = intent.getExtras().getString("status");
	            		if(status.equals("success")) {
	            			if (intent.hasExtra("instanceUri")) {
	    	            		String instanceUri = intent.getExtras().getString("instanceUri");
	    	                	Intent i = new Intent(this, org.odk.collect.android.activities.FormEntryActivity.class);
	    	                	Uri inst = Uri.parse(instanceUri); 
	    	                	i.setData(inst);
	    	                	startActivityForResult(i, 10);
	    	            	}
	            			
	            		} else {
	            			if (intent.hasExtra("message")) {
	    	            		String message = intent.getExtras().getString("message");
	    	            		Log.e("MainListActivity", message);
	            			}
	            			
	            		}
	            	}
	            	
	                break;
	            default:
	                break;
	        }
	        //super.onActivityResult(requestCode, resultCode, intent);
    	}
    	return;
    }
    
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case PROGRESS_DIALOG:
                mProgressDialog = new ProgressDialog(this);
                DialogInterface.OnClickListener loadingButtonListener =
                    new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            mDownloadTasks.setDownloaderListener(null, mContext);
                            mDownloadTasks.cancel(true);
                            // Refresh the task list
                            Intent intent = new Intent("refresh");
                	        LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);
                        }
                    };
                mProgressDialog.setTitle(getString(R.string.downloading_data));
                mProgressDialog.setMessage(mProgressMsg);
                mProgressDialog.setIcon(android.R.drawable.ic_dialog_info);
                mProgressDialog.setIndeterminate(true);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setButton(getString(R.string.cancel), loadingButtonListener);
                return mProgressDialog;
            case ALERT_DIALOG:
                mAlertDialog = new AlertDialog.Builder(this).create();
                mAlertDialog.setMessage(mAlertMsg);
                mAlertDialog.setTitle(getString(R.string.smap_get_tasks));
                DialogInterface.OnClickListener quitListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int i) {
                    	dialog.dismiss();
                    }
                };
                mAlertDialog.setCancelable(false);
                mAlertDialog.setButton(getString(R.string.ok), quitListener);
                mAlertDialog.setIcon(android.R.drawable.ic_dialog_info);
                return mAlertDialog;
    		case PASSWORD_DIALOG:

    			AlertDialog.Builder builder = new AlertDialog.Builder(this);
    			final AlertDialog passwordDialog = builder.create();

    			passwordDialog.setTitle(getString(R.string.enter_admin_password));
    			final EditText input = new EditText(this);
    			input.setInputType(InputType.TYPE_TEXT_VARIATION_PASSWORD);
    			input.setTransformationMethod(PasswordTransformationMethod
    					.getInstance());
    			passwordDialog.setView(input, 20, 10, 20, 10);

    			passwordDialog.setButton(AlertDialog.BUTTON_POSITIVE,
    					getString(R.string.ok),
    					new DialogInterface.OnClickListener() {
    						public void onClick(DialogInterface dialog,
    								int whichButton) {
    							String value = input.getText().toString();
    							String pw = mAdminPreferences.getString(
    									AdminPreferencesActivity.KEY_ADMIN_PW, "");
    							if (pw.compareTo(value) == 0) {
    								Intent i = new Intent(getApplicationContext(),
    										AdminPreferencesActivity.class);
    								startActivity(i);
    								input.setText("");
    								passwordDialog.dismiss();
    							} else {
    								Toast.makeText(
    										MainTabsActivity.this,
    										getString(R.string.admin_password_incorrect),
    										Toast.LENGTH_SHORT).show();
    								Collect.getInstance()
    										.getActivityLogger()
    										.logAction(this, "adminPasswordDialog",
    												"PASSWORD_INCORRECT");
    							}
    						}
    					});

    			passwordDialog.setButton(AlertDialog.BUTTON_NEGATIVE,
    					getString(R.string.cancel),
    					new DialogInterface.OnClickListener() {

    						public void onClick(DialogInterface dialog, int which) {
    							Collect.getInstance()
    									.getActivityLogger()
    									.logAction(this, "adminPasswordDialog",
    											"cancel");
    							input.setText("");
    							return;
    						}
    					});

    			passwordDialog.getWindow().setSoftInputMode(
    					WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
    			return passwordDialog;
        }
        return null;
    }

	@Override
	public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
		// TODO Auto-generated method stub
		// Ignore formsDownloading is called synchronously from taskDownloader
	}

	@Override
	public void progressUpdate(String currentFile, int progress, int total) {
		// TODO Auto-generated method stub
		mProgressMsg = getString(R.string.fetching_file, currentFile, progress, total);
		mProgressDialog.setMessage(mProgressMsg);
	}

	@Override
	public void uploadingComplete(HashMap<String, String> result) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void progressUpdate(int progress, int total) {
		 mAlertMsg = getString(R.string.sending_items, progress, total);
	        mProgressDialog.setMessage(mAlertMsg);
	}

	@Override
	public void authRequest(Uri url, HashMap<String, String> doneSoFar) {
		// TODO Auto-generated method stub
		
	}

    @Override
    protected void onStart() {
        super.onStart();

    }

    @Override
    protected void onStop() {
        super.onStop();

    }

	@Override
	protected void onResume() {

		super.onResume();

        if(mNfcAdapter != null && mNfcAdapter.isEnabled()) {
            setupNFCDispatch(this, mNfcAdapter);        // NFC
        }

        if (!listenerRegistered) {
            IntentFilter filter = new IntentFilter();
            filter.addAction("startTask");
            filter.addAction("startMapTask");
            registerReceiver(listener, filter);
            listenerRegistered = true;
        }
	}

	@Override
	protected void onPause() {

		super.onPause();

        if(mNfcAdapter != null) {
            stopNFCDispatch(this, mNfcAdapter);        // NFC
        }

        if (listenerRegistered) {
            unregisterReceiver(listener);
            listenerRegistered = false;
        }
	}

	/**
	 * @param activity The corresponding {@link Activity} requesting the foreground dispatch.
	 * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public void setupNFCDispatch(final Activity activity, NfcAdapter adapter) {

        if (settings == null) {
            settings = PreferenceManager.getDefaultSharedPreferences(activity);
        }

        if (settings.getBoolean(PreferencesActivity.KEY_STORE_LOCATION_TRIGGER, true)) {
            adapter.enableForegroundDispatch(activity, mNfcPendingIntent, mNfcFilters, null);
        }
	}

	/**
	 * @param activity The corresponding {@link Activity} requesting to stop the foreground dispatch.
	 * @param adapter The {@link NfcAdapter} used for the foreground dispatch.
	 */
	public static void stopNFCDispatch(final Activity activity, NfcAdapter adapter) {

        if (adapter != null) {
            adapter.disableForegroundDispatch(activity);
        }
	}

	@Override
	protected void onNewIntent(Intent intent) {
		handleNFCIntent(intent);
	}

	/*
	 * NFC detected
	 */
	private void handleNFCIntent(Intent intent) {

        if(nfcTriggersList != null && nfcTriggersList.size() > 0) {
            Log.i(TAG, "tag discovered");
            String action = intent.getAction();
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);

            mReadNFC = new NdefReaderTask();
            mReadNFC.setDownloaderListener(this);
            mReadNFC.execute(tag);
        } else {
            Toast.makeText(
                    MainTabsActivity.this,
                    R.string.smap_no_tasks_nfc,
                    Toast.LENGTH_SHORT).show();
        }

	}

	@Override
	public void readComplete(String result) {

        boolean foundTask = false;
        ArrayList<NfcTrigger> triggers = null;
        String tab = tabHost.getCurrentTabTag();

        boolean isMapTab  = tab.equals("taskMap");
        if(isMapTab) {
            triggers = nfcTriggersMap;
        } else {
            triggers = nfcTriggersList;
        }


        if(triggers != null) {
            for(NfcTrigger trigger : triggers) {
                if(trigger.uid.equals(result)) {
                    foundTask = true;

                    Intent i = new Intent();
                    if(isMapTab) {
                        i.setAction("startMapTask");
                    } else {
                        i.setAction("startTask");
                    }
                    i.putExtra("position", trigger.position);
                    sendBroadcast(i);

                    Toast.makeText(
                            MainTabsActivity.this,
                            getString(R.string.smap_starting_task_from_nfc, result),
                            Toast.LENGTH_SHORT).show();

                    break;
                }
            }
        }
        if(!foundTask) {
            Toast.makeText(
                    MainTabsActivity.this,
                    getString(R.string.smap_no_matching_tasks_nfc, result),
                    Toast.LENGTH_SHORT).show();
        }
	}

    /*
     * Get the tasks shown on the map
     * Disable map tab
    public List<TaskEntry> getMapTasks() {
        return mMapTasks;
    }
    */

    /*
     * Manage location triggers
     */
    public void setLocationTriggers(List<TaskEntry> data, boolean map) {

        // Need to maintain two lists of tasks as the position in the task list is different for maps than for the list view
        ArrayList<NfcTrigger> triggers = null;

        if(map) {
            // mMapTasks = data;    Disable map tab
            // nfcTriggersMap = new ArrayList<NfcTrigger> ();
            // triggers = nfcTriggersMap;
        } else {
            mTasks = data;
            nfcTriggersList = new ArrayList<NfcTrigger> ();
            triggers = nfcTriggersList;
        }
        /*
         * Set NFC triggers
         */

        int position = 0;
        for (TaskEntry t : data) {
            if(t.type.equals("task") && t.locationTrigger != null && t.locationTrigger.trim().length() > 0
                    && t.taskStatus.equals(Utilities.STATUS_T_ACCEPTED)) {
                triggers.add(new NfcTrigger(t.id, t.locationTrigger, position));
            }
            position++;
        }

        /*
         * TODO set geofence triggers
         */
    }

/*
 * The user has selected an option to edit / complete a task
 */
    public void completeTask(TaskEntry entry) {

        String surveyNotes = null;
        String formPath = Collect.FORMS_PATH + entry.taskForm;
        String instancePath = entry.instancePath;
        long taskId = entry.id;
        String status = entry.taskStatus;

        Log.i(TAG, "Complete task" + entry.id + " : " + entry.name + " : " + entry.taskStatus);

        if(entry.repeat) {
            entry.instancePath = duplicateInstance(formPath, entry.instancePath, entry);
        }

        // set the adhoc location
        boolean canUpdate = false;
        try {
            canUpdate = Utilities.canComplete(status);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Show a message if this task is read only
        if(!canUpdate) {
            Toast.makeText(
                    MainTabsActivity.this,
                    getString(R.string.read_only),
                    Toast.LENGTH_SHORT).show();
        }

        // Get the provider URI of the instance
        String where = InstanceProviderAPI.InstanceColumns.INSTANCE_FILE_PATH + "=?";
        String[] whereArgs = {
                instancePath
        };

        Cursor cInstanceProvider = Collect.getInstance().getContentResolver().query(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                null, where, whereArgs, null);

        if(cInstanceProvider.getCount() != 1) {
            Log.e("MainListActivity:completeTask", "Unique instance not found: count is:" +
                    cInstanceProvider.getCount());
        } else {
            cInstanceProvider.moveToFirst();
            Uri instanceUri = ContentUris.withAppendedId(InstanceProviderAPI.InstanceColumns.CONTENT_URI,
                    cInstanceProvider.getLong(
                            cInstanceProvider.getColumnIndex(InstanceProviderAPI.InstanceColumns._ID)));
            surveyNotes = cInstanceProvider.getString(
                    cInstanceProvider.getColumnIndex(InstanceProviderAPI.InstanceColumns.T_SURVEY_NOTES));
            // Start activity to complete form
            Intent i = new Intent(Intent.ACTION_EDIT, instanceUri);

            i.putExtra(FormEntryActivity.KEY_FORMPATH, formPath);	// TODO Don't think this is needed
            i.putExtra(FormEntryActivity.KEY_TASK, taskId);
            i.putExtra(FormEntryActivity.KEY_SURVEY_NOTES, surveyNotes);
            i.putExtra(FormEntryActivity.KEY_CAN_UPDATE, canUpdate);
            if(instancePath != null) {	// TODO Don't think this is needed
                i.putExtra(FormEntryActivity.KEY_INSTANCEPATH, instancePath);
            }
            startActivity(i);
        }
        cInstanceProvider.close();

    }


    /*
     * Duplicate the instance
     * Call this if the instance repeats
     */
    public String duplicateInstance(String formPath, String originalPath, TaskEntry entry) {
        String newPath = null;

        // 1. Get a new instance path
        ManageForm mf = new ManageForm();
        newPath = mf.getInstancePath(formPath, 0);

        // 2. Duplicate the instance entry and get the new path
        Utilities.duplicateTask(originalPath, newPath, entry);

        // 3. Copy the instance files
        Utilities.copyInstanceFiles(originalPath, newPath);
        return newPath;
    }

    protected class MainTabsListener extends BroadcastReceiver {

        private MainTabsActivity mActivity = null;

        public MainTabsListener(MainTabsActivity activity) {
            mActivity = activity;
        }
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.i(TAG, "Intent received: " + intent.getAction());

            if (intent.getAction().equals("startTask")) {

                int position =  intent.getIntExtra("position", -1);
                if(position >= 0) {
                    TaskEntry entry = (TaskEntry) mTasks.get(position);

                    mActivity.completeTask(entry);
                }
            } else if (intent.getAction().equals("startMapTask")) {

                // Disable map tab
                // int position =  intent.getIntExtra("position", -1);
                //if(position >= 0) {
                //    TaskEntry entry = (TaskEntry) mMapTasks.get(position);
                //    mActivity.completeTask(entry);
                //}
            }
        }
    }
}
