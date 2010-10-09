package org.odk.collect.android.application;

import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.R;
import org.odk.collect.android.logic.FileReferenceFactory;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.os.IBinder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

public class Collect extends Application {

	private static Collect singleton = null;
	
	public static Collect getInstance() {
		return singleton;
	}
	
	private FormEntryController formEntryController = null;

    private FileReferenceFactory factory = null;
    private boolean firstReferenceInitialization = true;

    private IBinder viewToken = null;
    
	/* (non-Javadoc)
	 * @see android.app.Application#onConfigurationChanged(android.content.res.Configuration)
	 */
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}

	/* (non-Javadoc)
	 * @see android.app.Application#onCreate()
	 */
	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		singleton = this;
	}

	public void setFormEntryController( FormEntryController formEntryController) {
		this.formEntryController = formEntryController;
	}
	
	/**
	 * Returns the form entry controller, if there is one.
	 * 
	 * @return the fec or null
	 */
	public FormEntryController getFormEntryController() {
		return formEntryController;
	}


	public void registerMediaPath(String mediaPath) {
        if ( factory != null ) {
    		ReferenceManager._().removeReferenceFactory(factory);
        }
    	factory = new FileReferenceFactory(mediaPath);
        ReferenceManager._().addReferenceFactory(factory);
        
    	if (firstReferenceInitialization) {
    		firstReferenceInitialization = false;
            ReferenceManager._()
                    .addRootTranslator(new RootTranslator("jr://images/", "jr://file/"));
            ReferenceManager._().addRootTranslator(new RootTranslator("jr://audio/", "jr://file/"));
            ReferenceManager._().addRootTranslator(new RootTranslator("jr://video/", "jr://file/"));
        }
	}

	public void showSoftKeyboard( View v ) {
        InputMethodManager inputManager =
            (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        IBinder b = v.getWindowToken();
        if ( viewToken != null && !viewToken.equals(b) ) {
        	inputManager.hideSoftInputFromInputMethod(viewToken, 0);
        }
        
        if ( inputManager.isActive(v) ) return;
        inputManager.showSoftInput(v, 0);
        viewToken = b;
	}
	
	public void hideSoftKeyboard( View c ) {
        InputMethodManager inputManager =
            (InputMethodManager) getBaseContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        if ( viewToken != null ) {
        	inputManager.hideSoftInputFromWindow(viewToken, 0);
        }
        viewToken = null;
        
        if ( c != null ) {
        	if ( inputManager.isActive()) {
        		inputManager.hideSoftInputFromWindow(c.getApplicationWindowToken(), 0);
        	}
        }
	}
	
	/**
	 * Creates and displays a dialog displaying the violated constraint.
	 */
	public void createConstraintToast(String constraintText, int saveStatus) {
		switch (saveStatus) {
		case FormEntryController.ANSWER_CONSTRAINT_VIOLATED:
			if (constraintText == null) {
				constraintText = getString(R.string.invalid_answer_error);
			}
			break;
		case FormEntryController.ANSWER_REQUIRED_BUT_EMPTY:
			constraintText = getString(R.string.required_answer_error);
			break;
		}

		showCustomToast(constraintText);
	}

	private void showCustomToast(String message) {
		LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View view = inflater.inflate(R.layout.toast_view, null);

		// set the text in the view
		TextView tv = (TextView) view.findViewById(R.id.message);
		tv.setText(message);

		Toast t = new Toast(this);
		t.setView(view);
		t.setDuration(Toast.LENGTH_SHORT);
		t.setGravity(Gravity.CENTER, 0, 0);
		t.show();
	}
}
