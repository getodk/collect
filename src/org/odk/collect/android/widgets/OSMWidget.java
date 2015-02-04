package org.odk.collect.android.widgets;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import android.content.Context;
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
	private String mInstanceFolder;
	private TextView mTagsTextView;
	private TextView mErrorTextView;
	
	public OSMWidget(Context context, FormEntryPrompt prompt) {
		super(context, prompt);
		
		mInstanceFolder =
                Collect.getInstance().getFormController().getInstancePath().getParent();
		
		setOrientation(LinearLayout.VERTICAL);
		
		TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        
        mErrorTextView = new TextView(context);
        mErrorTextView.setId(QuestionWidget.newUniqueId());
        mErrorTextView.setText("Something went wrong. We did not get valid OSM data.");
        
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
			}
		});
        
        // finish complex layout
        addView(mLaunchOpenMapKitButton);
        addView(mErrorTextView);
        
        // Hide Launch button if read-only
        if ( prompt.isReadOnly() ) {
        	mLaunchOpenMapKitButton.setVisibility(View.GONE);
        }
        mErrorTextView.setVisibility(View.GONE);
        
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
