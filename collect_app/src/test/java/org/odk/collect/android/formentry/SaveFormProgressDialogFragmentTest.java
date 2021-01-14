package org.odk.collect.android.formentry;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.formentry.saving.SaveFormProgressDialogFragment;
import org.odk.collect.android.support.RobolectricHelpers;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class SaveFormProgressDialogFragmentTest {

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<SaveFormProgressDialogFragment> fragmentScenario = RobolectricHelpers.launchDialogFragment(SaveFormProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
