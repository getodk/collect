package org.odk.collect.android.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.model.osm.OSMTagItem;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

/**
 * Widget that allows the user to launch OpenMapKit to get an OSM Feature with a
 * predetermined set of tags that are edited in the application.
 * 
 * @author Nicholas Hallahan nhallahan@spatialdev.com
 *
 */
public class OSMWidget extends QuestionWidget implements IBinaryWidget {
	
	// button colors
	private static final int OSM_GREEN = Color.rgb(126,188,111);
	private static final int OSM_BLUE = Color.rgb(112,146,255);
			
	private Button mLaunchOpenMapKitButton;
	private String mBinaryName;
	private String mInstanceDirectory;
	private TextView mErrorTextView;
	private TextView mOSMFileNameHeaderTextView;
	private TextView mOSMFileNameTextView;
	
	private List<OSMTag> mOsmRequiredTags;
	private String mInstanceId;
	private int mFormId;
	private String mFormFileName;
	private String mOSMFileName;
	
	public OSMWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		
		FormController formController = Collect.getInstance().getFormController();
		
		/**
		 * NH: I'm trying to find the form xml file name, but this is neither
		 * in the formController nor the formDef. In fact, it doesn't seem to
		 * be saved into any object in JavaRosa. However, the mediaFolder
		 * has the substring of the file name in it, so I extract the file name
		 * from here. Awkward...
		 */
		mFormFileName = formController.getMediaFolder().getName().split("-media")[0];
		
		mInstanceDirectory = formController.getInstancePath().getParent();
		mInstanceId = formController.getSubmissionMetadata().instanceId;
		mFormId = formController.getFormDef().getID();
		
		setOrientation(LinearLayout.VERTICAL);
        
        mErrorTextView = new TextView(context);
        mErrorTextView.setId(QuestionWidget.newUniqueId());
        mErrorTextView.setText("Something went wrong. We did not get valid OSM data.");
        
        // Determine the tags required
        QuestionDef question = prompt.getQuestion();
        mOsmRequiredTags = prompt.getQuestion().getOsmTags();
        
        // If an OSM File has already been saved, get the name.
        mOSMFileName = prompt.getAnswerText();
        
        // Setup Launch OpenMapKit Button
        mLaunchOpenMapKitButton = new Button(getContext());
        mLaunchOpenMapKitButton.setId(QuestionWidget.newUniqueId());
        
        // Button Styling
        if (mOSMFileName != null) {
        	mLaunchOpenMapKitButton.setBackgroundColor(OSM_BLUE);
        } else {
        	mLaunchOpenMapKitButton.setBackgroundColor(OSM_GREEN);
        }
        mLaunchOpenMapKitButton.setTextColor(Color.WHITE); // White text
        if (mOSMFileName != null) {
        	mLaunchOpenMapKitButton.setText(getContext().getString(R.string.recapture_osm));
        } else {
        	mLaunchOpenMapKitButton.setText(getContext().getString(R.string.capture_osm));
        }
        mLaunchOpenMapKitButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mLaunchOpenMapKitButton.setPadding(20, 20, 20, 20);
        mLaunchOpenMapKitButton.setEnabled(!prompt.isReadOnly());
        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(35, 30, 30, 35);
        mLaunchOpenMapKitButton.setLayoutParams(params);
        
