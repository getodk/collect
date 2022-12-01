package org.odk.collect.android.geo

import org.odk.collect.android.loaders.TaskEntry
import org.odk.collect.android.utilities.KeyValueJsonFns

class TaskMapMarker(
    val mapPoint: MapPoint,
    val task: TaskEntry,
) {

    val addressText: String
        get() = KeyValueJsonFns.getValues(task.taskAddress)

}