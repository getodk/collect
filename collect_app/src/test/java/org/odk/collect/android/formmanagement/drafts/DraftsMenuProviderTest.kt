package org.odk.collect.android.formmanagement.drafts

import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectHelpers

@RunWith(AndroidJUnit4::class)
class DraftsMenuProviderTest {

    private val activity = CollectHelpers.createThemedActivity(FragmentActivity::class.java)
    private val menuInflater = SupportMenuInflater(activity)
    private val menu = MenuBuilder(activity)
    private val draftsMenuProvider = DraftsMenuProvider(activity, mock()).also {
        it.onCreateMenu(menu, menuInflater)
    }

    @Test
    fun whenDraftCountHasNotLoaded_doesNotShowFinalizeAll() {
        draftsMenuProvider.draftsCount = null
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(false))
    }

    @Test
    fun whenDraftCountIsZero_doesNotShowFinalizeAll() {
        draftsMenuProvider.draftsCount = 0
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(false))
    }

    @Test
    fun whenDraftsCountIsNonZero_showsFinalizeAll() {
        draftsMenuProvider.draftsCount = 1
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(true))
    }
}
