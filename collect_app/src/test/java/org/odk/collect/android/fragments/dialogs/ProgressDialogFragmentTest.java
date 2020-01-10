package org.odk.collect.android.fragments.dialogs;

import android.app.ProgressDialog;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowProgressDialog;

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
    public void create_setsTitleAndMessage() {
        ProgressDialogFragment fragment = ProgressDialogFragment.create("A title", "A message");
        fragment.show(fragmentManager, "TAG");

        ShadowProgressDialog dialog = shadowOf((ProgressDialog) fragment.getDialog());
        assertThat(dialog.getTitle(), equalTo("A title"));
        assertThat(dialog.getMessage(), equalTo("A message"));
    }

    @Test
    public void setMessage_beforeDialogExists_setsMessageWhenDialogShown() {
        ProgressDialogFragment fragment = ProgressDialogFragment.create("A title", "A message");
        fragment.setMessage("blah");

        fragment.show(fragmentManager, "TAG");
        CharSequence message = shadowOf((ProgressDialog) fragment.getDialog()).getMessage();
        assertThat(message, equalTo("blah"));
    }

    @Test
    public void clickingCancel_callsCancelOnCancellable() {
        ProgressDialogFragment.Cancellable cancellable = mock(ProgressDialogFragment.Cancellable.class);
        ProgressDialogFragment fragment = new TestProgressDialogFragment(cancellable);
        fragment.setArguments("title", "A title");

        fragment.onCancel(fragment.getDialog());
        verify(cancellable).cancel();
    }

    public static class TestProgressDialogFragment extends ProgressDialogFragment {

        private final Cancellable cancellable;

         TestProgressDialogFragment(Cancellable cancellable) {
            this.cancellable = cancellable;
        }

        @Override
        protected Cancellable getCancellable() {
            return cancellable;
        }
    }
}