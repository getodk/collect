package org.odk.collect.android.formmanagement;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import org.junit.Before;
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
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class BlankFormListMenuDelegateTest {

    private FragmentActivity activity;

    @Before
    public void setup() {
        activity = Robolectric.setupActivity(FragmentActivity.class);
    }

    @Test
    public void onPrepareOptionsMenu_whenNotOutOfSync_showsSyncIcon() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(false));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(false));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel);

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(shadowOf(menu.findItem(R.id.menu_refresh).getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_refresh_24));
    }

    @Test
    public void onPrepareOptionsMenu_whenOutOfSync_showsErrorSyncIcon() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(false));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(true));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel);

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(shadowOf(menu.findItem(R.id.menu_refresh).getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_refresh_error_24));
    }

    @Test
    public void onPrepareOptionsMenu_whenSyncing_disablesRefreshButton() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(true));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(false));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel);

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