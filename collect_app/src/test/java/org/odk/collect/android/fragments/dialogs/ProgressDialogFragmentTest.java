package org.odk.collect.android.fragments.dialogs;

import android.app.ProgressDialog;
import android.content.DialogInterface;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
public class ProgressDialogFragmentTest {

    private FragmentManager fragmentManager;

    @Before
    public void setup() {
        FragmentActivity activity = Robolectric.setupActivity(FragmentActivity.class);
        fragmentManager = activity.getSupportFragmentManager();
    }

    @Test
    public void setMessage_updatesMessage() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");

        fragment.setMessage("blah");
        CharSequence message = shadowOf((ProgressDialog) fragment.getDialog()).getMessage();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void setMessage_beforeDialogExists_setsMessageWhenDialogShown() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setMessage("blah");

        fragment.show(fragmentManager, "TAG");
        CharSequence message = shadowOf((ProgressDialog) fragment.getDialog()).getMessage();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void restoringFragment_retainsMessage() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");
        fragment.setMessage("blah");

        ProgressDialogFragment restoredFragment = new ProgressDialogFragment();
        restoredFragment.setArguments(fragment.getArguments());
        restoredFragment.show(fragmentManager, "TAG");
        CharSequence message = shadowOf((ProgressDialog) restoredFragment.getDialog()).getMessage();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void setTitle_updatesTitle() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");

        fragment.setTitle("blah");
        CharSequence message = shadowOf((ProgressDialog) fragment.getDialog()).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void setTitle_beforeDialogExists_setsTitleWhenDialogShown() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.setTitle("blah");

        fragment.show(fragmentManager, "TAG");
        CharSequence message = shadowOf((ProgressDialog) fragment.getDialog()).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void restoringFragment_retainsTitle() {
        ProgressDialogFragment fragment = new ProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");
        fragment.setTitle("blah");

        ProgressDialogFragment restoredFragment = new ProgressDialogFragment();
        restoredFragment.setArguments(fragment.getArguments());
        restoredFragment.show(fragmentManager, "TAG");
        CharSequence message = shadowOf((ProgressDialog) restoredFragment.getDialog()).getTitle();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void cancelling_callsCancelOnCancellable() {
        ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
        ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);

        fragment.onCancel(fragment.getDialog());
        verify(cancellable).cancel();
    }

    @Test
    public void whenThereIsCancelButtonText_clickingCancel_dismissesAndCallsCancelOnCancellable() {
        ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
        ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);
        fragment.show(fragmentManager, "TAG");
        ProgressDialog dialog = (ProgressDialog) fragment.getDialog();

        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).performClick();
        verify(cancellable).cancel();
        assertThat(dialog.isShowing(), equalTo(false));
    }

    public static class TestProgressDialogFragment extends ProgressDialogFragment {

        private final Cancellable cancellable;

        TestProgressDialogFragment(Cancellable cancellable) {
            this.cancellable = cancellable;
        }

        @Override
        protected String getCancelButtonText() {
            return "Blah";
        }

        @Override
        protected Cancellable getCancellable() {
            return cancellable;
        }
    }
}