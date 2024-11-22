package org.odk.collect.android.activities

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.odk.collect.android.formhierarchy.FormHierarchyFragmentHostActivity
import org.odk.collect.android.support.CollectHelpers

@RunWith(AndroidJUnit4::class)
class FormHierarchyFragmentHostActivityTest {

    @Before
    fun setup() {
        CollectHelpers.setupDemoProject()
    }

    /**
     * This can happen if the app process is restored after being kicked from memory.
     */
    @Test
    fun whenFormHasNotLoadedYet_finishes() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val intent = Intent(context, FormHierarchyFragmentHostActivity::class.java).also {
            it.putExtra(FormHierarchyFragmentHostActivity.EXTRA_SESSION_ID, "blah")
        }

        ActivityScenario.launch<FormHierarchyFragmentHostActivity>(intent).use { scenario ->
            assertThat(scenario.state, equalTo(Lifecycle.State.DESTROYED))
        }
    }
}
