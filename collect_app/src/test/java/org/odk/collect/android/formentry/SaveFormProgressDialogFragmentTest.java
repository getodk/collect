package org.odk.collect.android.formentry;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class SaveFormProgressDialogFragmentTest {
    private FragmentManager fragmentManager;

    @Before
    public void setup() {
        FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);

        // Fragment relies on Activity having set up ViewModel
        ViewModelProviders
                .of(activity, new FormSaveViewModel.Factory(null, null))
                .get(FormSaveViewModel.class);

        fragmentManager = activity.getSupportFragmentManager();
    }

    @Test
    public void dialogIsNotCancellable() {
        SaveFormProgressDialogFragment fragment = new SaveFormProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");

        assertThat(shadowOf(fragment.getDialog()).isCancelable(), equalTo(false));
    }
}
