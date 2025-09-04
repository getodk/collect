package org.odk.collect.android.notifications

import android.app.Application
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.TestSettingsProvider
import org.odk.collect.android.formlists.blankformlist.BlankFormListActivity
import org.odk.collect.android.formmanagement.ServerFormDetails
import org.odk.collect.forms.FormSourceException
import org.odk.collect.forms.ManifestFile
import org.odk.collect.projects.InMemProjectsRepository
import org.odk.collect.projects.Project
import org.odk.collect.projects.ProjectsRepository
import org.odk.collect.strings.localization.getLocalizedString
import org.robolectric.Shadows

@RunWith(AndroidJUnit4::class)
class NotificationManagerNotifierTest {
    private lateinit var notifier: NotificationManagerNotifier
    private lateinit var notificationManager: NotificationManager
    private val projectsRepository: ProjectsRepository = InMemProjectsRepository().apply {
        save(Project.DEMO_PROJECT)
    }

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifier = NotificationManagerNotifier(
            context,
            TestSettingsProvider.getSettingsProvider(),
            projectsRepository
        )
    }

    @Test
    fun onSync_whenExceptionNull_clearsNotification() {
        notifier.onSync(FormSourceException.FetchError(), Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
        notifier.onSync(null, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(0)
        )
    }

    @Test
    fun onSyncStopped_sendsNotification() {
        notifier.onSyncStopped(Project.DEMO_PROJECT_ID)

        val context = ApplicationProvider.getApplicationContext<Application>()

        val notifications = Shadows.shadowOf(notificationManager).allNotifications
        val notification = notifications[0]
        val intent = Shadows.shadowOf(notification.contentIntent).savedIntent

        assertThat(notifications.size, equalTo(1))
        assertThat(
            notification.extras.getString(Notification.EXTRA_TITLE),
            equalTo(context.getLocalizedString(org.odk.collect.strings.R.string.form_update_error))
        )
        assertThat(
            notification.extras.getString(Notification.EXTRA_TEXT),
            equalTo(context.getLocalizedString(org.odk.collect.strings.R.string.form_update_error_massage))
        )
        assertThat(
            notification.extras.getString(Notification.EXTRA_SUB_TEXT),
            equalTo(Project.DEMO_PROJECT_NAME)
        )
        assertThat(
            intent.component?.className,
            equalTo(BlankFormListActivity::class.java.name)
        )
    }

    @Test
    fun onUpdatesAvailable_whenUpdatesHaveBeenSeenBefore_doesNotNotifyASecondTime() {
        val updates = listOf(
            ServerFormDetails(
                "form-1",
                "http://example.com/form-1",
                "form-1",
                "server",
                "form-1-hash",
                false,
                true,
                null
            )
        )
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
        notificationManager.cancelAll()
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(0)
        )
    }

    @Test
    fun onUpdatesAvailable_whenFormHasNewHash_notifies() {
        var updates = listOf(
            ServerFormDetails(
                "form-1",
                "http://example.com/form-1",
                "form-1",
                "server",
                "form-1-hash",
                false,
                true,
                null
            )
        )
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
        updates = listOf(
            ServerFormDetails(
                "form-1",
                "http://example.com/form-1",
                "form-1",
                "server",
                "form-1-hash-changed",
                false,
                true,
                null
            )
        )
        notificationManager.cancelAll()
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
    }

    @Test
    fun onUpdatesAvailable_whenFormHasNewManifestHash_notifies() {
        var updates = listOf(
            ServerFormDetails(
                "form-1",
                "http://example.com/form-1",
                "form-1",
                "server",
                "form-1-hash",
                false,
                true,
                ManifestFile("manifest-hash", emptyList())
            )
        )
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
        updates = listOf(
            ServerFormDetails(
                "form-1",
                "http://example.com/form-1",
                "form-1",
                "server",
                "form-1-hash",
                false,
                true,
                ManifestFile("manifest-hash-changed", emptyList())
            )
        )
        notificationManager.cancelAll()
        notifier.onUpdatesAvailable(updates, Project.DEMO_PROJECT_ID)
        assertThat(
            Shadows.shadowOf(notificationManager).allNotifications.size,
            equalTo(1)
        )
    }
}
