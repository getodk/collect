package org.odk.collect.android.application.initialization

import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.async.Scheduler
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.upgrade.Upgrade

class FormUpdatesUpgrade(
    private val scheduler: Scheduler,
    private val projectsRepository: ProjectsRepository,
    private val formUpdateScheduler: FormUpdateScheduler
) : Upgrade {

    override fun key(): String? {
        return null
    }

    override fun run() {
        scheduler.cancelAllDeferred()

        projectsRepository.getAll().forEach {
            formUpdateScheduler.scheduleUpdates(it.uuid)
        }
    }
}
