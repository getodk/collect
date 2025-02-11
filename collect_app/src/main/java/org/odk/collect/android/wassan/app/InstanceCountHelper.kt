package org.odk.collect.android.wassan.app

import org.odk.collect.android.database.instances.DatabaseInstancesRepository
import org.odk.collect.android.utilities.InstancesRepositoryProvider

object InstanceCountHelper {

    /**
     * Fetches the count of instances based on the query.
     */
    fun getInstanceCount(
        instancesRepositoryProvider: InstancesRepositoryProvider,
        projectId: String,
        selection: String,
        selectionArgs: Array<String>
    ): Int {
        val instancesRepository = instancesRepositoryProvider.create(projectId)

        if (instancesRepository is DatabaseInstancesRepository) {
            val cursor = instancesRepository.rawQuery(
                arrayOf("COUNT(*)"),  // Select only the count
                selection,
                selectionArgs,
                null, // No sorting
                null  // No grouping
            )

            cursor.use {
                return if (cursor.moveToFirst()) cursor.getInt(0) else 0 // Extract the count
            }
        }
        return 0
    }
}
