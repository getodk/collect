package org.odk.collect.android.database;

import java.io.File;
import java.util.Calendar;
import java.util.LinkedList;

import org.javarosa.core.model.FormIndex;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.application.Collect;

import android.app.Activity;
import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public final class ActivityLogger {
	
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
		
		DatabaseHelper() {
			super(Collect.LOG_PATH, DATABASE_NAME, null, DATABASE_VERSION);
			new File(Collect.LOG_PATH).mkdirs();
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.execSQL("DROP TABLE IF EXISTS " + DATABASE_TABLE);
			onCreate(db);
		}
	}
	
	/**
	 * The minimum delay, in milliseconds, for a scroll action to be considered new. 
	 */
	private static final long MIN_SCROLL_DELAY = 400L;
	/**
	 * The maximum size of the scroll action buffer.  After it reaches this size,
	 * it will be flushed.
	 */
	private static final int MAX_SCROLL_ACTION_BUFFER_SIZE = 8;
	
	private static final String DATABASE_TABLE = "log";
	private static final String ENABLE_LOGGING = "enabled";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_NAME = "activityLog.db";
	// Database columns
	private static final String ID = "_id";
	private static final String TIMESTAMP = "timestamp";
	private static final String DEVICEID = "device_id";
	private static final String CLASS = "class";
	private static final String CONTEXT = "context";
	private static final String ACTION = "action";
	private static final String INSTANCE_PATH = "instance_path";
	private static final String QUESTION = "question";
	private static final String PARAM1 = "param1";
	private static final String PARAM2 = "param2";
	
	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE + " (" +
			ID + " integer primary key autoincrement, " +
			TIMESTAMP + " integer not null, " +
			DEVICEID + " text not null, " +
			CLASS + " text not null, " +
			CONTEXT + " text not null, " +
			ACTION + " text, " +
			INSTANCE_PATH + " text, " +
			QUESTION + " text, " +
			PARAM1 + " text, " +
			PARAM2 + " text);";

	private final boolean mLoggingEnabled;
	private final String mDeviceId;
	private DatabaseHelper mDbHelper = null;
	private SQLiteDatabase mDb = null;
	private boolean mIsOpen = false;
	// We buffer scroll actions to make sure there aren't too many pauses
	// during scrolling.  This list is flushed every time any other type of
	// action is logged.
	private LinkedList<ContentValues> mScrollActions = new LinkedList<ContentValues>();
    
	public ActivityLogger(String deviceId) {
		this.mDeviceId = deviceId;
		mLoggingEnabled = new File(Collect.LOG_PATH, ENABLE_LOGGING).exists();
        open();
	}
	
	public boolean isOpen() {
		return mLoggingEnabled && mIsOpen;
	}
	
    public void open() throws SQLException {
    	if (!mLoggingEnabled || mIsOpen) return;
        try {
            mDbHelper = new DatabaseHelper();
            mDb = mDbHelper.getWritableDatabase();
            mIsOpen = true;
        } catch (SQLiteException e) {
        	System.err.println("Error: " + e.getMessage());
        	mIsOpen = false;
        }
    }
    
    // cached to improve logging performance...
    // only access these through getXPath(FormIndex index);
    private FormIndex cachedXPathIndex = null;
    private String cachedXPathValue = null;

    // DO NOT CALL THIS OUTSIDE OF synchronized(mScrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(mScrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(mScrollActions) !!!!
    // DO NOT CALL THIS OUTSIDE OF synchronized(mScrollActions) !!!!
    private String getXPath(FormIndex index) {
    	if ( index == cachedXPathIndex ) return cachedXPathValue;
    	
    	cachedXPathIndex = index;
    	cachedXPathValue = FormEntryActivity.mFormController.getXPath(index);
    	return cachedXPathValue;
    }


    private void log(String object, String context, String action, String instancePath, FormIndex index, String param1, String param2) {
		if (!isOpen()) return;

        ContentValues cv = new ContentValues();
        cv.put(DEVICEID, mDeviceId);
        cv.put(CLASS, object);
        cv.put(CONTEXT, context);
        cv.put(ACTION, action);
        cv.put(INSTANCE_PATH, instancePath);
        cv.put(PARAM1, param1);
        cv.put(PARAM2, param2);
        cv.put(TIMESTAMP, Calendar.getInstance().getTimeInMillis());

        insertContentValues(cv, index);
    }
    
    public void logScrollAction(Object t, int distance) {
    	if (!isOpen()) return;

    	synchronized(mScrollActions) {
	    	long timeStamp = Calendar.getInstance().getTimeInMillis();
	    	
	    	// Check to see if we can add this scroll action to the previous action.
	    	if (!mScrollActions.isEmpty()) {
	    		ContentValues lastCv = mScrollActions.get(mScrollActions.size() - 1);
		    	long oldTimeStamp = lastCv.getAsLong(TIMESTAMP);
		    	int oldDistance = Integer.parseInt(lastCv.getAsString(PARAM1));
		    	if (Integer.signum(distance) == Integer.signum(oldDistance) &&
		    			timeStamp - oldTimeStamp < MIN_SCROLL_DELAY) {
		    		lastCv.put(PARAM1, oldDistance + distance);
		    		lastCv.put(TIMESTAMP, timeStamp);
		    		return;
		    	}
	    	}
	    	
	    	if (mScrollActions.size() >= MAX_SCROLL_ACTION_BUFFER_SIZE) {
	    		insertContentValues(null, null); // flush scroll list...
	    	}
	
	    	String idx = "";
	    	if ( FormEntryActivity.mFormController != null ) {
	    		idx = getXPath(FormEntryActivity.mFormController.getFormIndex());
	    	}
	    	String instancePath = FormEntryActivity.mInstancePath;
	    	
	    	// Add a new scroll action to the buffer.
	    	ContentValues cv = new ContentValues();
	    	cv.put(DEVICEID, mDeviceId);
	    	cv.put(CLASS, t.getClass().getName());
	    	cv.put(CONTEXT, "scroll");
	    	cv.put(ACTION, "");
	    	cv.put(PARAM1, distance);
	    	cv.put(QUESTION, idx);
	    	cv.put(INSTANCE_PATH, instancePath);
	        cv.put(TIMESTAMP, timeStamp);
	    	cv.put(PARAM2, timeStamp);
	    	mScrollActions.add(cv);
    	}
    }
    
    private void insertContentValues(ContentValues cv, FormIndex index) {
    	synchronized(mScrollActions) {
	        try {
	        	while ( !mScrollActions.isEmpty() ) {
	        		ContentValues scv = mScrollActions.removeFirst();
	        		mDb.insert(DATABASE_TABLE, null, scv);
	        	}
	
	        	if ( cv != null ) {
	    	    	String idx = "";
	    	    	if ( index != null ) {
	    	    		idx = getXPath(index);
	    	    	}
	    	    	cv.put(QUESTION,idx);
	        		mDb.insert(DATABASE_TABLE, null, cv);
	        	}
	        } catch (SQLiteConstraintException e) {
	            System.err.println("Error: " + e.getMessage());
	        }
    	}
    }

    // Convenience methods
    
    public void logOnStart(Activity a) {
		log( a.getClass().getName(), "onStart", null, null, null, null, null);
    }

    public void logOnStop(Activity a) {
		log( a.getClass().getName(), "onStop", null, null, null, null, null);
    }
    
    public void logAction(Object t, String context, String action) {
		log( t.getClass().getName(), context, action, null, null, null, null);
    }
    
    public void logActionParam(Object t, String context, String action, String param1) {
		log( t.getClass().getName(), context, action, null, null, param1, null);
    }

    public void logInstanceAction(Object t, String context, String action) {
    	FormIndex index = null;
    	if ( FormEntryActivity.mFormController != null ) {
    		index = FormEntryActivity.mFormController.getFormIndex();
    	}
    	log( t.getClass().getName(), context, action, FormEntryActivity.mInstancePath, index, null, null);
    }

    public void logInstanceAction(Object t, String context, String action, FormIndex index) {
    	log( t.getClass().getName(), context, action, FormEntryActivity.mInstancePath, index, null, null);
    }
}
