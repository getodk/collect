package org.odk.collect.projects.providers

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.BaseColumns
import org.odk.collect.projects.ProjectsDependencyComponentProvider
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.projects.providers.ProjectsProviderAPI.PROJECT_NAME
import org.odk.collect.projects.providers.ProjectsProviderAPI.PROJECT_UUID
import javax.inject.Inject

class ProjectsProvider : ContentProvider() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    // Do not call it in onCreate() https://stackoverflow.com/questions/23521083/inject-database-in-a-contentprovider-with-dagger
    private fun daggerInit() {
        val provider = context!!.applicationContext as ProjectsDependencyComponentProvider
        provider.projectsDependencyComponent.inject(this)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        daggerInit()

        val cursor = MatrixCursor(arrayOf(BaseColumns._ID, PROJECT_UUID, PROJECT_NAME))
        var index = 1
        projectsRepository.getAll().forEach {
            cursor.addRow(arrayOf<Any>(index++, it.uuid, it.name))
        }
        return cursor
    }

    override fun getType(uri: Uri) = ProjectsProviderAPI.CONTENT_TYPE

    override fun insert(uri: Uri, values: ContentValues?) = throw UnsupportedOperationException()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = throw UnsupportedOperationException()

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = throw UnsupportedOperationException()
}
