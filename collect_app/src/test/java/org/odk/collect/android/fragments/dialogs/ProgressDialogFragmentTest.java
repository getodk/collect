package org.odk.collect.android.fragments.dialogs;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.fragmentstest.DialogFragmentTest;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static org.robolectric.shadows.ShadowView.innerText;

@RunWith(AndroidJUnit4.class)
public class ProgressDialogFragmentTest {

    @Test
    public void setTitle_updatesTitle() {
        FragmentScenario<ProgressDialogFragment> scenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class);
        scenario.onFragment(fragment -> {
            fragment.setTitle("blah");
            CharSequence message = shadowOf(fragment.getDialog()).getTitle();
            assertThat(message, equalTo("blah"));
        });
    }

    @Test
    public void recreate_persistsTitle() {
        FragmentScenario<ProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class);
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
        FragmentScenario<ProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            View dialogView = fragment.getDialogView();
            assertThat(dialogView.findViewById(R.id.progress_bar).getVisibility(), is(View.VISIBLE));
        });
    }

    @Test
    public void setMessage_updatesMessage() {
        FragmentScenario<ProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            View dialogView = fragment.getDialogView();

            fragment.setMessage("blah");
            assertThat(innerText(dialogView), equalTo("blah"));
        });
    }

    @Test
    public void recreate_persistsMessage() {
        FragmentScenario<ProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class);
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
        Bundle args = new Bundle();
        args.putBoolean(ProgressDialogFragment.CANCELABLE, false);

        FragmentScenario<ProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(ProgressDialogFragment.class, args);
        fragmentScenario.onFragment(fragment -> {
            assertThat(shadowOf(fragment.getDialog()).isCancelable(), equalTo(false));
        });
    }

    @Test
    public void cancelling_callsCancelOnCancellable() {
        FragmentScenario<TestProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(TestProgressDialogFragment.class);
        fragmentScenario.onFragment(fragment -> {
            ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
            fragment.setCancellableCallback(cancellable);

            fragment.onCancel(fragment.getDialog());
            verify(cancellable).cancel();
        });
    }

    @Test
    public void whenThereIsCancelButtonText_clickingCancel_dismissesAndCallsCancelOnCancellable() {
        FragmentScenario<TestProgressDialogFragment> fragmentScenario = DialogFragmentTest.launchDialogFragment(TestProgressDialogFragment.class);

        fragmentScenario.onFragment(fragment -> {
            ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
            fragment.setCancellableCallback(cancellable);

            AlertDialog dialog = (AlertDialog) fragment.getDialog();
            dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
            shadowOf(getMainLooper()).idle();

            verify(cancellable).cancel();
            assertThat(dialog.isShowing(), equalTo(false));
        });
    }

    public static class TestProgressDialogFragment extends ProgressDialogFragment {

        private Cancellable cancellable;

        @Override
        protected String getCancelButtonText() {
            return "Blah";
        }

        @Override
        protected Cancellable getCancellable() {
            return cancellable;
        }

        public void setCancellableCallback(Cancellable cancellable) {
            this.cancellable = cancellable;
        }
    }
}
