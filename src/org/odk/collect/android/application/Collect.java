package org.odk.collect.android.application;

import org.apache.http.client.CookieStore;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.SyncBasicHttpContext;
import org.javarosa.core.reference.ReferenceManager;
import org.javarosa.core.reference.RootTranslator;
import org.javarosa.form.api.FormEntryController;
import org.odk.collect.android.R;
import org.odk.collect.android.database.StorageDatabase;
import org.odk.collect.android.logic.FileReferenceFactory;
import org.odk.collect.android.provider.FormsStorage;
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.utilities.AgingCredentialsProvider;

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

	public enum StorageType { FORMS, SUBMISSIONS };
	
	private static Collect singleton = null;
	
	public static Collect getInstance() {
		return singleton;
	}
	
	private StorageDatabase formsDatabase = null;
	private StorageDatabase submissionsDatabase = null;

	private HttpContext localContext = null;
	
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

	public synchronized HttpContext getHttpContext() {
		if ( localContext == null ) {
            // set up one context for all HTTP requests so that authentication
			// and cookies can be retained.
			localContext = new SyncBasicHttpContext(new BasicHttpContext());
            
            // establish a local cookie store for this attempt at downloading...
            CookieStore cookieStore = new BasicCookieStore();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            
            // and establish a credentials provider...
            CredentialsProvider credsProvider = new AgingCredentialsProvider(7*60*1000);
            localContext.setAttribute(ClientContext.CREDS_PROVIDER, credsProvider);
		}
		return localContext;
	}
	
	public synchronized StorageDatabase getStorageDb( StorageType type ) {
		if ( StorageType.FORMS == type ) {
			if ( formsDatabase == null ) {
				formsDatabase = new StorageDatabase(FormsStorage.getOpenHelper("forms"));
			}
			return formsDatabase;
		}
		if ( StorageType.SUBMISSIONS == type ) {
			if ( submissionsDatabase == null ) {
				submissionsDatabase = new StorageDatabase(SubmissionsStorage.getOpenHelper("submissions"));
			}
			return submissionsDatabase;
		}
		throw new IllegalArgumentException("Unexpected storage type: " + type.toString());
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
