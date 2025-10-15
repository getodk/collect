package org.odk.collect.android.formlists.blankformlist

import android.view.Menu
import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.core.internal.view.SupportMenu
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.MutableLiveData
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formlists.sorting.FormListSortingBottomSheetDialog
import org.odk.collect.android.support.CollectHelpers
import org.odk.collect.async.network.NetworkStateProvider
import org.robolectric.Shadows
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog
import org.robolectric.shadows.ShadowToast

@RunWith(AndroidJUnit4::class)
class BlankFormListMenuProviderTest {
    private lateinit var activity: FragmentActivity

    private val viewModel: BlankFormListViewModel = mock {
        on { sortingOrder } doReturn BlankFormListViewModel.SortOrder.NAME_ASC
    }

    private val networkStateProvider: NetworkStateProvider = mock()

    private val menuInflater: SupportMenuInflater
        get() = SupportMenuInflater(activity)

    @Before
    fun setup() {
        whenever(networkStateProvider.isDeviceOnline).thenReturn(true)
        whenever(viewModel.isLoading).thenReturn(MutableLiveData(false))
        whenever(viewModel.isOutOfSyncWithServer()).thenReturn(MutableLiveData(false))

        activity = CollectHelpers.createThemedActivity(FragmentActivity::class.java)
    }

    @Test
    fun `onPrepareMenu when not out of sync shows sync icon`() {
        val menuProvider = createMenuProvider()
        val menu = createdMenu()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        assertThat(
            Shadows.shadowOf(menu.findItem(R.id.menu_refresh).icon).createdFromResId,
            `is`(R.drawable.ic_baseline_refresh_24)
        )
    }

    @Test
    fun `onPrepareMenu when out of sync shows error sync icon`() {
        whenever(viewModel.isOutOfSyncWithServer()).thenReturn(MutableLiveData(true))

        val menuProvider = createMenuProvider()
        val menu = createdMenu()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        assertThat(
            Shadows.shadowOf(menu.findItem(R.id.menu_refresh).icon).createdFromResId,
            `is`(R.drawable.ic_baseline_refresh_error_24)
        )
    }

    @Test
    fun `onPrepareMenu when loading disables refresh button`() {
        whenever(viewModel.isLoading).thenReturn(MutableLiveData(true))

        val menuProvider = createMenuProvider()
        val menu = createdMenu()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_refresh).isEnabled, `is`(false))
    }

    @Test
    fun `onPrepareMenu when not loading enables refresh button`() {
        val menuProvider = createMenuProvider()
        val menu = createdMenu()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        assertThat(menu.findItem(R.id.menu_refresh).isEnabled, `is`(true))
    }

    @Test
    fun `clicking refresh for sync when device is offline shows error toast and does not sync`() {
        whenever(networkStateProvider.isDeviceOnline).thenReturn(false)

        val menuProvider = createMenuProvider()
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_refresh))

        assertThat(
            ShadowToast.getTextOfLatestToast(),
            `is`(activity.getString(org.odk.collect.strings.R.string.no_connection))
        )
        verify(viewModel, never()).syncWithServer()
    }

    @Test
    fun `clicking sort displays sorting dialog`() {
        val menuProvider = createMenuProvider()
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_sort))

        assertThat(ShadowDialog.getLatestDialog(), instanceOf(FormListSortingBottomSheetDialog::class.java))
    }

    @Test
    fun `changing search text should set filterText in viewModel`() {
        val menu = createdMenu()
        val menuProvider = createMenuProvider()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        val searchView = (menu.findItem(R.id.menu_filter).actionView as SearchView).findViewById<SearchView.SearchAutoComplete>(androidx.appcompat.R.id.search_src_text)
        searchView.setText("abc")

        verify(viewModel).filterText = "abc"
    }

    @Test
    fun `clicking search when matchExactly enabled hides refresh and sort and then collapsing search shows them again`() {
        whenever(viewModel.isMatchExactlyEnabled()).thenReturn(true)

        val menu = createdMenu()
        val menuProvider = createMenuProvider()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        menu.findItem(R.id.menu_filter).expandActionView()

        assertThat(menu.findItem(R.id.menu_refresh).isVisible, `is`(false))
        assertThat(menu.findItem(R.id.menu_sort).isVisible, `is`(false))

        menu.findItem(R.id.menu_filter).collapseActionView()

        assertThat(menu.findItem(R.id.menu_refresh).isVisible, `is`(true))
        assertThat(menu.findItem(R.id.menu_sort).isVisible, `is`(true))
    }

    @Test
    fun `collapsing search when matchExactly not enabled does not show refresh`() {
        whenever(viewModel.isMatchExactlyEnabled()).thenReturn(false)

        val menu = createdMenu()
        val menuProvider = createMenuProvider()

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        menu.findItem(R.id.menu_filter).expandActionView()
        menu.findItem(R.id.menu_filter).collapseActionView()

        assertThat(menu.findItem(R.id.menu_refresh).isVisible, `is`(false))
    }

    /**
     * SearchView is closed automatically after screen recreation. We think that filtering is not
     * crucial enough to restore its state, so we do not reopen the view, and we clear filterText to
     * display all available forms.
     */
    @Test
    fun `filterText should be cleared when menu is created`() {
        whenever(viewModel.isMatchExactlyEnabled()).thenReturn(false)

        val menu = createdMenu()
        val menuProvider = createMenuProvider()

        menuProvider.onCreateMenu(menu, menuInflater)

        verify(viewModel).filterText = ""
    }

    private fun createMenuProvider(): BlankFormListMenuProvider {
        return BlankFormListMenuProvider(activity, viewModel, networkStateProvider)
    }

    private fun createdMenu(): Menu {
        val menu: SupportMenu = MenuBuilder(activity)
        menuInflater.inflate(R.menu.blank_form_list_menu, menu)
        return menu
    }
}
