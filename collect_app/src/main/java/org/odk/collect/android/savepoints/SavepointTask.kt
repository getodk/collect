package org.odk.collect.android.savepoints

import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.async.Scheduler
import org.odk.collect.async.SchedulerAsyncTaskMimic
import org.odk.collect.forms.savepoints.Savepoint
import org.odk.collect.forms.savepoints.SavepointsRepository
import org.odk.collect.shared.files.FileUtils
import timber.log.Timber
import java.io.File

class SavepointTask(
    private var listener: SavepointListener?,
    private val formController: FormController,
    private val formDbId: Long,
    private val instanceDbId: Long?,
    private val cacheDir: String,
    private val savepointsRepository: SavepointsRepository,
    val scheduler: Scheduler
) : SchedulerAsyncTaskMimic<Unit, Unit, String?>(scheduler) {
    private var priority: Int = ++lastPriorityUsed

    override fun onPreExecute() = Unit

    override fun doInBackground(vararg params: Unit): String? {
        if (priority < lastPriorityUsed) {
            return null
        }

        return try {
            val savepointFile = File(cacheDir, "${formController.getInstanceFile()!!.name}.save")
            val savepoint = Savepoint(formDbId, instanceDbId, savepointFile.absolutePath, formController.getInstanceFile()!!.absolutePath)

            if (priority == lastPriorityUsed) {
                FileUtils.saveToFile(formController.getFilledInFormXml().payloadStream, savepointFile.absolutePath)
                savepointsRepository.save(savepoint)
            }

            null
        } catch (e: Exception) {
            Timber.e(e.message)
            e.message
        }
    }

    override fun onPostExecute(result: String?) {
        if (result != null) {
            listener?.onSavePointError(result)
            listener = null
        }
    }

    override fun onProgressUpdate(vararg values: Unit) = Unit

    override fun onCancelled() = Unit

    companion object {
        private var lastPriorityUsed: Int = 0
    }
}

interface SavepointListener {
    fun onSavePointError(errorMessage: String?)
}
