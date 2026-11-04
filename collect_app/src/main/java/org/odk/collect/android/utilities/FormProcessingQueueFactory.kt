package org.odk.collect.android.utilities

import org.odk.collect.android.formmanagement.FormDownload
import org.odk.collect.async.WorkQueue
import org.odk.collect.projects.ProjectDependencyFactory
import org.odk.collect.shared.files.FileExt.deleteDirectory
import javax.inject.Singleton

@Singleton
class FormProcessingQueueFactory : ProjectDependencyFactory<WorkQueue<FormDownload>> {

    private val queues = mutableMapOf<String, WorkQueue<FormDownload>>()

    override fun create(projectId: String): WorkQueue<FormDownload> {
        return synchronized(this) {
            queues.getOrPut(projectId) {
                WorkQueue(
                    afterConsume = { formDownloads ->
                        formDownloads.forEach { it.tempDir.deleteDirectory() }
                    }
                )
            }
        }
    }
}
