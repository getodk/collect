package org.odk.collect.android.backgroundwork

import android.content.Context
import androidx.work.BackoffPolicy
import org.odk.collect.analytics.Analytics
import org.odk.collect.android.formmanagement.FormsDataService
import org.odk.collect.android.injection.DaggerUtils
import org.odk.collect.android.injection.config.ProjectDependencyModuleFactory
import org.odk.collect.android.notifications.Notifier
import org.odk.collect.async.TaskSpec
import org.odk.collect.async.TaskSpec.Result
import java.util.function.Supplier
import javax.inject.Inject

class SyncFormsTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var notifier: Notifier

    override val maxRetries = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Result> {
        DaggerUtils.getComponent(context).inject(this)

        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                val result = formsDataService.matchFormsWithServer(
                    projectId,
                    isLastUniqueExecution,
                    isStopped
                )
                if (result) {
                    Result.Success()
                } else {
                    Result.Failure()
                }
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }
}

class SyncFormsDownloadTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var notifier: Notifier

    override val maxRetries = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Result> {
        DaggerUtils.getComponent(context).inject(this)

        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]
            if (projectId != null) {
                val formDownloads =
                    formsDataService.downloadFormList(projectId, isStopped) ?: emptyList()
                val serializedFormDownloads = FormDownloadSerializer.serialize(formDownloads)
                val outputData = mapOf(
                    TaskData.DATA_PROJECT_ID to projectId,
                    SyncFormsProcessTaskSpec.DATA_FORM_DOWNLOADS to serializedFormDownloads
                )

                Result.Success(outputData)
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }
}

class SyncFormsProcessTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var notifier: Notifier

    @Inject
    lateinit var projectDependencyModuleFactory: ProjectDependencyModuleFactory

    override val maxRetries = 3
    override val backoffPolicy = BackoffPolicy.EXPONENTIAL
    override val backoffDelay: Long = 60_000

    override fun getTask(
        context: Context,
        inputData: Map<String, String>,
        isLastUniqueExecution: Boolean,
        isStopped: (() -> Boolean)
    ): Supplier<Result> {
        DaggerUtils.getComponent(context).inject(this)

        return Supplier {
            val projectId = inputData[TaskData.DATA_PROJECT_ID]

            if (projectId != null) {
                val formsRepository =
                    projectDependencyModuleFactory.create(projectId).formsRepository
                val formDownloads = inputData[DATA_FORM_DOWNLOADS]?.let {
                    FormDownloadSerializer.deserialize(it, formsRepository)
                }

                if (formDownloads != null) {
                    formsDataService.processFormDownloads(projectId, formDownloads)
                    Result.Success()
                } else {
                    throw IllegalArgumentException("No form downloads provided!")
                }
            } else {
                throw IllegalArgumentException("No project ID provided!")
            }
        }
    }

    override fun onException(exception: Throwable) {
        Analytics.logNonFatal(exception)
    }

    companion object {
        const val DATA_FORM_DOWNLOADS = "formDownloads"
    }
}
