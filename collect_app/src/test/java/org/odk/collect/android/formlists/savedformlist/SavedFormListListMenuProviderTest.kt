package org.odk.collect.android.formlists.savedformlist

import androidx.appcompat.view.SupportMenuInflater
import androidx.appcompat.view.menu.MenuBuilder
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.FragmentActivity
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.odk.collect.android.R
import org.odk.collect.android.support.CollectHelpers

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
}
