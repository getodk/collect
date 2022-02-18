package org.odk.collect.android.formentry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;

@RunWith(AndroidJUnit4.class)
public class FormLoadingDialogFragmentTest {

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Test
    public void dialogIsNotCancellable() {
        FragmentScenario<FormLoadingDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(FormLoadingDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }
}
