/* The MIT License (MIT)
 *
 *       Copyright (c) 2015 PMA2020
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.odk.collect.android.database;

import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.odk.collect.android.application.Collect;

/**
 *  Implements database creation and upgrade for form relations.
 *
 *  Creator: James K. Pringle
 *  E-mail: jpringle@jhu.edu
 *  Last modified: 20 August 2015
 */
public class FormRelationsDb extends ODKSQLiteOpenHelper {

    private static final String TAG = "FormRelationsDb";
    private static final boolean LOCAL_LOG = true;

    public FormRelationsDb() {
        super(Collect.METADATA_PATH, FormRelationsContract.DATABASE_NAME, null,
                FormRelationsContract.DATABASE_VERSION);
        if (LOCAL_LOG) {
            Log.d(TAG, "constructor");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (LOCAL_LOG) {
            Log.d(TAG, "onCreate. Created relations table.");
        }
        db.execSQL(FormRelationsContract.FormRelations.CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                + newVersion + ", which will destroy all old data");
        db.execSQL(FormRelationsContract.FormRelations.DELETE_TABLE);
        onCreate(db);
    }

    /*
     *  public void insertSomething(something) {
     *      SQLiteDatabase db = this.getWritableDatabase();
     *      //do stuff
     *      db.close();
     */
}
