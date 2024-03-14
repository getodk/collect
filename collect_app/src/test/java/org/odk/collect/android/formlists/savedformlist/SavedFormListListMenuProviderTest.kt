package org.odk.collect.android.formlists.savedformlist

import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.CoreMatchers.instanceOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.odk.collect.android.R
import org.odk.collect.android.formlists.sorting.FormListSortingBottomSheetDialog
import org.odk.collect.android.formlists.sorting.FormListSortingOption
import org.odk.collect.android.support.CollectHelpers
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog

@RunWith(AndroidJUnit4::class)
class SavedFormListListMenuProviderTest {

    private val activity: FragmentActivity =
        CollectHelpers.createThemedActivity(FragmentActivity::class.java)

    private val viewModel: SavedFormListViewModel = mock()
    private val menuInflater: SupportMenuInflater
        get() = SupportMenuInflater(activity)

    @Test
    fun `changing search text should set filterText in viewModel`() {
        val menu = MenuBuilder(activity)
        val menuProvider = SavedFormListListMenuProvider(activity, viewModel)

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        val searchView =
            (menu.findItem(R.id.menu_filter).actionView as SearchView).findViewById<SearchView.SearchAutoComplete>(
                androidx.appcompat.R.id.search_src_text
            )
        searchView.setText("abc")
        verify(viewModel).filterText = "abc"
    }

    @Test
    fun `clicking search hides sort and hiding search shows it again`() {
        val menu = MenuBuilder(activity)
        val menuProvider = SavedFormListListMenuProvider(activity, viewModel)

        menuProvider.onCreateMenu(menu, menuInflater)
        menuProvider.onPrepareMenu(menu)

        menu.findItem(R.id.menu_filter).expandActionView()
        assertThat(menu.findItem(R.id.menu_sort).isVisible, equalTo(false))

        menu.findItem(R.id.menu_filter).collapseActionView()
        assertThat(menu.findItem(R.id.menu_sort).isVisible, equalTo(true))
    }

    @Test
    fun `clicking sort displays sorting dialog`() {
        whenever(viewModel.sortOrder).doReturn(SavedFormListViewModel.SortOrder.DATE_DESC)

        val menuProvider = SavedFormListListMenuProvider(activity, viewModel)
        menuProvider.onMenuItemSelected(RoboMenuItem(R.id.menu_sort))

        val dialog = ShadowDialog.getLatestDialog()
        assertThat(dialog, instanceOf(FormListSortingBottomSheetDialog::class.java))

        val formListSortingBottomSheetDialog = dialog as FormListSortingBottomSheetDialog
        assertThat(dialog.selectedOption, equalTo(2))
        assertThat(
            formListSortingBottomSheetDialog.options,
            contains(
                FormListSortingOption(
                    R.drawable.ic_sort_by_alpha,
                    org.odk.collect.strings.R.string.sort_by_name_asc
                ),
                FormListSortingOption(
                    R.drawable.ic_sort_by_alpha,
                    org.odk.collect.strings.R.string.sort_by_name_desc
                ),
                FormListSortingOption(
                    R.drawable.ic_access_time,
                    org.odk.collect.strings.R.string.sort_by_date_desc
                ),
                FormListSortingOption(
                    R.drawable.ic_access_time,
                    org.odk.collect.strings.R.string.sort_by_date_asc
                )
            )
        )

        formListSortingBottomSheetDialog.onSelectedOptionChanged.accept(3)
        verify(viewModel).sortOrder = SavedFormListViewModel.SortOrder.DATE_ASC
    }
}
