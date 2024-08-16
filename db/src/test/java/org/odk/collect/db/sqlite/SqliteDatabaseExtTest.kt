package org.odk.collect.db.sqlite

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.db.sqlite.SQLiteDatabaseExt.doesColumnExist

@RunWith(AndroidJUnit4::class)
class SqliteDatabaseExtTest {

    @Test
    fun doesColumnExistTest() {
        val tableName = "testTable"
        val db = ApplicationProvider.getApplicationContext<Context>()
            .openOrCreateDatabase("testDatabase", Context.MODE_PRIVATE, null)
        db.execSQL("CREATE TABLE $tableName (id integer, column1 text);")

        assertThat(db.doesColumnExist(tableName, "id"), equalTo(true))
        assertThat(db.doesColumnExist(tableName, "column1"), equalTo(true))
        assertThat(db.doesColumnExist(tableName, "blah"), equalTo(false))
    }
}
