package org.odk.collect.android.formmanagement

import android.app.Activity
import android.content.Intent.ACTION_EDIT
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.Matchers.not
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.activities.FormEntryActivity
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.projects.CurrentProjectProvider
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE
import org.odk.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT
import org.odk.collect.androidtest.RecordedIntentsRule
import org.odk.collect.forms.instances.Instance
import org.odk.collect.formstest.InMemInstancesRepository
import org.odk.collect.projects.Project
import org.odk.collect.settings.InMemSettingsProvider
import org.odk.collect.settings.keys.ProtectedProjectKeys.KEY_EDIT_SAVED
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class FormNavigatorTest {

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    private val activity = Robolectric.buildActivity(Activity::class.java).get()
    private val currentProjectProvider = mock<CurrentProjectProvider> {
        on { getCurrentProject() } doReturn Project.Saved("projectId", "", "", "")
    }

    private val settingsProvider = InMemSettingsProvider()
    private val instancesRepository = InMemInstancesRepository()

    private val navigator = FormNavigator(currentProjectProvider, settingsProvider) {
        instancesRepository
    }

    @Test
    fun `editInstance starts FormEntryActivity with instance URI`() {
        settingsProvider.getProtectedSettings().save(KEY_EDIT_SAVED, true)

        navigator.editInstance(activity, 101)

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormEntryActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", 101)))
        intended(not(hasExtra(FORM_MODE, VIEW_SENT)))
    }

    @Test
    fun `editInstance stars FormEntryActivity in view only mode when editing is disabled`() {
        settingsProvider.getProtectedSettings().save(KEY_EDIT_SAVED, false)

        navigator.editInstance(activity, 101)

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormEntryActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", 101)))
        intended(hasExtra(FORM_MODE, VIEW_SENT))
    }

    @Test
    fun `editInstance stars FormEntryActivity in view only mode when instance has been sent`() {
        settingsProvider.getProtectedSettings().save(KEY_EDIT_SAVED, true)
        val instance = instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_SUBMITTED)
                .build()
        )

        navigator.editInstance(activity, instance.dbId)

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormEntryActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", instance.dbId)))
        intended(hasExtra(FORM_MODE, VIEW_SENT))
    }

    @Test
    fun `editInstance stars FormEntryActivity in view only mode when instance has failed to send`() {
        settingsProvider.getProtectedSettings().save(KEY_EDIT_SAVED, true)
        val instance = instancesRepository.save(
            Instance.Builder()
                .status(Instance.STATUS_SUBMISSION_FAILED)
                .build()
        )

        navigator.editInstance(activity, instance.dbId)

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormEntryActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", instance.dbId)))
        intended(hasExtra(FORM_MODE, VIEW_SENT))
    }
}
