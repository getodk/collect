package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChangesReasonPromptDialogFragmentTest {

    private FragmentManager fragmentManager;
    private ChangesReasonPromptViewModel viewModel;
    private ViewModelProvider.Factory testFactory;

    @Before
    public void setup() {
        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();

        viewModel = mock(ChangesReasonPromptViewModel.class);
        when(viewModel.requiresReasonToContinue()).thenReturn(new MutableLiveData<>(true));
        testFactory = new TestFactory(viewModel);
    }

    @Test
    public void show_onlyEverOpensOneDialog() {
        ChangesReasonPromptDialogFragment dialog1 = createFragment();
        dialog1.show("Best Form", fragmentManager);

        ChangesReasonPromptDialogFragment dialog2 = createFragment();
        dialog2.show("Best Form", fragmentManager);

        assertThat(fragmentManager.getFragments().size(), equalTo(1));
    }

    @Test
    public void onBackPressed_and_onCloseClicked_callPromptDismissed() {
        ChangesReasonPromptDialogFragment dialog = createFragment();
        dialog.show("Best Form", fragmentManager);

        dialog.onBackPressed();
        verify(viewModel, times(1)).promptDismissed();

        dialog.onCloseClicked();
        verify(viewModel, times(2)).promptDismissed();
    }

    @NotNull
    private ChangesReasonPromptDialogFragment createFragment() {
        ChangesReasonPromptDialogFragment dialog = new ChangesReasonPromptDialogFragment();
        dialog.viewModelFactory = testFactory;
        return dialog;
    }

    private static class TestFactory implements ViewModelProvider.Factory {

        private final ChangesReasonPromptViewModel viewModel;

        TestFactory(ChangesReasonPromptViewModel viewModel) {
            this.viewModel = viewModel;
        }

        @NonNull
        @Override
        public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
            return (T) viewModel;
        }
    }
}