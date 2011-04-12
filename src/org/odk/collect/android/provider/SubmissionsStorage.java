/*
 * Copyright (C) 2011 University of Washington
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

package org.odk.collect.android.provider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.ODKSQLiteOpenHelper;
import org.odk.collect.android.database.StorageDatabase;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.util.Log;

public class SubmissionsStorage extends ContentProvider {

	private static final String t = "SubmissionsStorage";

	/** URI of the Submissions content provider */
	public static final Uri CONTENT_URI;
	public static final Uri CONTENT_URI_INFO_DATASET;
	public static final Uri CONTENT_URI_INSTANCE_FILE_DATASET;
	public static final Uri CONTENT_URI_SUBMISSION_BLOB_FILE_DATASET;
	public static final Uri CONTENT_URI_FORMS_INFO_URI_DATASET;

	public static final String INFO_DATASET = "info";
	public static final String INSTANCE_FILE_DATASET = "instance";
	public static final String SUBMISSION_BLOB_FILE_DATASET = "submissionBlob";
	public static final String FORMS_INFO_URI_DATASET = "form"; // get uri to form info record

    // status for instances
    public static final String STATUS_INCOMPLETE = "incomplete";
    public static final String STATUS_COMPLETE = "complete";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_PARTIALLY_SUBMITTED = "partiallySubmitted";
    public static final String STATUS_SUBMISSION_FAILED = "submissionFailed";

	// these values are read-only through content provider...
	public static final String KEY_ID = "_id"; // required for Android
	public static final String KEY_DISPLAY_NAME = "displayName"; // (form name)
	public static final String KEY_DISPLAY_SUBTEXT = "displaySubtext";
	// KEY_DISPLAY_SUB_SUBTEXT is a text fragment that is appended to the synthesized 
	// KEY_DISPLAY_SUBTEXT.  It is not separately retained in the datastore.
	public static final String KEY_DISPLAY_SUB_SUBTEXT = "displaySubSubtext";

	public static final String KEY_STATUS = "status";
	public static final String KEY_LAST_STATUS_CHANGE_DATE = "date";

	public static final String KEY_CAN_EDIT_SUBMISSION = "canEditSubmission"; // boolean
	public static final String KEY_SUBMISSION_URI = "submissionUri";
	
	public static final String KEY_INSTANCE_DIRECTORY_PATH = "instanceDirectory";

	// this is a key into the synthesized CONTENT_URI_FORMS_INFO_URI_DATASET
	public static final String KEY_URI_FORMS_INFO = "uriFormsInfo";
	
	private static final int INFO_ALLROWS = 1;
	private static final int INFO_SINGLE_ROW = 2;
	private static final int INSTANCE_FILE_SINGLE_ROW = 3;
	private static final int SUBMISSION_BLOB_FILE_SINGLE_ROW = 4;
	private static final int FORMS_INFO_URI_ALLROWS= 5;
	private static final int FORMS_INFO_URI_SINGLE_ROW = 6;
	
	private static final UriMatcher uriMatcher;
	
	static {
		// Collect.getInstance() is null at this point!!!
		String submissionsAuthority = "org.opendatakit.storage.submissions";
		CONTENT_URI = Uri.parse("content://" + submissionsAuthority);
		CONTENT_URI_INFO_DATASET = Uri.withAppendedPath(CONTENT_URI, INFO_DATASET);
		CONTENT_URI_INSTANCE_FILE_DATASET = Uri.withAppendedPath(CONTENT_URI, INSTANCE_FILE_DATASET);
		CONTENT_URI_SUBMISSION_BLOB_FILE_DATASET = Uri.withAppendedPath(CONTENT_URI, SUBMISSION_BLOB_FILE_DATASET);
		CONTENT_URI_FORMS_INFO_URI_DATASET = Uri.withAppendedPath(CONTENT_URI, FORMS_INFO_URI_DATASET);
		
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(submissionsAuthority, INFO_DATASET, INFO_ALLROWS);
		uriMatcher.addURI(submissionsAuthority, INFO_DATASET + "/#", INFO_SINGLE_ROW);
		uriMatcher.addURI(submissionsAuthority, INSTANCE_FILE_DATASET + "/#", INSTANCE_FILE_SINGLE_ROW);
		uriMatcher.addURI(submissionsAuthority, SUBMISSION_BLOB_FILE_DATASET + "/#", SUBMISSION_BLOB_FILE_SINGLE_ROW);
		uriMatcher.addURI(submissionsAuthority, FORMS_INFO_URI_DATASET + "/#", FORMS_INFO_URI_SINGLE_ROW);
	}

	private static final int matchOnly(Uri uri, int ... values) {
		int value = uriMatcher.match(uri);
		for ( int i = 0 ; i < values.length ; ++i ) {
			if ( value == values[i] ) return value;
		}
		throw new IllegalArgumentException(
				"Invalid URI for this operation: " + uri.toString());
	}
	
	private static final void isExposableProjection(String[] projection) {
		for ( String s : projection ) {
			if ( KEY_INSTANCE_DIRECTORY_PATH.equalsIgnoreCase(s)) {
				Log.w(t, "Exposing KEY_INSTANCE_DIRECTORY_PATH -- consider restructuring to hide this!");
				// throw new IllegalArgumentException("Unrecognized element");
			}
		}
	}
	
	private static final class SelectionCriteria {
		public final String selection;
		public final String[] selectionArgs;
		
		SelectionCriteria(String selection, String[] selectionArgs) {
			this.selection = selection;
			this.selectionArgs = selectionArgs;
		}
		
		SelectionCriteria(String selection, String[] selectionArgs, Uri uri) {
			if ( uri.getPathSegments().size() < 2 ) {
				throw new IllegalArgumentException("Expected a rowId qualifier: " + uri.toString());
			}
			String key = uri.getPathSegments().get(1);
			String newSelection = null;
			String[] newArgs = null;
			if ( selectionArgs == null || selectionArgs.length == 0 ) {
				newArgs = new String[] { key };
				newSelection = ((selection == null || selection.length() == 0) ?
								"" : (selection + " AND ")) + KEY_ID + " = ?";
			} else {
				newArgs = new String[selectionArgs.length+1];
				int i = 0;
				for (; i < selectionArgs.length ; ++i ) {
					newArgs[i] = selectionArgs[i];
				}
				newArgs[i] = key;
				newSelection = ((selection == null || selection.length() == 0) ?
						"" : (selection + " AND ")) + KEY_ID + " = ?";
			}
			this.selection = newSelection;
			this.selectionArgs = newArgs;
		}
	}

	public static final String SUBMISSIONS_TABLE = "submissions";
    
	private static final String getDisplaySubtext(String state, Date date) {
		String ts =
            new SimpleDateFormat("EEE, MMM dd, yyyy 'at' HH:mm").format(date);
		if ( state == null ) {
			return "Added on " + ts;
		} else if ( STATUS_INCOMPLETE.equalsIgnoreCase(state) ) {
			return "Saved on " + ts;
		} else if ( STATUS_COMPLETE.equalsIgnoreCase(state) ) {
			return "Finished on " + ts;
		} else if ( STATUS_SUBMITTED.equalsIgnoreCase(state) ) {
			return "Submitted on " + ts;
		} else if ( STATUS_PARTIALLY_SUBMITTED.equalsIgnoreCase(state) ) {
			return "Partially submitted on " + ts;
		} else if ( STATUS_SUBMISSION_FAILED.equalsIgnoreCase(state) ) {
			return "Submission attempt failed on " + ts;
		} else {
			return "Added on " + ts;
		}
	}

	/**
	 * Database helper.  Adding or altering the database structure should 
	 * result in a new class than handles transforming from version n-1 to 
	 * version n.  When onUpgrade is called, it can then invoke the onUpgrade
	 * of the earlier versions until it has the version at n-1, then it does
	 * its own processing.
	 * 
	 * @author mitchellsundt@gmail.com
	 *
	 */
    private static class DatabaseHelper1 extends ODKSQLiteOpenHelper {
        public static final int DATABASE_VERSION = 1;
        
        private static final String SUBMISSIONS_TABLE_CREATE =
            "create table " + SubmissionsStorage.SUBMISSIONS_TABLE + " ("
            		+ KEY_ID + " integer primary key, " 
                    + KEY_DISPLAY_NAME + " text not null, " 
                    
                    + KEY_DISPLAY_SUBTEXT + " text not null, "
                    + KEY_STATUS + " text not null, "
                    + KEY_LAST_STATUS_CHANGE_DATE + " date not null, "

                    + KEY_CAN_EDIT_SUBMISSION + " integer not null, "
                    + KEY_SUBMISSION_URI + " text null, "
                	+ KEY_INSTANCE_DIRECTORY_PATH + " text not null );";

        DatabaseHelper1(String databaseName) {
            super(FileUtils.getDatabasePath(), databaseName, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(SUBMISSIONS_TABLE_CREATE);
        }


        @Override
        // upgrading will destroy all old data
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + SubmissionsStorage.SUBMISSIONS_TABLE);
            onCreate(db);
        }
    }

    public static final ODKSQLiteOpenHelper getOpenHelper(String databaseName) {
    	return new DatabaseHelper1(databaseName);
    }
    
	private StorageDatabase guardedStorageDb;
	
	private synchronized StorageDatabase getStorageDb() {
		if ( guardedStorageDb != null ) return guardedStorageDb;
		
		Collect app = Collect.getInstance();
		if ( app == null ) throw new IllegalStateException("Collect application not yet initialized");

		guardedStorageDb = app.getStorageDb(Collect.StorageType.SUBMISSIONS);
		sync();
		
		return guardedStorageDb;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#onCreate()
	 */
	@Override
	public boolean onCreate() {
		return true;
	}
	
	private static class InstanceDetails {
		String formId;
		Integer modelVersion;
		Integer uiVersion;
	}
	
	private InstanceDetails retrieveDetails(File instancePath) {
		final InstanceDetails f = new InstanceDetails();
		
		InputStream is;
		try {
			is = new FileInputStream(instancePath);
		} catch (FileNotFoundException e1) {
			throw new IllegalStateException(e1);
		}

		InputStreamReader isr;
		try {
			isr = new InputStreamReader(is,"UTF-8");
		} catch(UnsupportedEncodingException uee) {
			Log.w(t,"UTF 8 encoding unavailable, trying default encoding");
			isr = new InputStreamReader(is); 
		}
		
		if(isr != null) {
			
			Document doc;
			try {
				doc = XFormParser.getXMLDocument(isr);
			} finally {
				try {
					isr.close();
				} catch (IOException e) {
					Log.w(t,"Error closing form reader");
					e.printStackTrace();
				}
			}
			
			Element tle = doc.getRootElement();
			String id = tle.getAttributeValue(null, "id");
			String xmlns = tle.getAttributeValue(null, "xmlns");
			String modelVersion = tle.getAttributeValue(null, "version");
			String uiVersion = tle.getAttributeValue(null, "uiVersion");
			
			f.formId = (id == null) ? xmlns : id;
			f.modelVersion = (modelVersion == null) ? null : Integer.valueOf(modelVersion);
			f.uiVersion = (uiVersion == null) ? null : Integer.valueOf(uiVersion);
		}
		
		return f;
	}
    private class SyncWithFilesystem extends AsyncTask<Void,Void,Void> {

		@Override
		protected Void doInBackground(Void... unused) {
			boolean repeat = true;
			while ( repeat ) {
				int changeCount = 0;
		    	// get the available submissions directories...
		        ArrayList<String> xmlSubmissions = new ArrayList<String>();
		        if (FileUtils.createFolder(FileUtils.INSTANCES_PATH)) {
		        	xmlSubmissions = FileUtils.getFoldersAsArrayList(FileUtils.INSTANCES_PATH);
		        }

		        List<Long> toMarkAsSubmitted = new ArrayList<Long>();
		        
		        Cursor c = getStorageDb().query(SUBMISSIONS_TABLE, 
		        				new String[] { KEY_ID, KEY_INSTANCE_DIRECTORY_PATH, KEY_STATUS },
		        						null, null, null);
		        int idxKey = c.getColumnIndex(KEY_ID);
		        int idxInstanceDirectory = c.getColumnIndex(KEY_INSTANCE_DIRECTORY_PATH);
		        int idxStatus = c.getColumnIndex(KEY_STATUS);
		        while (c.moveToNext()) {
		    		String dirPath = c.getString(idxInstanceDirectory);
		    		File submissionDir = new File(dirPath);
		    		File instanceFile = new File(FileUtils.getInstanceFilePath(dirPath));

		    		if ( !instanceFile.exists() ) {
		    			// file doesn't exist -- must have been submitted
		    			String status = c.getString(idxStatus);
		    			if ( status.compareToIgnoreCase(STATUS_SUBMITTED) != 0 ) {
		    				toMarkAsSubmitted.add(c.getLong(idxKey));
		    			}
		    		} else {
		    			// file and database entry exist -- everything is ok
		    			xmlSubmissions.remove(submissionDir.getAbsolutePath());
		    		}
		    	}
		    	c.close();
		    	
		    	// update the status of the missing submissions to submitted...
		    	ContentValues values = new ContentValues();
		    	values.put(KEY_STATUS, STATUS_SUBMITTED);
		    	Date now = new Date();
		    	values.put(KEY_DISPLAY_SUBTEXT, getDisplaySubtext( STATUS_SUBMITTED, now));
		    	values.put(KEY_LAST_STATUS_CHANGE_DATE, now.getTime());
		    	
		    	for ( Long id : toMarkAsSubmitted ) {
		    		getStorageDb().update(SUBMISSIONS_TABLE, values, KEY_ID + " = ?",
		    				new String[] { Long.toString(id)} );
					changeCount++;
		    	}
		    	
		    	// and add the newly found submissions...
		    	for ( String xmlSubmissionDirs : xmlSubmissions ) {
		        	File submissionDir = new File(xmlSubmissionDirs);
		        	File formXml = new File(FileUtils.getInstanceFilePath(xmlSubmissionDirs));
		        	File submissionXml = new File(FileUtils.getSubmissionBlobPath(xmlSubmissionDirs));
		        	
		        	if ( formXml.exists() || submissionXml.exists() ) {
		        		// instance or submission exists -- must be incomplete submission 
			    		ContentValues v = new ContentValues();
			    		v.put(KEY_INSTANCE_DIRECTORY_PATH, submissionDir.getAbsolutePath());
			    		SubmissionsStorage.this.insert(CONTENT_URI_INFO_DATASET, v);
						changeCount++;
		        	} else {
		        		// must be submitted submission
		        		if ( submissionDir.listFiles().length == 0 ) {
		        			if ( !submissionDir.delete() ) {
		        				Log.i(t, "Failed to delete directory: " + xmlSubmissionDirs);
		        			} else {
								changeCount++;
		        			}
		        		} else {
		        			// we have some too-large file for upload -- need to preserve it...
				    		ContentValues v = new ContentValues();
				    		v.put(KEY_STATUS, STATUS_SUBMITTED);
				    		// TODO: change to use INSTANCE_DIRECTORY instead of filename
				    		// SubmissionsStorage.this.insert(CONTENT_URI_INFO_DATASET, v);
							// changeCount++;
		        		}
		        	}
		        }

		    	Log.i(t, "Number of changes to the known instances list: " + Integer.toString(changeCount));
		    	if ( changeCount != 0 ) {
		    		getContext().getContentResolver().notifyChange(SubmissionsStorage.CONTENT_URI, null);
		    	}
		    	repeat = SubmissionsStorage.this.removeGuardedSync();
			}
			return null;
		}
    }
    
    private int requestCount = 0;
    
    private synchronized boolean removeGuardedSync() {
    	--requestCount;
    	return (requestCount == 0);
    }
    
    private synchronized void sync() {
    	++requestCount;
    	if ( requestCount == 1) {
    		// no requests were outstanding.
    		new SyncWithFilesystem().execute((Void[]) null);
    	}
    }

    private static class InstanceFilesetInfo {
    	String id;
    	File instanceDirPath;
    }

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#delete(android.net.Uri, java.lang.String, java.lang.String[])
	 */
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		SelectionCriteria criteria = null;
		switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW)) {
		case INFO_ALLROWS:
			criteria = new SelectionCriteria(selection, selectionArgs);
			break;
		case INFO_SINGLE_ROW:
			criteria = new SelectionCriteria(selection, selectionArgs, uri);
			break;
		}
		String[] projection = new String[] { KEY_ID, KEY_INSTANCE_DIRECTORY_PATH };
		
		List<InstanceFilesetInfo> toDelete = new ArrayList<InstanceFilesetInfo>();
		
		Cursor c = null;
		try {
			c = getStorageDb().query(SUBMISSIONS_TABLE, projection, criteria.selection, criteria.selectionArgs, null);
			int idxId = c.getColumnIndex(KEY_ID);
			int idxInstanceDirPath = c.getColumnIndex(KEY_INSTANCE_DIRECTORY_PATH);
	
			while ( c.moveToNext() ) {
				InstanceFilesetInfo f = new InstanceFilesetInfo();
				f.id = c.getString(idxId);
				f.instanceDirPath = new File(c.getString(idxInstanceDirPath));
				toDelete.add(f);
			}
		} finally {
			try {
				if ( c != null ) {
					c.close();
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			c = null;
		}
		
		int deleteCount = 0;
		for ( InstanceFilesetInfo f : toDelete ) {
			boolean success = true;
			for ( File file : f.instanceDirPath.listFiles() ) {
				try {
					success = success && file.delete();
				} catch ( Exception e ) {
					e.printStackTrace();
					Log.e(t, "Unable to delete instance file: " + file.getAbsolutePath());
				}
			}
			try {
				success = success && f.instanceDirPath.delete();
			} catch ( Exception e ) {
				e.printStackTrace();
				Log.e(t, "Unable to delete instance directory: " + f.instanceDirPath.getAbsolutePath());
			}
			
			int found = getStorageDb().delete(SUBMISSIONS_TABLE, KEY_ID + " = ?", new String[] { f.id } );
			if ( found != 1 ) {
				Log.w(t, "Unexpected found count(" + Integer.toString(found) 
						+ ") returned from delete on instance record: " + f.instanceDirPath.getAbsolutePath());
			}
			deleteCount += found;
		}
		
		if ( deleteCount > 0 ) {
    		getContext().getContentResolver().notifyChange(FormsStorage.CONTENT_URI, null);
		}
		return deleteCount;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#getType(android.net.Uri)
	 */
	@Override
	public String getType(Uri uri) {
		String s = null;
		switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW)) {
		case INFO_ALLROWS:
			s = Collect.getInstance().getString(R.string.mime_type_submissions_list);
			break;
		case INFO_SINGLE_ROW:
			s = Collect.getInstance().getString(R.string.mime_type_submissions_item);
			break;
		}
		return s;
	}
	
	/* (non-Javadoc)
	 * @see android.content.ContentProvider#insert(android.net.Uri, android.content.ContentValues)
	 */
	@Override
	public Uri insert(Uri uri, ContentValues values) {
		// any insert into FORM_FILE or INFO dataset is equivalent
		int type = matchOnly(uri, INFO_ALLROWS);
		
		String instanceDirPath = values.getAsString(KEY_INSTANCE_DIRECTORY_PATH);
		if ( instanceDirPath == null ) {
			throw new IllegalArgumentException("insertions must specify "
												+ KEY_INSTANCE_DIRECTORY_PATH );
		}
    	File instanceDir = new File(instanceDirPath);
    	File xmlInstanceFile = new File(FileUtils.getInstanceFilePath(instanceDirPath));
    	File submissionFile = new File(FileUtils.getSubmissionBlobPath(instanceDirPath));
    	
		// double-check that the form file does not already exist...
		Cursor c = null;
		try {
			FilterUtils.FilterCriteria fd = 
				FilterUtils.buildSelectionClause(KEY_INSTANCE_DIRECTORY_PATH, instanceDir.getAbsolutePath());
			c = getStorageDb().query(SUBMISSIONS_TABLE, new String[] { KEY_ID },
					fd.selection, fd.selectionArgs, null);
			if ( c.moveToNext() ) {
				// the file already exists in database -- return the link...
				long keyId = c.getLong(c.getColumnIndex(KEY_ID));
		        if ( type == INFO_ALLROWS ) {
		        	return ContentUris.withAppendedId(CONTENT_URI_INFO_DATASET,
						keyId);
		        } else {
		        	throw new IllegalStateException("missing case");
		        }
			}
		} finally {
			try {
				if ( c != null ) {
					c.close();
				}
			} catch ( Exception e ) {
				e.printStackTrace();
			}
			c = null;
		}
		
		// doesn't exist --- insert it.
        Date now = new Date();
        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
        g.setTime(now);

        // build up actual inserted content
        ContentValues v = new ContentValues();
        v.put(KEY_INSTANCE_DIRECTORY_PATH, instanceDir.getAbsolutePath());
        if ( values.containsKey(KEY_DISPLAY_NAME) ) {
        	v.put(KEY_DISPLAY_NAME, values.getAsString(KEY_DISPLAY_NAME));
        } else {
        	v.put(KEY_DISPLAY_NAME, instanceDir.getName());
        }
        
        String status = submissionFile.exists() ? STATUS_COMPLETE : STATUS_INCOMPLETE;
        if ( values.containsKey(KEY_STATUS) ) {
        	status = values.getAsString(KEY_STATUS);
        }
        v.put(KEY_STATUS, status);
        String subtext = getDisplaySubtext(status, now);
        if ( values.containsKey(KEY_DISPLAY_SUB_SUBTEXT) ) {
        	String subsubtext = values.getAsString(KEY_DISPLAY_SUB_SUBTEXT).trim();
        	if ( subsubtext.length() != 0 ) {
        		subtext += "\n[" + subsubtext + "]";
        	}
        }
        v.put(KEY_DISPLAY_SUBTEXT, subtext);
        v.put(KEY_LAST_STATUS_CHANGE_DATE, now.getTime());
        boolean canEditSubmission = xmlInstanceFile.exists();
    	v.put(KEY_CAN_EDIT_SUBMISSION, canEditSubmission);
		
        // insert
        long keyId = getStorageDb().insert(SUBMISSIONS_TABLE, v);
		getContext().getContentResolver().notifyChange(FormsStorage.CONTENT_URI, null);
        
        // and return the appropriate Uri (to file or metadata)
        if ( type == INFO_ALLROWS ) {
        	return ContentUris.withAppendedId(CONTENT_URI_INFO_DATASET,
				keyId);
        } else {
        	throw new IllegalStateException("missing case");
        }
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#openFile(android.net.Uri, java.lang.String)
	 */
	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode)
			throws FileNotFoundException {

		SelectionCriteria criteria = null;
		String[] projection = new String[] { KEY_INSTANCE_DIRECTORY_PATH };
		int uriType = matchOnly(uri, INSTANCE_FILE_SINGLE_ROW, SUBMISSION_BLOB_FILE_SINGLE_ROW);
		boolean wantInstanceFile = true;
		switch (uriType) {
		case INSTANCE_FILE_SINGLE_ROW:
			criteria = new SelectionCriteria(null, null, uri);
			wantInstanceFile = true;
			break;
		case SUBMISSION_BLOB_FILE_SINGLE_ROW:
			criteria = new SelectionCriteria(null, null, uri);
			wantInstanceFile = false;
			break;
		}
		
		File file;
		Cursor c = null;
		try {
			c = getStorageDb().query(SUBMISSIONS_TABLE, projection, 
										criteria.selection, criteria.selectionArgs, null );
			if ( !c.moveToFirst() ) {
				throw new FileNotFoundException("Unable to locate indicated record: " + uri.toString());
			}
		
			String instanceDirPath = c.getString(c.getColumnIndex(projection[0]));
			if ( instanceDirPath == null ) {
				throw new FileNotFoundException("No path defined for this instance: " + uri.toString());
			}
			if ( wantInstanceFile ) {
				file = new File(FileUtils.getInstanceFilePath(instanceDirPath));
			} else {
				file = new File(FileUtils.getSubmissionBlobPath(instanceDirPath));
			}
		} finally {
			if ( c != null ) {
				try { 
					c.close();
				} catch ( Exception e ) {
					e.printStackTrace();
				}
				c = null;
			}
		}

		int modeInt = ParcelFileDescriptor.MODE_READ_ONLY;
		if ( mode.compareToIgnoreCase("r") == 0 ) {
			modeInt = ParcelFileDescriptor.MODE_READ_ONLY;
			if ( !file.exists() ) {
				throw new FileNotFoundException("Unable to locate file for record: " + uri.toString());
			}
		} else if ( mode.compareToIgnoreCase("w") == 0 ) {
			modeInt = ParcelFileDescriptor.MODE_WRITE_ONLY |
					( file.exists() ? 
						ParcelFileDescriptor.MODE_TRUNCATE :
						ParcelFileDescriptor.MODE_CREATE );
		}
		
		return ParcelFileDescriptor.open(file, modeInt );
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#query(android.net.Uri, java.lang.String[], java.lang.String, java.lang.String[], java.lang.String)
	 */
	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {
		isExposableProjection(projection);
		String[] fiProjection = null;
		SelectionCriteria c = null;
		int type;
		switch (type = matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW, FORMS_INFO_URI_ALLROWS, FORMS_INFO_URI_SINGLE_ROW)) {
		case INFO_ALLROWS:
			c = new SelectionCriteria(selection, selectionArgs);
			break;
		case FORMS_INFO_URI_ALLROWS:
			c = new SelectionCriteria(selection, selectionArgs);
			break;
		case FORMS_INFO_URI_SINGLE_ROW:
			c = new SelectionCriteria(selection, selectionArgs, uri);
			break;
		case INFO_SINGLE_ROW:
			c = new SelectionCriteria(selection, selectionArgs, uri);
			break;
		}
		if ( type == FORMS_INFO_URI_ALLROWS || type == FORMS_INFO_URI_SINGLE_ROW ) {
			fiProjection = projection;
			for ( String s : fiProjection ) {
				if ( KEY_ID.equals(s) || KEY_URI_FORMS_INFO.equals(s) ) continue;
				throw new IllegalStateException("Invalid projection element: " + s);
			}
			projection = new String[] { KEY_ID, KEY_INSTANCE_DIRECTORY_PATH };
		}
		Cursor cursor = getStorageDb().query(SUBMISSIONS_TABLE, projection, 
										c.selection, c.selectionArgs, sortOrder );
		
		if ( type == FORMS_INFO_URI_ALLROWS || type == FORMS_INFO_URI_SINGLE_ROW ) {
			MatrixCursor mc = new MatrixCursor( fiProjection);
			try {
				while ( cursor.moveToNext() ) {
					long instanceId = cursor.getLong(cursor.getColumnIndex(KEY_ID));
					String instanceDirPath = cursor.getString(cursor.getColumnIndex(KEY_INSTANCE_DIRECTORY_PATH));
					if ( instanceDirPath == null || instanceDirPath.length() == 0 ) continue;
					File instance = new File(FileUtils.getInstanceFilePath(instanceDirPath));
					if ( !instance.exists() ) continue;
					InstanceDetails id = retrieveDetails(instance);
					Cursor fc = null;
					try {
						FilterUtils.FilterCriteria fd = FilterUtils.buildSelectionClause(
								new String[]{FormsStorage.KEY_FORM_ID,
										FormsStorage.KEY_MODEL_VERSION,
										FormsStorage.KEY_UI_VERSION},
								new Object[] {id.formId, id.modelVersion, id.uiVersion },
								null);
						
						fc = getContext().getContentResolver().query(FormsStorage.CONTENT_URI_INFO_DATASET,
								new String[] { FormsStorage.KEY_ID },
								fd.selection, fd.selectionArgs, null);
						while ( fc.moveToNext() ) {
							long formId = fc.getLong(fc.getColumnIndex(FormsStorage.KEY_ID));
							String[] rowValue = new String[fiProjection.length];
							for ( int i = 0 ; i < fiProjection.length ; ++i ) {
								if ( fiProjection[i].equals(KEY_ID) ) {
									rowValue[i] = Long.toString(instanceId);
								}
								if ( fiProjection[i].equals(KEY_URI_FORMS_INFO) ) {
									rowValue[i] = ContentUris.withAppendedId(FormsStorage.CONTENT_URI_INFO_DATASET, formId).toString();
								}
							}
							mc.addRow(rowValue);
						}
					} finally {
						if ( fc != null ) {
							fc.close();
						}
					}
				}
			} finally {
				if ( cursor != null ) {
					cursor.close();
				}
			}
			mc.setNotificationUri(getContext().getContentResolver(), uri);
			return mc;
		}
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}

	/* (non-Javadoc)
	 * @see android.content.ContentProvider#update(android.net.Uri, android.content.ContentValues, java.lang.String, java.lang.String[])
	 */
	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		SelectionCriteria c = null;
		switch (matchOnly(uri, INFO_ALLROWS, INFO_SINGLE_ROW)) {
		case INFO_ALLROWS:
			c = new SelectionCriteria(selection, selectionArgs);
			break;
		case INFO_SINGLE_ROW:
			c = new SelectionCriteria(selection, selectionArgs, uri);
			break;
		}

		if (values.containsKey(KEY_STATUS)) {
			String status = values.getAsString(KEY_STATUS);
			
			// doesn't exist --- insert it.
	        Date now = new Date();
	        GregorianCalendar g = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
	        g.setTime(now);
			
	        String subtext = getDisplaySubtext(status, now);
	        if ( values.containsKey(KEY_DISPLAY_SUB_SUBTEXT) ) {
	        	String subsubtext = values.getAsString(KEY_DISPLAY_SUB_SUBTEXT).trim();
	        	if ( subsubtext.length() != 0 ) {
	        		subtext += "\n[" + subsubtext + "]";
	        	}
	        	values.remove(KEY_DISPLAY_SUB_SUBTEXT);
	        }
	        values.put(KEY_DISPLAY_SUBTEXT, subtext);
	        values.put(KEY_LAST_STATUS_CHANGE_DATE, now.getTime());
		}
		// TODO: update selective fields...
		int change = getStorageDb().update(SUBMISSIONS_TABLE, values, c.selection, c.selectionArgs);
		if ( change != 0 ) {
    		getContext().getContentResolver().notifyChange(CONTENT_URI, null);
		}
		return change;
	}

}
