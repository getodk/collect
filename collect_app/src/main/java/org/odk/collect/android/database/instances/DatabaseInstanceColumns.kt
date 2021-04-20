package org.odk.collect.android.database.instances

import android.provider.BaseColumns

object DatabaseInstanceColumns : BaseColumns {

    // instance column names
    const val DISPLAY_NAME = "displayName"
    const val SUBMISSION_URI = "submissionUri"
    const val INSTANCE_FILE_PATH = "instanceFilePath"
    const val JR_FORM_ID = "jrFormId"
    const val JR_VERSION = "jrVersion"
    const val STATUS = "status"
    const val CAN_EDIT_WHEN_COMPLETE = "canEditWhenComplete"
    const val LAST_STATUS_CHANGE_DATE = "date"
    const val DELETED_DATE = "deletedDate"
    const val GEOMETRY = "geometry"
    const val GEOMETRY_TYPE = "geometryType"
}
