package org.odk.collect.android;

import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.views.ODKView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

@RunWith(AndroidJUnit4.class)
public class AllFormsWidgetTest {

    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        Context instrumentationContext = InstrumentationRegistry.getContext();

        AssetManager assetManager = instrumentationContext.getAssets();
        InputStream inputStream = assetManager.open("all_widgets.xml");

        String path = Environment.getExternalStorageDirectory().getPath() + "/odk/forms/all_widgets.xml";
        File outFile = new File(path);

        OutputStream outputStream = new FileOutputStream(outFile);
        IOUtils.copy(inputStream, outputStream);
    }

    @Test
    public void testActivityOpen()  {
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
        onView(withClassName(is(ODKView.class.toString()))).perform(swipeLeft());
    }

    private class FormEntryActivityTestRule extends ActivityTestRule<FormEntryActivity> {

        public FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            String path = Environment.getExternalStorageDirectory().getPath() + "/odk/forms/all_widgets.xml";
            intent.putExtra(EXTRA_TESTING_PATH, path);

            return intent;
        }

        @Override
        protected void afterActivityLaunched() {
            super.afterActivityLaunched();
        }
    }

}
