package org.odk.collect.android.database.helpers;

import android.database.sqlite.SQLiteOpenHelper;

import org.junit.runners.Parameterized;

public abstract class SqlLiteHelperTest {
    @Parameterized.Parameter
    public String description;

    @Parameterized.Parameter(1)
    public String dbFilename;

    static final String TEMPORARY_EXTENSION = ".real";

    /**
     * Gets a read-only reference to the instances database and then immediately releases it.
     *
     * Without this, it appears that the migrations only get partially applied. It's not clear how
     * this is possible since calls to onDowngrade and onUpgrade are wrapped in transactions. See
     * discussion at https://github.com/opendatakit/collect/pull/3250#issuecomment-516439704
     */
    void ensureMigrationAppliesFully(SQLiteOpenHelper databaseHelper) {
        databaseHelper.getReadableDatabase().close();
    }
}
