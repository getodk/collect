package org.odk.collect.android.formmanagement.drafts

import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.core.view.MenuProvider
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.equalTo
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectHelpers

@RunWith(AndroidJUnit4::class)
class DraftsMenuProviderTest {

    private val activity = CollectHelpers.createThemedActivity(MenuProviderTestActivity::class.java)
    private val menuInflater = SupportMenuInflater(activity)
    private val menu = MenuBuilder(activity)

    private val draftsCountLiveData: MutableLiveData<Int> = MutableLiveData(null)
    private val bulkFinalizationViewModel = mock<BulkFinalizationViewModel> {
        on { draftsCount } doReturn draftsCountLiveData
    }

    private val draftsMenuProvider = DraftsMenuProvider(activity, bulkFinalizationViewModel).also {
        it.onCreateMenu(menu, menuInflater)
    }

    @Test
    fun whenDraftCountHasNotLoaded_doesNotShowFinalizeAll() {
        draftsCountLiveData.value = null
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(false))
    }

    @Test
    fun whenDraftCountIsZero_doesNotShowFinalizeAll() {
        draftsCountLiveData.value = 0
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(false))
    }

    @Test
    fun whenDraftsCountIsNonZero_showsFinalizeAll() {
        draftsCountLiveData.value = 1
        draftsMenuProvider.onPrepareMenu(menu)
        assertThat(menu.findItem(R.id.bulk_finalize).isVisible, equalTo(true))
    }

    @Test
    fun whenDraftsCountUpdates_invalidatesMenu() {
        assertThat(activity.invalidateCount, equalTo(1))

        draftsCountLiveData.value = 11
        assertThat(activity.invalidateCount, equalTo(2))
    }
}

private class MenuProviderTestActivity : FragmentActivity() {

    var invalidateCount = 0
        private set

    override fun addMenuProvider(provider: MenuProvider) {
        super.addMenuProvider(provider)
        invalidateCount = 0 // Reset the count after Activity creation
    }

    override fun invalidateOptionsMenu() {
        super.invalidateOptionsMenu()
        invalidateCount++
    }
}
