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

package org.odk.collect.android.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

public class StorageDatabase {

    private final ODKSQLiteOpenHelper mDbHelper;
    private SQLiteDatabase mDb = null;

    public StorageDatabase(ODKSQLiteOpenHelper mDbHelper) {
    	this.mDbHelper = mDbHelper;
	}

	public StorageDatabase open() throws SQLException {
        mDb = mDbHelper.getWritableDatabase();

        return this;
    }

    public void close() {
    	try {
    		mDbHelper.close();
    	} finally {
    		try {
	    		mDb.close();
	    	} finally {
	    		mDb = null;
	    	}
    	}
    }
    
    public SQLiteDatabase getDb() {
    	if ( mDb == null ) {
    		open();
    	}
    	return mDb;
    }

    public long insert( String tableName, ContentValues values ) {
    	if ( values == null || values.size() == 0 ) {
    		throw new IllegalArgumentException(
    				"insert of empty ContentValues array is not allowed");
    	}
    	return getDb().insert( tableName, null, values);
    }
    
	public int delete( String tableName, String selection, String[] selectionArgs ) {
		return getDb().delete(tableName, selection, selectionArgs ); 
	}
	
	public int update( String tableName, ContentValues values, String selection, String[] selectionArgs ) {
		return getDb().update(tableName, values, selection, selectionArgs);
	}
	
	public Cursor query( String tableName, String[] projection, String selection, String[] selectionArgs, String sortOrder ) {
		return getDb().query(tableName, projection, selection, selectionArgs,
						 null, null, sortOrder, null );
	}
}
