package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.fragmentstest.DialogFragmentTest;

@RunWith(AndroidJUnit4.class)
public class FormLoadingDialogFragmentTest {

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<FormLoadingDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(FormLoadingDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
