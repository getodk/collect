package org.odk.collect.android.provider

import android.content.ContentResolver
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`is`
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.provider.ProjectsProvider.Companion.PROJECT_NAME
import org.odk.collect.android.provider.ProjectsProvider.Companion.PROJECT_UUID
import org.odk.collect.projects.Project

@RunWith(AndroidJUnit4::class)
class ProjectsProviderTest {

    private lateinit var contentResolver: ContentResolver

    @Before
    fun setup() {
        contentResolver = ApplicationProvider.getApplicationContext<Context>().contentResolver
    }

    @Test
    fun `Query should return list of existing projects`() {
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>()).projectsRepository().save(Project("Project 1", "1", "#FF0000", "1"))
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>()).projectsRepository().save(Project("Project 2", "2", "#00FF00", "2"))
        DaggerUtils.getComponent(ApplicationProvider.getApplicationContext<Context>()).projectsRepository().save(Project("Project 3", "3", "#000000", "3"))

        contentResolver.query(ProjectsProviderAPI.CONTENT_URI, null, null, null, null).use { cursor ->
            assertThat(cursor!!.count, `is`(3))
            assertThat(cursor.columnCount, `is`(2))

            cursor.moveToNext()

            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_UUID)), `is`("1"))
            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_NAME)), `is`("Project 1"))

            cursor.moveToNext()

            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_UUID)), `is`("2"))
            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_NAME)), `is`("Project 2"))

            cursor.moveToNext()

            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_UUID)), `is`("3"))
            assertThat(cursor.getString(cursor.getColumnIndex(PROJECT_NAME)), `is`("Project 3"))
        }
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `Insert should throw UnsupportedOperationException`() {
        contentResolver.insert(ProjectsProviderAPI.CONTENT_URI, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `Delete should throw UnsupportedOperationException`() {
        contentResolver.delete(ProjectsProviderAPI.CONTENT_URI, null, null)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun `Update should throw UnsupportedOperationException`() {
        contentResolver.update(ProjectsProviderAPI.CONTENT_URI, null, null, null)
    }
}
