package org.odk.collect.android.widgets;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.protocol.HTTP;
import org.javarosa.core.model.data.IAnswerData;
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
	private TextView mTagsTextView;
	private TextView mErrorTextView;
	
	private List<String> mOsmRequiredTags;
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
        String tagsStr = "Required Tags:\n";
        if (mOsmRequiredTags != null) {
        	for (String tag : mOsmRequiredTags) {
            	tagsStr += tag + '\n';
            }
        }
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
        
        // finish complex layout
        addView(mLaunchOpenMapKitButton);
        addView(mErrorTextView);
        addView(mTagsTextView);
        
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

            //send list of required tags
            launchIntent.putStringArrayListExtra("REQUIRED_TAGS", (ArrayList<String>) mOsmRequiredTags);
            
            //verify the package resolves before starting activity
            Context ctx = getContext();
            PackageManager packageManager = ctx.getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities(launchIntent, 0);
            boolean isIntentSafe = activities.size() > 0;

            //launch activity if it is safe
            if (isIntentSafe) {
                ((Activity)ctx).startActivityForResult(launchIntent, FormEntryActivity.OSM_CAPTURE);
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
		// TODO Auto-generated method stub

	}

	@Override
	public void cancelWaitingForBinaryData() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isWaitingForBinaryData() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public IAnswerData getAnswer() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAnswer() {
		// TODO Auto-generated method stub

	}

	@Override
	public void setFocus(Context context) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setOnLongClickListener(OnLongClickListener l) {
		// TODO Auto-generated method stub

	}

}
