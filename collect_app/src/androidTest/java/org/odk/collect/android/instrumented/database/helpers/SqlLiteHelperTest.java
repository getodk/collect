package org.odk.collect.android.instrumented.database.helpers;

import android.Manifest;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.rules.RuleChain;
import org.junit.runners.Parameterized;
import org.odk.collect.android.support.ResetStateRule;

public abstract class SqlLiteHelperTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule());

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
     * discussion at https://github.com/getodk/collect/pull/3250#issuecomment-516439704
     */
    void ensureMigrationAppliesFully(SQLiteOpenHelper databaseHelper) {
        databaseHelper.getReadableDatabase().close();
    }
}
