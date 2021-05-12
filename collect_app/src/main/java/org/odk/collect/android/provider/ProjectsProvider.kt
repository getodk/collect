package org.odk.collect.android.provider

import android.content.ContentProvider
import android.content.ContentValues
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.projects.ProjectsRepository
import javax.inject.Inject

class ProjectsProvider : ContentProvider() {

    @Inject
    lateinit var projectsRepository: ProjectsRepository

    // Do not call it in onCreate() https://stackoverflow.com/questions/23521083/inject-database-in-a-contentprovider-with-dagger
    private fun deferDaggerInit() {
        DaggerUtils.getComponent(context).inject(this)
    }

    override fun onCreate(): Boolean {
        return true
    }

    override fun query(uri: Uri, projection: Array<out String>?, selection: String?, selectionArgs: Array<out String>?, sortOrder: String?): Cursor? {
        deferDaggerInit()

        val cursor = MatrixCursor(arrayOf(PROJECT_UUID, PROJECT_NAME))
        projectsRepository.getAll().forEach {
            cursor.addRow(arrayOf<Any>(it.uuid, it.name))
        }
        return cursor
    }

    override fun getType(uri: Uri) = ProjectsProviderAPI.CONTENT_TYPE

    override fun insert(uri: Uri, values: ContentValues?) = throw UnsupportedOperationException()

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?) = throw UnsupportedOperationException()

    override fun update(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?) = throw UnsupportedOperationException()

    companion object {
        const val PROJECT_UUID = "uuid"
        const val PROJECT_NAME = "name"
    }
}
