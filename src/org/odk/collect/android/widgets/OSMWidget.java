package org.odk.collect.android.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.core.model.osm.OSMTag;
import org.javarosa.core.model.osm.OSMTagItem;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.logic.FormController.InstanceMetadata;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.MotionEvent;
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
	
	private Button mLaunchOpenMapKitButton;
	private String mBinaryName;
	private String mInstanceDirectory;
	private TextView mErrorTextView;
	private TextView mTagsTextView;
	private TextView mOSMFileNameTextView;
	
	private List<OSMTag> mOsmRequiredTags;
	private String mInstanceId;
	private int mFormId;
	
	public OSMWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		
		FormController formController = Collect.getInstance().getFormController();
		mInstanceDirectory = formController.getInstancePath().getParent();
		mInstanceId = formController.getSubmissionMetadata().instanceId;
		mFormId = formController.getFormDef().getID();
		
		setOrientation(LinearLayout.VERTICAL);
		
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        
        mErrorTextView = new TextView(context);
        mErrorTextView.setId(QuestionWidget.newUniqueId());
        mErrorTextView.setText("Something went wrong. We did not get valid OSM data.");
        
        // Show user the tags required
        mOsmRequiredTags = prompt.getQuestion().getOsmTags();
        mTagsTextView = new TextView(context);
        mTagsTextView.setId(QuestionWidget.newUniqueId());
        String tagsStr = osmRequiredTagsAsString(mOsmRequiredTags);
        mTagsTextView.setText(tagsStr);
        
        // Setup Launch OpenMapKit Button
        mLaunchOpenMapKitButton = new Button(getContext());
        mLaunchOpenMapKitButton.setId(QuestionWidget.newUniqueId());
        
        // Button Styling
        mLaunchOpenMapKitButton.setBackgroundColor(Color.rgb(126,188,111)); // OSM Green
        mLaunchOpenMapKitButton.setTextColor(Color.WHITE); // White text
        
        mLaunchOpenMapKitButton.setText(getContext().getString(R.string.capture_osm));
        mLaunchOpenMapKitButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mLaunchOpenMapKitButton.setPadding(20, 20, 20, 20);
        mLaunchOpenMapKitButton.setEnabled(!prompt.isReadOnly());
        mLaunchOpenMapKitButton.setLayoutParams(params);
        
        // Launch OpenMapKit intent on click
        mLaunchOpenMapKitButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				mLaunchOpenMapKitButton.setBackgroundColor(Color.rgb(112,146,255)); // OSM Blue
				Collect.getInstance().getActivityLogger().logInstanceAction(this, "launchOpenMapKitButton", 
            			"click", mPrompt.getIndex());
				mErrorTextView.setVisibility(View.GONE);
				launchOpenMapKit();
			}
		});
        
        // text view showing the resulting OSM file name
        mOSMFileNameTextView = new TextView(context);
        mOSMFileNameTextView.setId(QuestionWidget.newUniqueId());
        
        String s = prompt.getAnswerText();
        if (s != null) {
        	mOSMFileNameTextView.setText(s);
        	mTagsTextView.setVisibility(View.GONE);
        }
        
        // finish complex layout
        addView(mLaunchOpenMapKitButton);
        addView(mErrorTextView);
        addView(mTagsTextView);
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
		// hide initial view showing required tags
		mTagsTextView.setVisibility(View.GONE);
		
		// show file name of saved osm data
		String osmFileName = (String) answer;
		String osmInfo = osmFileName;
		mOSMFileNameTextView.setText(osmInfo);
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

	private String osmRequiredTagsAsString(List<OSMTag> osmRequiredTags) {
		String str = "Required Tags:\n";
	    if (osmRequiredTags != null && osmRequiredTags.size() > 0) {
	    	for (OSMTag tag : osmRequiredTags) {
	        	str += tag.key;
	        	if (tag.label != null) {
	        		str += " (Label: " + tag.label + ")";
	        	}
	        	str += '\n';
	        	if (tag.items.size() > 0) {
	        		str += "\tTag Value Choices:\n";
	        		for (OSMTagItem item : tag.items) {
	        			str += "\t\t";
	        			str += item.value;
	        			if (item.label != null){
	        				str += " (Label: " + item.label + ")";
	        			}
	        			str += "\n";
	        		}
	        	}
	        }
	    }
		return str;
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
			for (OSMTagItem item : tag.items) {
				tagValues.add(item.value);
				if (item.label != null) {
					intent.putExtra("TAG_VALUE_LABEL." + tag.key + "." + item.value, item.label);
				}
			}
			intent.putStringArrayListExtra("TAG_VALUES." + tag.key, tagValues);
		}
		intent.putStringArrayListExtra("TAG_KEYS", tagKeys);
	}
}
