package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChangesReasonPromptDialogFragmentTest {

    private FragmentManager fragmentManager;
    private FormEntryViewModel viewModel;
    private ViewModelProvider.Factory testFactory;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();

        viewModel = mock(FormEntryViewModel.class);
        when(viewModel.requiresReasonToContinue()).thenReturn(new MutableLiveData<>(true));
        testFactory = new TestFactory(viewModel);
    }

    @Test
    public void onBackPressed_and_onCloseClicked_callPromptDismissed() {
        ChangesReasonPromptDialogFragment dialog = ChangesReasonPromptDialogFragment.create("Best Form");
        dialog.viewModelFactory = testFactory;
        dialog.show(fragmentManager, "TAG");

        dialog.onBackPressed();
        verify(viewModel, times(1)).promptDismissed();

        dialog.onCloseClicked();
        verify(viewModel, times(2)).promptDismissed();
    }

    private static class TestFactory implements ViewModelProvider.Factory {

        private final FormEntryViewModel viewModel;

        TestFactory(FormEntryViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) viewModel;
        }
    }
}