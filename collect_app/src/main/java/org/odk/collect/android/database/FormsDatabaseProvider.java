package org.odk.collect.android.database;

import android.database.sqlite.SQLiteDatabase;

import org.odk.collect.android.storage.StoragePathProvider;

import javax.inject.Singleton;

/**
 * Holds "connection" (in this case an instance of {@link android.database.sqlite.SQLiteOpenHelper}
 * to the Forms database. According to the Android team these should be kept open for the whole apps
 * lifecycle.
 *
 * @see <a href="https://stackoverflow.com/questions/6608498/best-place-to-close-database-connection">https://stackoverflow.com/questions/6608498/best-place-to-close-database-connection</a>
 */

@Singleton
public class FormsDatabaseProvider {

    private FormsDatabaseHelper dbHelper;

    public SQLiteDatabase getWriteableDatabase() {
        return getDbHelper().getWritableDatabase();
    }

    public SQLiteDatabase getReadableDatabase() {
        return getDbHelper().getReadableDatabase();
    }

    public void recreateDatabaseHelper() {
        dbHelper = new FormsDatabaseHelper(new FormDatabaseMigrator(), new StoragePathProvider());
    }

    public void releaseDatabaseHelper() {
        if (dbHelper != null) {
            dbHelper.close();
            dbHelper = null;
        }
    }

    private synchronized FormsDatabaseHelper getDbHelper() {
        if (dbHelper == null) {
            recreateDatabaseHelper();
        }

        return dbHelper;
    }
}
