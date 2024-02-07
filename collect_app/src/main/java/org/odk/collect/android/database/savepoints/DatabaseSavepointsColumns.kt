package org.odk.collect.android.database.savepoints

import android.provider.BaseColumns

object DatabaseSavepointsColumns : BaseColumns {
    /**
     * The form db id of the blank form that the savepoint belongs to.
     */
    const val FORM_DB_ID = "fromDbId"

    /**
     * The instance db id of the saved form that the savepoint belongs to.
     */
    const val INSTANCE_DB_ID = "instanceDbId"

    /**
     * The relative path to the file containing a savepoint.
     */
    const val SAVEPOINT_FILE_PATH = "savepointFilePath"

    /**
     * The relative path to the instance file.
     */
    const val INSTANCE_FILE_PATH = "instanceFilePath"
}
