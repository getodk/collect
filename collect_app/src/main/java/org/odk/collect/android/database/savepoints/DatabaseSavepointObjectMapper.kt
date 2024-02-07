package org.odk.collect.android.database.savepoints

import android.content.ContentValues
import android.database.Cursor
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.shared.PathUtils

object DatabaseSavepointObjectMapper {
    fun getSavepointFromCurrentCursorPosition(
        cursor: Cursor,
        cachePath: String,
        instancesPath: String
    ): Savepoint {
        val formDbIdColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.FORM_DB_ID)
        val instanceDbIdColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.INSTANCE_DB_ID)
        val savepointFilePathColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.SAVEPOINT_FILE_PATH)
        val instanceDirPathColumnIndex = cursor.getColumnIndex(DatabaseSavepointsColumns.INSTANCE_FILE_PATH)

        return Savepoint(
            cursor.getLong(formDbIdColumnIndex),
            if (cursor.isNull(instanceDbIdColumnIndex)) null else cursor.getLong(instanceDbIdColumnIndex),
            PathUtils.getAbsoluteFilePath(
                cachePath,
                cursor.getString(savepointFilePathColumnIndex)
            ),
            PathUtils.getAbsoluteFilePath(
                instancesPath,
                cursor.getString(instanceDirPathColumnIndex)
            )
        )
    }

    fun getValuesFromSavepoint(savepoint: Savepoint, cachePath: String, instancesPath: String): ContentValues {
        return ContentValues().apply {
            put(DatabaseSavepointsColumns.FORM_DB_ID, savepoint.formDbId)
            put(DatabaseSavepointsColumns.INSTANCE_DB_ID, savepoint.instanceDbId)
            put(DatabaseSavepointsColumns.SAVEPOINT_FILE_PATH, PathUtils.getRelativeFilePath(cachePath, savepoint.savepointFilePath))
            put(DatabaseSavepointsColumns.INSTANCE_FILE_PATH, PathUtils.getRelativeFilePath(instancesPath, savepoint.instanceFilePath))
        }
    }
}
