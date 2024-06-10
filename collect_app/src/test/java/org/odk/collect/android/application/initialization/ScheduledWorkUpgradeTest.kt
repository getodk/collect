package org.odk.collect.android.application.initialization

import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.backgroundwork.FormUpdateScheduler
import org.odk.collect.android.backgroundwork.InstanceSubmitScheduler
import org.odk.collect.async.Scheduler
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project

class ScheduledWorkUpgradeTest {

    @Test
    fun `cancels all existing background jobs`() {
        val scheduler = mock<Scheduler>()
        val scheduledWorkUpgrade = ScheduledWorkUpgrade(
            scheduler,
            InMemProjectsRepository(),
            mock(),
            mock()
        )

        scheduledWorkUpgrade.run()
        verify(scheduler).cancelAllDeferred()
    }

    @Test
    fun `schedules updates for all projects`() {
        val projectsRepository = InMemProjectsRepository()
        val project1 = projectsRepository.save(Project.New("1", "1", "#ffffff"))
        val project2 = projectsRepository.save(Project.New("2", "2", "#ffffff"))

        val formUpdateScheduler = mock<FormUpdateScheduler>()
        val scheduledWorkUpgrade = ScheduledWorkUpgrade(
            mock(),
            projectsRepository,
            formUpdateScheduler,
            mock()
        )

        scheduledWorkUpgrade.run()
        verify(formUpdateScheduler).scheduleUpdates(project1.uuid)
        verify(formUpdateScheduler).scheduleUpdates(project2.uuid)
    }

    @Test
    fun `schedules submits for all projects`() {
        val projectsRepository = InMemProjectsRepository()
        val project1 = projectsRepository.save(Project.New("1", "1", "#ffffff"))
        val project2 = projectsRepository.save(Project.New("2", "2", "#ffffff"))

        val instanceSubmitScheduler = mock<InstanceSubmitScheduler>()
        val scheduledWorkUpgrade = ScheduledWorkUpgrade(
            mock(),
            projectsRepository,
            mock(),
            instanceSubmitScheduler
        )

        scheduledWorkUpgrade.run()
        verify(instanceSubmitScheduler).scheduleAutoSend(project1.uuid)
        verify(instanceSubmitScheduler).scheduleFormAutoSend(project1.uuid)
        verify(instanceSubmitScheduler).scheduleAutoSend(project2.uuid)
        verify(instanceSubmitScheduler).scheduleFormAutoSend(project2.uuid)
    }
}
