package org.odk.collect.android.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;

import org.odk.collect.android.application.Collect;

import android.content.ContentValues;
import android.database.SQLException;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

public class ActivityLogger {
	public static boolean LOGGING_ENABLED = true;
	
    private static class DatabaseHelper extends ODKSQLiteOpenHelper {
		
		DatabaseHelper() {
			super(DATABASE_PATH, getDBName(), null, DATABASE_VERSION);
			new File(DATABASE_PATH).mkdirs();
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
	private static final int MIN_SCROLL_DELAY = 200;
	/**
	 * The maximum size of the scroll action buffer.  After it reaches this size,
	 * it will be flushed.
	 */
	private static final int MAX_SCROLL_ACTION_BUFFER_SIZE = 8;
	
	private static final String DATABASE_TABLE = "log";
	private static final int DATABASE_VERSION = 1;
	private static final String DATABASE_PATH = Collect.LOG_PATH;

	// Database columns
	private static final String ID = "_id";
	private static final String TIMESTAMP = "timestamp";
	private static final String ACTION = "action";
	private static final String INSTANCE_PATH = "instance_path";
	private static final String QUESTION = "question";
	private static final String PARAM1 = "param1";
	private static final String PARAM2 = "param2";
	
	private static final String DATABASE_CREATE =
			"create table " + DATABASE_TABLE + " (" +
			ID + " integer primary key autoincrement, " +
			TIMESTAMP + " text not null, " +
			ACTION + " text not null, " +
			INSTANCE_PATH + " text, " +
			QUESTION + " text, " +
			PARAM1 + " text, " +
			PARAM2 + " text);";

	private DatabaseHelper mDbHelper;
	private SQLiteDatabase mDb;
	private boolean mIsOpen;
	// We buffer scroll actions to make sure there aren't too many pauses
	// during scrolling.  This list is flushed every time any other type of
	// action is logged.
	private ArrayList<ContentValues> mScrollActions;

    private static String getDBName() {
    	return Collect.getInstance().getDeviceId() + "_log.sqlite";
    }
    
    private static String getDBPath() {
    	return DATABASE_PATH + "/" + getDBName();
    }
	
	public ActivityLogger() {
		mScrollActions = new ArrayList<ContentValues>();
	}
	
	public boolean isOpen() {
		return LOGGING_ENABLED && mIsOpen;
	}
	
    public synchronized void open() throws SQLException {
    	if (!LOGGING_ENABLED || mIsOpen) return;
        try {
            mDbHelper = new DatabaseHelper();
            mDb = mDbHelper.getWritableDatabase();
            mIsOpen = true;
        } catch (SQLiteException e) {
        	System.err.println("Error: " + e.getMessage());
        	mIsOpen = false;
        }
    }

    public synchronized void close() {
    	if (!LOGGING_ENABLED) return;
    	if (!mIsOpen) return;
        mDbHelper.close();
        mDb.close();
        mIsOpen = false;
    }

    public synchronized void log(String action, String instancePath, String question,
    		String param1, String param2) {
    	if (!LOGGING_ENABLED) return;
    	openAndLogErrorIfNeeded();
    	if (!mScrollActions.isEmpty()) {
    		flushScrollActions();
    	}
        ContentValues cv = new ContentValues();
        cv.put(ACTION, action);
        cv.put(QUESTION, question);
        cv.put(INSTANCE_PATH, instancePath);
        cv.put(PARAM1, param1);
        cv.put(PARAM2, param2);
        cv.put(TIMESTAMP, "" + Calendar.getInstance().getTimeInMillis());
        insertContentValues(cv);
    }
    
    public synchronized void logScrollAction(String instancePath, String question, int distance) {
    	if (!LOGGING_ENABLED) return;
    	openAndLogErrorIfNeeded();
    	// Check to see if we can add this scroll action to the previous action.
    	if (!mScrollActions.isEmpty()) {
    		ContentValues lastCv = mScrollActions.get(mScrollActions.size() - 1);
	    	long timeStamp = Calendar.getInstance().getTimeInMillis();
	    	long oldTimeStamp = Long.parseLong(lastCv.getAsString(TIMESTAMP));
	    	int oldDistance = Integer.parseInt(lastCv.getAsString(PARAM1));
	    	if (Integer.signum(distance) == Integer.signum(oldDistance) &&
	    			timeStamp - oldTimeStamp < MIN_SCROLL_DELAY) {
	    		lastCv.put(PARAM1, oldDistance + distance);
	    		lastCv.put(TIMESTAMP, "" + timeStamp);
	    		// PARAM2 will hold the time the scroll started.
	    		if (!lastCv.containsKey(PARAM2)) {
		    		lastCv.put(PARAM2, "" + oldTimeStamp);
	    		}
	    		return;
	    	}
    	}
    	
    	if (mScrollActions.size() >= MAX_SCROLL_ACTION_BUFFER_SIZE) {
    		flushScrollActions();
    	}
    	
    	// Add a new scroll action to the buffer.
    	ContentValues cv = new ContentValues();
    	cv.put(ACTION, "scroll");
    	cv.put(QUESTION, question);
    	cv.put(INSTANCE_PATH, instancePath);
    	cv.put(PARAM1, "" + distance);
        cv.put(TIMESTAMP, "" + Calendar.getInstance().getTimeInMillis());
    	mScrollActions.add(cv);
    }
    
    private synchronized void flushScrollActions() {
		for (ContentValues cv : mScrollActions) {
			insertContentValues(cv);
		}
		mScrollActions.clear();
    }
    
    private void openAndLogErrorIfNeeded() {
    	if (!mIsOpen) {
    		System.err.println("Logger.log() called without being opened.  Opening...");
    		open();
    	}
    }
    
    private void insertContentValues(ContentValues cv) {
        try {
            mDb.insert(DATABASE_TABLE, null, cv);
        } catch (SQLiteConstraintException e) {
            System.err.println("Error: " + e.getMessage());
        }    	
    }

    // Convenience methods
    public synchronized void log(String action, String instancePath, String question, String param1) {
        log(action, instancePath, question, param1, null);
    }

    public synchronized void log(String action, String instancePath, String question) {
        log(action, instancePath, question, null, null);
    }

    public synchronized void log(String action, String instancePath) {
        log(action, instancePath, null, null, null);
    }

    public synchronized void log(String action) {
        log(action, null, null, null, null);
    }
}
