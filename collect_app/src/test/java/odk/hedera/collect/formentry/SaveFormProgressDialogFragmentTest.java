package odk.hedera.collect.formentry;

import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import odk.hedera.collect.formentry.saving.SaveFormProgressDialogFragment;
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
        fragmentManager = activity.getSupportFragmentManager();
    }

    @Test
    public void dialogIsNotCancellable() {
        SaveFormProgressDialogFragment fragment = new SaveFormProgressDialogFragment();
        fragment.show(fragmentManager, "TAG");

        assertThat(shadowOf(fragment.getDialog()).isCancelable(), equalTo(false));
    }
}
