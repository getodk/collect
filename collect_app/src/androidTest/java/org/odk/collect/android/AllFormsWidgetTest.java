package org.odk.collect.android;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.FrameLayout;

import org.hamcrest.Description;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;

import static android.support.test.espresso.Espresso.onView;

@RunWith(AndroidJUnit4.class)
public class AllFormsWidgetTest {
    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    private FormEntryActivity activity;

    @Before
    public void setupActivity() {
        activity = activityTestRule.getActivity();
    }

    @Test
    public void testActivityOpen()  {
    }

    private class FormEntryActivityTestRule extends ActivityTestRule<FormEntryActivity> {

        public FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            Uri formUri = Uri.parse("file:///android_asset/all_widgets.xml");
            intent.setData(formUri);

            return intent;
        }
    }

}
