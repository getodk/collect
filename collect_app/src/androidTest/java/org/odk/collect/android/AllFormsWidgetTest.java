package org.odk.collect.android;

import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.runner.AndroidJUnit4;

import net.bytebuddy.utility.RandomString;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ActivityUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Random;

import static android.app.Activity.RESULT_OK;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intended;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.Visibility.VISIBLE;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

@RunWith(AndroidJUnit4.class)
public class AllFormsWidgetTest {

    private static final String ALL_WIDGETS_FORM = "all_widgets.xml";
    private static final String FORMS_DIRECTORY = "/odk/forms/";

    private final Random random = new Random();

    private String stringWidgetText = randomString();
    private String stringNumberWidgetText = randomNumberString();

    private String exStringWidgetFirstText = randomString();
    private String exStringWidgetSecondText = randomString();

    private ActivityResult okResult = new ActivityResult(RESULT_OK, new Intent());

    @Mock
    ActivityUtil activityUtil;

    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname = formPath();
        if (new File(pathname).exists()) {
            return;
        }

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(ALL_WIDGETS_FORM);
;
        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    public static String formPath() {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + ALL_WIDGETS_FORM;
    }

    @Before
    public void prepareDependencies() {
        activityTestRule.getActivity()
                .setActivityUtil(activityUtil);
    }

    @Test
    public void testActivityOpen()  {

        // Label widget:
        onView(withText(startsWith("This form"))).perform(swipeLeft());

        // String widget:
        onVisibleEditText().perform(replaceText(stringWidgetText));
        onView(withText("String widget")).perform(swipeLeft());

        // String number widget:
        onVisibleEditText().perform(replaceText(stringNumberWidgetText));
        onView(withText("String number widget")).perform(swipeLeft());

        // UrlWidget:
        Uri uri = Uri.parse("http://opendatakit.org/");

        intending(allOf(hasAction(Intent.ACTION_VIEW), hasData(uri)))
                .respondWith(okResult);

        onView(withText("Open Url")).perform(click());
        onView(withText("URL widget")).perform(swipeLeft());

        // Ex String Widget:
        when(activityUtil.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

        onView(withText("Launch")).perform(click());
        onVisibleEditText().perform(replaceText(exStringWidgetFirstText));

        onView(withText("Ex string widget")).perform(swipeLeft());
        onView(withText("Ex printer widget")).perform(swipeRight());

        Intent stringIntent = new Intent();
        stringIntent.putExtra("value", exStringWidgetSecondText);

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);
        intending(allOf(
                hasAction("change.uw.android.BREATHCOUNT"),
                hasExtra("value", exStringWidgetFirstText)

        )).respondWith(exStringResult);


        when(activityUtil.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        onView(withText("Launch")).perform(click());
        onView(withText(exStringWidgetSecondText))
                .check(matches(isDisplayed()));

        onView(withText("Ex string widget")).perform(swipeLeft());

        // ExPrinterWidget

        onView(withText("Initiate Printing")).perform(click());

        intended(hasAction("org.opendatakit.sensors.ZebraPrinter"));
        // There is also a BroadcastIntent that sends the data but we don't
        // have a way to test that currently.
        // Will probably move that out to a helper class we can Unit test in Robolectric and
        // inject here.

        onView(withText("Ex printer widget")).perform(swipeLeft());

        // Integer widget:

        onView(withText("Integer widget")).perform(swipeLeft());
        // Ex integer widget:

        onView(withText("Ex integer widget")).perform(swipeLeft());
        onView(withText("Decimal widget")).perform(swipeLeft());
        onView(withText("Ex decimal widget")).perform(swipeLeft());
        onView(withText("Bearing widget")).perform(swipeLeft());

        onView(withText("Image widget")).perform(swipeLeft());
        onView(withText("Selfie widget")).perform(swipeLeft());
        onView(withText("Draw widget")).perform(swipeLeft());
        onView(withText("Annotate widget")).perform(swipeLeft());
        onView(withText("Signature widget")).perform(swipeLeft());

        onView(withText("Web view image widget")).perform(swipeLeft());
        onView(withText("Align image widget")).perform(swipeLeft());

        onView(withText("Barcode widget")).perform(swipeLeft());
        onView(withText("Audio widget")).perform(swipeLeft());
        onView(withText("Video widget")).perform(swipeLeft());
        onView(withText("Date widget")).perform(swipeLeft());
        onView(withText("Date Widget")).perform(swipeLeft());
        onView(withText("Date widget")).perform(swipeLeft());
        onView(withText("Date widget")).perform(swipeLeft());
        onView(withText("Time widget")).perform(swipeLeft());

        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());
        onView(allOf(withText("Date time widget"), withEffectiveVisibility(VISIBLE)))
                .perform(swipeLeft());

        onView(withText("Geopoint widget")).perform(swipeLeft());
        onView(withText("Geopoint widget")).perform(swipeLeft());
        onView(withText("Geopoint widget")).perform(swipeLeft());
        onView(withText("Geotrace widget")).perform(swipeLeft());
        onView(withText("Geoshape widget")).perform(swipeLeft());
        onView(withText("OSM integration")).perform(swipeLeft());
        onView(withText("OSM integration")).perform(swipeLeft());
        onView(withText("Select one widget")).perform(swipeLeft());
        onView(withText("Spinner widget")).perform(swipeLeft());
        onView(withText("Select one autoadvance widget")).perform(swipeLeft());
        onView(withText("Select one search widget")).perform(swipeLeft());
        onView(withText("Select one search widget")).perform(swipeLeft());
        onView(withText("Grid select one widget")).perform(swipeLeft());
        onView(withText("Grid select one widget")).perform(swipeLeft());
        onView(withText("Grid select one widget")).perform(swipeLeft());
        onView(withText("Grid select one widget")).perform(swipeLeft());
        onView(withText("Grid select one widget")).perform(swipeLeft());
        onView(withText("Multi select widget")).perform(swipeLeft());
        onView(withText("Grid select multiple widget")).perform(swipeLeft());
        onView(withText("Grid select multiple widget")).perform(swipeLeft());
        onView(withText("Spinner widget: select multiple")).perform(swipeLeft());
        onView(withText("Label widget")).perform(swipeLeft());
        onView(withText("Trigger widget")).perform(swipeLeft());

    }

    public static ViewInteraction onVisibleEditText() {
        return onView(withClassName(endsWith("EditText")));
    }

    private String randomString() {
        return RandomString.make();
    }

    private int randomInt() {
        return Math.abs(random.nextInt());
    }

    private String randomNumberString() {
        return Integer.toString(randomInt());
    }

    private class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {

        public FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            intent.putExtra(EXTRA_TESTING_PATH, formPath());

            return intent;
        }
    }
}