        // Launch OpenMapKit intent on click
        mLaunchOpenMapKitButton.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				mLaunchOpenMapKitButton.setBackgroundColor(OSM_BLUE);
				Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchOpenMapKitButton", 
            			"click", mPrompt.getIndex());
				mErrorTextView.setVisibility(View.GONE);
				launchOpenMapKit();
			}
		});
        
        mOSMFileNameHeaderTextView = new TextView(context);
        mOSMFileNameHeaderTextView.setId(QuestionWidget.newUniqueId());
        mOSMFileNameHeaderTextView.setTextSize(20);
        mOSMFileNameHeaderTextView.setTypeface(null, Typeface.BOLD);
        mOSMFileNameHeaderTextView.setPadding(10, 0, 0, 10);
        mOSMFileNameHeaderTextView.setText("Edited OSM XML File:");
        
        // text view showing the resulting OSM file name
        mOSMFileNameTextView = new TextView(context);
        mOSMFileNameTextView.setId(QuestionWidget.newUniqueId());
        mOSMFileNameTextView.setTextSize(18);
        mOSMFileNameTextView.setTypeface(null, Typeface.ITALIC);
        if (mOSMFileName != null) {
        	mOSMFileNameTextView.setText(mOSMFileName);
        } else {
        	mOSMFileNameHeaderTextView.setVisibility(View.GONE);
        }
        mOSMFileNameTextView.setLayoutParams(params);

        
        // finish complex layout
        addView(mLaunchOpenMapKitButton);
        addView(mErrorTextView);
        addView(mOSMFileNameHeaderTextView);
        addView(mOSMFileNameTextView);
        
        // Hide Launch button if read-only
        if ( prompt.isReadOnly() ) {
        	mLaunchOpenMapKitButton.setVisibility(View.GONE);
        }
        mErrorTextView.setVisibility(View.GONE);
	}

	private void launchOpenMapKit() {
		try {
			//launch with intent that sends plain text
            Intent launchIntent = new Intent(Intent.ACTION_SEND);
            launchIntent.setType(HTTP.PLAIN_TEXT_TYPE);

            //send form id
            launchIntent.putExtra("FORM_ID", String.valueOf(mFormId));

            //send instance id
            launchIntent.putExtra("INSTANCE_ID", mInstanceId);
            
            //send instance directory
            launchIntent.putExtra("INSTANCE_DIR", mInstanceDirectory);
            
            //send form file name
            launchIntent.putExtra("FORM_FILE_NAME", mFormFileName);
            
            //send OSM file name if there was a previous edit
            if (mOSMFileName != null) {
            	launchIntent.putExtra("OSM_EDIT_FILE_NAME", mOSMFileName);
            }

            //send encode tag data structure to intent
            writeOsmRequiredTagsToExtras(launchIntent);
            
            //verify the package resolves before starting activity
            Context ctx = getContext();
            PackageManager packageManager = ctx.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(launchIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            //launch activity if it is safe
            if (isIntentSafe) {
            	// notify that the form is waiting for data
            	Collect.getInstance().getFormController().setIndexWaitingForData(mPrompt.getIndex());
            	// launch
                ((Activity)ctx).startActivityForResult(launchIntent, FormEntryActivity.OSM_CAPTURE);
            } else {
            	mErrorTextView.setVisibility(View.VISIBLE);
            }
		} catch(Exception ex) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Alert");
            builder.setMessage("Please install OpenMapKit!");
            DialogInterface.OnClickListener okClickListener = new DialogInterface.OnClickListener() {
            	public void onClick(DialogInterface dialog, int id) {
                    //TODO: launch to app store?
                }
            	
            };
            builder.setPositiveButton("Ok", okClickListener);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
	}
	
	@Override
	public void setBinaryData(Object answer) {	
		// show file name of saved osm data
		mOSMFileName = (String) answer;
		mOSMFileNameTextView.setText(mOSMFileName);
		mOSMFileNameHeaderTextView.setVisibility(View.VISIBLE);
		mOSMFileNameTextView.setVisibility(View.VISIBLE);
		
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public void cancelWaitingForBinaryData() {
		Collect.getInstance().getFormController().setIndexWaitingForData(null);
	}

	@Override
	public boolean isWaitingForBinaryData() {
		return mPrompt.getIndex().equals(
				Collect.getInstance().getFormController()
						.getIndexWaitingForData());
	}

	@Override
	public IAnswerData getAnswer() {
		String s = mOSMFileNameTextView.getText().toString();
		if (s == null || s.equals("")) {
			return null;
		} else {
			return new StringData(s);
		}
	}

	@Override
	public void clearAnswer() {
		mOSMFileNameTextView.setText(null);
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
		mOSMFileNameTextView.setOnLongClickListener(l);
		mLaunchOpenMapKitButton.setOnLongClickListener(l);
	}
	
	/**
	 * See: https://github.com/AmericanRedCross/openmapkit/wiki/ODK-Collect-Tag-Intent-Extras
	 */
	private void writeOsmRequiredTagsToExtras(Intent intent) {
		ArrayList<String> tagKeys = new ArrayList<String>();
		for (OSMTag tag: mOsmRequiredTags) {
			tagKeys.add(tag.key);
			if (tag.label != null) {
				intent.putExtra("TAG_LABEL." + tag.key, tag.label);
			}
			ArrayList<String> tagValues = new ArrayList<String>();
			if (tag.items != null) {
				for (OSMTagItem item : tag.items) {
					tagValues.add(item.value);
					if (item.label != null) {
						intent.putExtra("TAG_VALUE_LABEL." + tag.key + "." + item.value, item.label);
					}
				}
			}		
			intent.putStringArrayListExtra("TAG_VALUES." + tag.key, tagValues);
		}
		intent.putStringArrayListExtra("TAG_KEYS", tagKeys);
	}
}
