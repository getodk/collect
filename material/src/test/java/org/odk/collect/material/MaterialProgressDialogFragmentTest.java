package org.odk.collect.material;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowView.innerText;

import android.content.DialogInterface;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.fragmentstest.FragmentScenarioLauncherRule;

@RunWith(AndroidJUnit4.class)
public class MaterialProgressDialogFragmentTest {

    @Rule
    public FragmentScenarioLauncherRule launcherRule = new FragmentScenarioLauncherRule();

    @Test
    public void setTitle_updatesTitle() {
        FragmentScenario<MaterialProgressDialogFragment> scenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        scenario.onFragment(fragment -> {
            fragment.setTitle("blah");
            CharSequence message = shadowOf(fragment.getDialog()).getTitle();
            assertThat(message, equalTo("blah"));
        });
    }

    @Test
    public void recreate_persistsTitle() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.setTitle("blah");
        });

        fragmentScenario.recreate();
        fragmentScenario.onFragment(fragment -> {
            CharSequence title = shadowOf(fragment.getDialog()).getTitle();
            assertThat(title, equalTo("blah"));
        });
    }

    @Test
    public void whenMessageNotSet_showsProgressBar() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            View dialogView = fragment.getDialogView();
            assertThat(dialogView.findViewById(R.id.progress_bar).getVisibility(), Matchers.is(View.VISIBLE));
        });
    }

    @Test
    public void setMessage_updatesMessage() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            View dialogView = fragment.getDialogView();

            fragment.setMessage("blah");
            assertThat(innerText(dialogView), equalTo("blah"));
        });
    }

    @Test
    public void recreate_persistsMessage() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.setMessage("blah");
        });

        fragmentScenario.recreate();
        fragmentScenario.onFragment(fragment -> {
            CharSequence message = innerText(fragment.getDialogView());
            assertThat(message, equalTo("blah"));
        });
    }

    @Test
    public void setCancellableFalse_makesTheDialogNotCancellable() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.setCancelable(false);
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }

    @Test
    public void recreate_persistsCancellable() {
        FragmentScenario<MaterialProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(MaterialProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            fragment.setCancelable(false);
        });

        fragmentScenario.recreate();
        fragmentScenario.onFragment(fragment -> {
            assertThat(fragment.isCancelable(), equalTo(false));
        });
    }

    @Test
    public void cancelling_callsCancelOnCancellable() {
        FragmentScenario<TestProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(TestProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            MaterialProgressDialogFragment.OnCancelCallback onCancelCallback = mock(MaterialProgressDialogFragment.OnCancelCallback.class);
            fragment.setCancellableCallback(onCancelCallback);

            fragment.onCancel(fragment.getDialog());
            verify(onCancelCallback).cancel();
        });
    }

    @Test
    public void whenThereIsCancelButtonText_clickingCancel_dismissesAndCallsCancelOnCancellable() {
        FragmentScenario<TestProgressDialogFragment> fragmentScenario = launcherRule.launchDialogFragment(TestProgressDialogFragment.class);

        fragmentScenario.onFragment(fragment -> {
            MaterialProgressDialogFragment.OnCancelCallback onCancelCallback = mock(MaterialProgressDialogFragment.OnCancelCallback.class);
            fragment.setCancellableCallback(onCancelCallback);

            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            shadowOf(getMainLooper()).idle();

            verify(onCancelCallback).cancel();
            assertThat(dialog.isShowing(), equalTo(false));
        });
    }

    public static class TestProgressDialogFragment extends MaterialProgressDialogFragment {

        private OnCancelCallback onCancelCallback;

        @Override
        protected String getCancelButtonText() {
            return "Blah";
        }

        @Override
        protected OnCancelCallback getOnCancelCallback() {
            return onCancelCallback;
        }

        public void setCancellableCallback(OnCancelCallback onCancelCallback) {
            this.onCancelCallback = onCancelCallback;
        }
    }
}
