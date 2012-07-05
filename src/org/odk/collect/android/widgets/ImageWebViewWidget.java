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

package org.odk.collect.android.widgets;

import java.io.File;
import java.util.Date;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Widget that allows user to take pictures, sounds or video and add them to the form.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class ImageWebViewWidget extends QuestionWidget implements IBinaryWidget {
    private final static String t = "MediaWidget";

    private Button mCaptureButton;
    private Button mChooseButton;
    private WebView mImageDisplay;

    private String mBinaryName;

    private String mInstanceFolder;
    private boolean mWaitingForData;

    private TextView mErrorTextView;

    private String constructImageElement() {
        File f = new File(mInstanceFolder + File.separator + mBinaryName);

        Display display =
                ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE))
                        .getDefaultDisplay();
        int screenWidth = display.getWidth();
        // int screenHeight = display.getHeight();

    	String imgElement = f.exists() ?  
    		("<img align=\"middle\" src=\"file:///" +
				f.getAbsolutePath() +
				// Appending the time stamp to the filename is a hack to prevent caching.
				"?" + new Date().getTime() + 
				"\" width=\"" + Integer.toString(screenWidth-10) + "\" >") : "";

    	return imgElement;
    }

    public boolean suppressFlingGesture(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
    	if ( mImageDisplay == null || mImageDisplay.getVisibility() != View.VISIBLE ) {
    		return false;
    	}

    	Rect rect = new Rect(); 
    	mImageDisplay.getHitRect(rect);
    	
    	// Log.i(t, "hitRect: " + rect.left + "," + rect.top + " : " + rect.right + "," + rect.bottom );
    	// Log.i(t, "e1 Raw, Clean: " + e1.getRawX() + "," + e1.getRawY() + " : " + e1.getX() + "," + e1.getY());
    	// Log.i(t, "e2 Raw, Clean: " + e2.getRawX() + "," + e2.getRawY() + " : " + e2.getX() + "," + e2.getY());

    	// starts in WebView
    	if ( rect.contains((int) e1.getRawX(), (int) e1.getRawY()) ) {
    		return true;
    	}
    	
    	// ends in WebView
    	if ( rect.contains((int) e2.getRawX(), (int) e2.getRawY()) ) {
    		return true;
    	}
    	
    	// transits WebView
    	if ( rect.contains( (int) ((e1.getRawX() + e2.getRawX())/2.0), 
    						(int) ((e1.getRawY() + e2.getRawY())/2.0) ) ) {
    		return true;
    	}
    	// Log.i(t, "NOT SUPPRESSED");
    	return false;
    }
    
    public ImageWebViewWidget(Context context, FormEntryPrompt prompt) {
        super(context, prompt);

        mWaitingForData = false;
        mInstanceFolder =
            FormEntryActivity.mInstancePath.substring(0,
                FormEntryActivity.mInstancePath.lastIndexOf(File.separator) + 1);

        setOrientation(LinearLayout.VERTICAL);

        TableLayout.LayoutParams params = new TableLayout.LayoutParams();
        params.setMargins(7, 5, 7, 5);
        
        mErrorTextView = new TextView(context);
        mErrorTextView.setText("Selected file is not a valid image");

        // setup capture button
        mCaptureButton = new Button(getContext());
        mCaptureButton.setText(getContext().getString(R.string.capture_image));
        mCaptureButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mCaptureButton.setPadding(20, 20, 20, 20);
        mCaptureButton.setEnabled(!prompt.isReadOnly());
        mCaptureButton.setLayoutParams(params);

        // launch capture intent on click
        mCaptureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorTextView.setVisibility(View.GONE);
                Intent i = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                // We give the camera an absolute filename/path where to put the
                // picture because of bug:
                // http://code.google.com/p/android/issues/detail?id=1480
                // The bug appears to be fixed in Android 2.0+, but as of feb 2,
                // 2010, G1 phones only run 1.6. Without specifying the path the
                // images returned by the camera in 1.6 (and earlier) are ~1/4
                // the size. boo.

                // if this gets modified, the onActivityResult in
                // FormEntyActivity will also need to be updated.
                i.putExtra(android.provider.MediaStore.EXTRA_OUTPUT,
                    Uri.fromFile(new File(Collect.TMPFILE_PATH)));
                try {
                    mWaitingForData = true;
                    ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.IMAGE_CAPTURE);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),
                        getContext().getString(R.string.activity_not_found, "image capture"),
                        Toast.LENGTH_SHORT).show();
                    mWaitingForData = false;
                }

            }
        });

        // setup chooser button
        mChooseButton = new Button(getContext());
        mChooseButton.setText(getContext().getString(R.string.choose_image));
        mChooseButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, mAnswerFontsize);
        mChooseButton.setPadding(20, 20, 20, 20);
        mChooseButton.setEnabled(!prompt.isReadOnly());
        mChooseButton.setLayoutParams(params);

        // launch capture intent on click
        mChooseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mErrorTextView.setVisibility(View.GONE);
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.setType("image/*");

                try {
                    mWaitingForData = true;
                    ((Activity) getContext()).startActivityForResult(i,
                        FormEntryActivity.IMAGE_CHOOSER);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(),
                        getContext().getString(R.string.activity_not_found, "choose image"),
                        Toast.LENGTH_SHORT).show();
                    mWaitingForData = false;
                }

            }
        });

        // finish complex layout
        addView(mCaptureButton);
        addView(mChooseButton);
        addView(mErrorTextView);
     
        // and hide the capture and choose button if read-only
        if ( prompt.isReadOnly() ) {
        	mCaptureButton.setVisibility(View.GONE);
        	mChooseButton.setVisibility(View.GONE);
        }
        mErrorTextView.setVisibility(View.GONE);

        // retrieve answer from data model and update ui
        mBinaryName = prompt.getAnswerText();

        // Only add the imageView if the user has taken a picture
        if (mBinaryName != null) {
        	mImageDisplay = new WebView(getContext());
        	mImageDisplay.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        	mImageDisplay.getSettings().setBuiltInZoomControls(true);
        	mImageDisplay.getSettings().setDefaultZoom(WebSettings.ZoomDensity.FAR);
        	mImageDisplay.setVisibility(View.VISIBLE);
        	mImageDisplay.setLayoutParams(params);
        	
    		// HTML is used to display the image.
    		String html =   "<body>" +  constructImageElement() + "</body>";
    		       
    		mImageDisplay.loadDataWithBaseURL("file:///" + mInstanceFolder + File.separator,
    								 html, "text/html", "utf-8", "");
    		addView(mImageDisplay);
        }
    }


    private void deleteMedia() {
        // get the file path and delete the file

        // There's only 1 in this case, but android 1.6 doesn't implement delete on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI only on
        // android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI + a #
        String[] projection = {
            Images.ImageColumns._ID
        };
        int del = 0;
        Cursor c = null;
        try {
        	c = getContext().getContentResolver().query(
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection,
                "_data='" + mInstanceFolder + mBinaryName + "'", null, null);
	        if (c.getCount() > 0) {
	            c.moveToFirst();
	            String id = c.getString(c.getColumnIndex(Images.ImageColumns._ID));
	
	            Log.i(
	                t,
	                "attempting to delete: "
	                        + Uri.withAppendedPath(
	                            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id));
	            del =
	                getContext().getContentResolver().delete(
	                    Uri.withAppendedPath(
	                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id), null,
	                    null);
	        }
        } finally {
        	if ( c != null ) {
                c.close();
        	}
        }

        // clean up variables
        mBinaryName = null;
        Log.i(t, "Deleted " + del + " rows from media content provider");
    }


    @Override
    public void clearAnswer() {
        // remove the file
        deleteMedia();
        
        if ( mImageDisplay != null ) {
	 		// update HTML to not hold image file reference.
	 		String html =   "<body></body>";
	 		mImageDisplay.loadDataWithBaseURL("file:///" + mInstanceFolder + File.separator,
	 								 html, "text/html", "utf-8", "");
	
	        mImageDisplay.setVisibility(View.INVISIBLE);
        }
        
        mErrorTextView.setVisibility(View.GONE);

        // reset buttons
        mCaptureButton.setText(getContext().getString(R.string.capture_image));
    }


    @Override
    public IAnswerData getAnswer() {
        if (mBinaryName != null) {
            return new StringData(mBinaryName.toString());
        } else {
            return null;
        }
    }


    private String getPathFromUri(Uri uri) {
        if (uri.toString().startsWith("file")) {
            return uri.toString().substring(6);
        } else {
            // find entry in content provider
        	String colString = null;
            Cursor c = null;
            try {
            	c = getContext().getContentResolver().query(uri, null, null, null, null);
            	c.moveToFirst();

	            // get data path
	            colString = c.getString(c.getColumnIndex("_data"));
            } finally {
            	if ( c != null ) {
            		c.close();
            	}
            }
            return colString;
        }
    }


    @Override
    public void setBinaryData(Object binaryuri) {
        // you are replacing an answer. delete the previous image using the
        // content provider.
        if (mBinaryName != null) {
            deleteMedia();
        }
        String binarypath = getPathFromUri((Uri) binaryuri);
        File f = new File(binarypath);
        mBinaryName = f.getName();
        Log.i(t, "Setting current answer to " + f.getName());

        mWaitingForData = false;
    }


    @Override
    public void setFocus(Context context) {
        // Hide the soft keyboard if it's showing.
        InputMethodManager inputManager =
            (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(this.getWindowToken(), 0);
    }


    @Override
    public boolean isWaitingForBinaryData() {
        return mWaitingForData;
    }


    @Override
	public void cancelWaitingForBinaryData() {
    	mWaitingForData = false;
	}


    @Override
    public void setOnLongClickListener(OnLongClickListener l) {
        mCaptureButton.setOnLongClickListener(l);
        mChooseButton.setOnLongClickListener(l);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mCaptureButton.cancelLongPress();
        mChooseButton.cancelLongPress();
    }

}
