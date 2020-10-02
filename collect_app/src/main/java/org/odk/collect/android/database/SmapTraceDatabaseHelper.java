
package org.odk.collect.android.database.helpers;

import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.TraceProviderAPI;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.utilities.SQLiteUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;
import static org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.SOURCE;
import static org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LAT;
import static org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.LON;
import static org.odk.collect.android.provider.TraceProviderAPI.TraceColumns.TIME;


/**
 * This class helps open, create, and upgrade the database file.
 */
public class SmapTraceDatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "trace.db";
    private static final String TABLE_NAME = "trace";

    static final int DATABASE_VERSION = 2;

    private static final String[] COLUMN_NAMES_V1 = {
            _ID,
            LAT,
            LON,
            TIME,
    };

    private static final String[] COLUMN_NAMES_V2 = {
            _ID,
            SOURCE,
            LAT,
            LON,
            TIME,
            };



    static final String[] CURRENT_VERSION_COLUMN_NAMES = COLUMN_NAMES_V2;  // smap

    private static boolean isDatabaseBeingMigrated;

    public SmapTraceDatabaseHelper() {
        super(new DatabaseContext(new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA)), DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static String getDatabasePath() {
        return new StoragePathProvider().getDirPath(StorageSubdirectory.METADATA) + File.separator + DATABASE_NAME;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTraceTableV2(db, TABLE_NAME);
    }

    /**
     * Upgrades the database.
     *
     * When a new migration is added, a corresponding test case should be added to
     * InstancesDatabaseHelperTest by copying a real database into assets.
     */
    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

            switch (oldVersion) {
                case 1:
                    upgradeToVersion2(db);
                    break;
                default:
                    Timber.i("Unknown version %d", oldVersion);
            }

            Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            isDatabaseBeingMigrated = false;
        } catch (SQLException e) {
            isDatabaseBeingMigrated = false;
            throw e;
        }
    }

    private void upgradeToVersion2(SQLiteDatabase db) {
        if (!SQLiteUtils.doesColumnExist(db, TABLE_NAME, SOURCE)) {
            SQLiteUtils.addColumn(db, TABLE_NAME, SOURCE, "text");
        }
    }

    private void createTraceTableV2(SQLiteDatabase db, String name) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + name + " ("
                + _ID + " integer primary key, "
                + SOURCE + " text, "
                + LAT + " double not null, "
                + LON + " double not null, "
                +TraceProviderAPI.TraceColumns.TIME + " long not null "
                + ");");
    }

    public static void databaseMigrationStarted() {
        isDatabaseBeingMigrated = true;
    }

    public static boolean isDatabaseBeingMigrated() {
        return isDatabaseBeingMigrated;
    }

    public static boolean databaseNeedsUpgrade() {
        boolean isDatabaseHelperOutOfDate = false;
        try {
            SQLiteDatabase db = SQLiteDatabase.openDatabase(SmapTraceDatabaseHelper.getDatabasePath(), null, SQLiteDatabase.OPEN_READONLY);
            isDatabaseHelperOutOfDate = SmapTraceDatabaseHelper.DATABASE_VERSION != db.getVersion();
            db.close();
        } catch (SQLException e) {
            Timber.i(e);
        }
        return isDatabaseHelperOutOfDate;
    }
}
