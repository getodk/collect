package org.odk.collect.android.database.helpers;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.odk.collect.android.application.Collect;
import org.odk.collect.android.database.DatabaseContext;
import org.odk.collect.android.provider.XPathProviderAPI;
import org.odk.collect.android.utilities.CustomSQLiteQueryBuilder;

import timber.log.Timber;

import static android.provider.BaseColumns._ID;

/**
 * This class helps open, create, and upgrade the database file.
 */
public class XPathDatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "xpath_expr_index.db";
    public static final String XPATH_TABLE_NAME = "xpath_expr_index";

    private static final int DATABASE_VERSION = 7;


    public XPathDatabaseHelper() {
        super(new DatabaseContext(Collect.METADATA_PATH), DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createXPathTableV7(db);
    }

    @SuppressWarnings({"checkstyle:FallThrough"})
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Timber.i("Upgrading database from version %d to %d", oldVersion, newVersion);

        Timber.i("Upgrading database from version %d to %d completed with success.", oldVersion, newVersion);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        CustomSQLiteQueryBuilder
                .begin(db)
                .dropIfExists(XPATH_TABLE_NAME)
                .end();

        createXPathTableV7(db);

        Timber.i("Downgrading database from %d to %d completed with success.", oldVersion, newVersion);
    }

    private void createXPathTableV7(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + XPATH_TABLE_NAME + " ("
                + _ID + " integer primary key autoincrement, "
                + XPathProviderAPI.XPathsColumns.EVAL_EXPR + " text not null, "
                + XPathProviderAPI.XPathsColumns.GENERIC_TREE_REF + " text, "
                + XPathProviderAPI.XPathsColumns.SPECIFIC_TREE_REF_ + " text );");
    }

}
