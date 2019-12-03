package org.odk.collect.android.formentry.audit;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.RobolectricHelpers;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class ChangesReasonPromptDialogFragmentTest {

    @Test
    public void show_onlyEverOpensOneDialog() {
        ChangesReasonPromptViewModel viewModel = mock(ChangesReasonPromptViewModel.class);
        when(viewModel.requiresReasonToContinue()).thenReturn(new MutableLiveData<>(true));
        ChangesReasonPromptViewModel.Factory factory = new TestFactory(viewModel);

        FragmentActivity activity = RobolectricHelpers.createThemedActivity(FragmentActivity.class);
        FragmentManager fragmentManager = activity.getSupportFragmentManager();

        ChangesReasonPromptDialogFragment.show("Best Form", fragmentManager, factory);
        ChangesReasonPromptDialogFragment.show("Best Form", fragmentManager, factory);

        assertThat(fragmentManager.getFragments().size(), equalTo(1));
    }

    private static class TestFactory extends ChangesReasonPromptViewModel.Factory {

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