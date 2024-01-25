package org.odk.collect.android.formentry.savepoint

import org.odk.collect.android.javarosawrapper.FormController
import org.odk.collect.android.tasks.SaveFormToDisk
import org.odk.collect.async.Scheduler
import org.odk.collect.async.SchedulerAsyncTaskMimic
import timber.log.Timber

interface SavePointListener {
    fun onSavePointError(errorMessage: String?)
}

class SavePointTask(
    private var listener: SavePointListener?,
    private val formController: FormController,
    val scheduler: Scheduler
) : SchedulerAsyncTaskMimic<Unit, Unit, String?>(scheduler) {
    private var priority: Int = ++lastPriorityUsed

    override fun onPreExecute() = Unit

    override fun doInBackground(vararg params: Unit): String? {
        if (priority < lastPriorityUsed) {
            return null
        }

        return try {
            val payload = formController.getFilledInFormXml()
            val savepoint = SavePointManager.getSavepointFile(formController.getInstanceFile()!!.name)

            if (priority == lastPriorityUsed) {
                SaveFormToDisk.writeFile(payload, savepoint.absolutePath)
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
