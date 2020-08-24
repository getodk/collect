package org.odk.collect.android.formmanagement;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.MutableLiveData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.network.NetworkStateProvider;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.fakes.RoboMenu;
import org.robolectric.fakes.RoboMenuItem;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class BlankFormListMenuDelegateTest {

    private FragmentActivity activity;
    private final BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
    private final NetworkStateProvider networkStateProvider = mock(NetworkStateProvider.class);

    @Before
    public void setup() {
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(false));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(false));
        when(networkStateProvider.isDeviceOnline()).thenReturn(true);

        activity = Robolectric.setupActivity(FragmentActivity.class);
    }

    @Test
    public void onPrepareOptionsMenu_whenNotOutOfSync_showsSyncIcon() {
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(false));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(false));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(shadowOf(menu.findItem(R.id.menu_refresh).getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_refresh_24));
    }

    @Test
    public void onPrepareOptionsMenu_whenOutOfSync_showsErrorSyncIcon() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(false));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(true));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(shadowOf(menu.findItem(R.id.menu_refresh).getIcon()).getCreatedFromResId(), is(R.drawable.ic_baseline_refresh_error_24));
    }

    @Test
    public void onPrepareOptionsMenu_whenSyncing_disablesRefreshButton() {
        BlankFormsListViewModel viewModel = mock(BlankFormsListViewModel.class);
        when(viewModel.isSyncing()).thenReturn(new MutableLiveData<>(true));
        when(viewModel.isOutOfSync()).thenReturn(new MutableLiveData<>(false));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);

        RoboMenu menu = createdMenu();
        menuDelegate.onPrepareOptionsMenu(menu);
        assertThat(menu.findItem(R.id.menu_refresh).isEnabled(), is(false));
    }

    @Test
    public void onOptionsSelected_forSync_showsSuccessToast() {
        when(viewModel.syncWithServer()).thenReturn(new MutableLiveData<>(true));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_refresh));

        assertThat(ShadowToast.getTextOfLatestToast(), is(activity.getString(R.string.form_update_complete)));
    }

    @Test
    public void onOptionsItemSelected_forSync_whenDeviceIsOffline_showsErrorToastAndDoesNotSync() {
        when(networkStateProvider.isDeviceOnline()).thenReturn(false);
        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_refresh));

        assertThat(ShadowToast.getTextOfLatestToast(), is(activity.getString(R.string.no_connection)));
        verify(viewModel, never()).syncWithServer();
    }

    @Test
    public void onOptionsSelected_forSync_whenSyncingFails_doesNotShowToast() {
        when(viewModel.syncWithServer()).thenReturn(new MutableLiveData<>(false));

        BlankFormListMenuDelegate menuDelegate = new BlankFormListMenuDelegate(activity, viewModel, networkStateProvider);
        menuDelegate.onOptionsItemSelected(new RoboMenuItem(R.id.menu_refresh));

        assertThat(ShadowToast.getLatestToast(), nullValue());
    }

    private RoboMenu createdMenu() {
        RoboMenu menu = new RoboMenu();
        Robolectric.setupActivity(FragmentActivity.class).getMenuInflater().inflate(R.menu.list_menu, menu);
        return menu;
    }
}