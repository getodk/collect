package org.odk.collect.android.formmanagement;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenu;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class BlankFormListMenuDelegateTest {

    @Test
    public void onPrepareOptionsMenu_whenSyncing_disablesRefreshButton() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(viewModel);

        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(true));

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.menu_refresh).isEnabled(), is(false));
    }

    private RoboMenu createdMenu() {
        RoboMenu menu = new RoboMenu();
        Robolectric.setupActivity(FragmentActivity.class).getMenuInflater().inflate(R.menu.list_menu, menu);
        return menu;
    }
}