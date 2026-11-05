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
import org.odk.collect.async.clearAndBlock
import java.util.function.Supplier
import javax.inject.Inject

class SyncFormsTaskSpec : TaskSpec {
    @Inject
    lateinit var formsDataService: FormsDataService

    @Inject
    lateinit var notifier: Notifier

    @Inject
    lateinit var projectDependencyModuleFactory: ProjectDependencyModuleFactory

    override val maxRetries = null
    override val backoffPolicy = null
    override val backoffDelay = null

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
                val workQueue = projectDependencyModuleFactory.create(projectId).formProcessingQueue
                workQueue.clearAndBlock {
                    formsDataService.matchFormsWithServer(
                        projectId,
                        isLastUniqueExecution,
                        isStopped
                    )
                }

                Result.Success()
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
                val workQueue = projectDependencyModuleFactory.create(projectId).formProcessingQueue
                workQueue.set {
                    formsDataService.downloadFormList(projectId, isStopped) ?: emptyList()
                }

                Result.Success(mapOf(TaskData.DATA_PROJECT_ID to projectId))
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
                val workQueue = projectDependencyModuleFactory.create(projectId).formProcessingQueue
                workQueue.consume { formDownloads ->
                    formsDataService.processFormDownloads(projectId, formDownloads)
                    Result.Success()
                }

                Result.Success()
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
