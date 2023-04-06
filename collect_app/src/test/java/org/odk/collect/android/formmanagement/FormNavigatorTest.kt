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
import org.odk.collect.android.external.FormUriActivity
import org.odk.collect.android.external.InstancesContract
import org.odk.collect.android.utilities.ApplicationConstants.BundleKeys.FORM_MODE
import org.odk.collect.android.utilities.ApplicationConstants.FormModes.VIEW_SENT
import org.odk.collect.androidtest.RecordedIntentsRule
import org.robolectric.Robolectric

@RunWith(AndroidJUnit4::class)
class FormNavigatorTest {

    @get:Rule
    val recordedIntentsRule = RecordedIntentsRule()

    private val activity = Robolectric.buildActivity(Activity::class.java).get()

    @Test
    fun `newInstance starts FormUriActivity with instance URI`() {
        activity.startActivity(FormNavigator.newInstanceIntent(activity, InstancesContract.getUri("projectId", 101)))

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormUriActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", 101)))
        intended(not(hasExtra(FORM_MODE, VIEW_SENT)))
    }

    @Test
    fun `editInstance starts FormUriActivity with instance URI`() {
        activity.startActivity(FormNavigator.editInstanceIntent(activity, "projectId", 101))

        intended(hasAction(ACTION_EDIT))
        intended(hasComponent(FormUriActivity::class.java.name))
        intended(hasData(InstancesContract.getUri("projectId", 101)))
        intended(not(hasExtra(FORM_MODE, VIEW_SENT)))
    }
}
