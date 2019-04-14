package org.odk.collect.android.formfilling;

import android.Manifest;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Environment;
import androidx.test.InstrumentationRegistry;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;
import android.view.View;

import org.apache.commons.io.IOUtils;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyAbove;
import static androidx.test.espresso.assertion.PositionAssertions.isCompletelyBelow;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.core.StringStartsWith.startsWith;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;

@RunWith(AndroidJUnit4.class)
/**
 * TODO: consolidate boilerplate with {@link org.odk.collect.android.AllWidgetsFormTest}? Challenge
 * is that much of it is static and needs to have the form path.
 */
public class FieldListUpdateTest {
    private static final String FORMS_PATH = Environment.getExternalStorageDirectory().getPath() + "/odk/forms/";
    private static final String FIELD_LIST_TEST_FORM = "fieldlist-updates.xml";

    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Rule
    public GrantPermissionRule permissionRule = GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname =  FORMS_PATH + FIELD_LIST_TEST_FORM;

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(FIELD_LIST_TEST_FORM);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    @Test
    public void relevanceChangeAtEnd_ShouldToggleLastWidgetVisibility() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        // TODO: what is with those hidden views with the same text?
        onView(allOf(withText("Single relevance at end"), isDisplayed())).perform(click());
        onView(withText("Source1")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target1")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target1")).check(matches(isDisplayed()));
        onView(withText("Target1")).check(isCompletelyBelow(withText("Source1")));

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target1")).check(doesNotExist());
    }

    @Test
    public void relevanceChangeAtBeginning_ShouldToggleFirstWidgetVisibility() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Single relevance at beginning"), isDisplayed())).perform(click());
        onView(withText("Source2")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target2")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target2")).check(matches(isDisplayed()));
        onView(withText("Target2")).check(isCompletelyAbove(withText("Source2")));

        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Target2")).check(doesNotExist());
    }

    @Test
    public void relevanceChangeInMiddle_ShouldToggleMiddleWidgetVisibility() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Single relevance in middle"), isDisplayed())).perform(click());
        onView(withText("Source3")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target3")).check(matches(isDisplayed()));
        onView(withText("Target3")).check(isCompletelyBelow(withText("Source3")));
        onView(withText("Target3")).check(isCompletelyAbove(withText("Filler3")));

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
    }

    @Test
    public void longPress_ShouldClearAndUpdate() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Single relevance in middle"), isDisplayed())).perform(click());
        onView(withText("Source3")).perform(click());

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Target3")).check(doesNotExist());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText("A"));
        onView(withText("Target3")).check(matches(isDisplayed()));

        onView(withText("Source3")).perform(longClick());
        onView(withText(R.string.clear_answer)).perform(click());
        onView(withText(R.string.discard_answer)).perform(click());
        onView(withIndex(withClassName(endsWith("EditText")), 0)).check(matches(withText("")));
        onView(withText("Target3")).check(doesNotExist());
    }

    @Test
    public void changeInValueUsedInLabel_ShouldChangeLabelText() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Label change"), isDisplayed())).perform(click());
        onView(withText(startsWith("Hello"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Hello, , how are you today?")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(name));
        onView(withText("Hello, " + name + ", how are you today?")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).perform(replaceText(""));
        onView(withText("Hello, , how are you today?")).check(matches(isDisplayed()));
    }

    @Test
    public void changeInValueUsedInHint_ShouldChangeHintText() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Hint change"), isDisplayed())).perform(click());
        onView(withText(startsWith("What is your"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Please don't use your calculator, !")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(name));
        onView(withText("Please don't use your calculator, " + name + "!")).check(matches(isDisplayed()));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withText("Please don't use your calculator, !")).check(matches(isDisplayed()));
    }

    /** TODO: calculation doesn't seem to be updated whether or not there's a fieldlist.
    @Test
    public void changeInValueUsedInOtherField_ShouldChangeValue() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Value change"), isDisplayed())).perform(click());
        onView(withText(startsWith("What is your"))).perform(click());

        String name = UUID.randomUUID().toString();

        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText("0")));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(name));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText(name.length())));
        onView(withIndex(withClassName(endsWith("EditText")), 0)).perform(replaceText(""));
        onView(withIndex(withClassName(endsWith("EditText")), 1)).check(matches(withText("0")));
    }
    **/

    @Test
    public void selectionChangeAtFirstCascadeLevel_ShouldUpdateNextLevels() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Cascading select"), isDisplayed())).perform(click());
        onView(withText(startsWith("Level1"))).perform(click());

        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting C for level 1 should only reveal options for C at level 2
        onView(withText("C")).perform(click());
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());

        // Selecting A for level 1 should reveal options for A at level 2
        onView(withText("A")).perform(click());
        onView(withText("A1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());

        // Selecting A1 for level 2 should reveal options for A1 at level 3
        onView(withText("A1")).perform(click());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    // TODO: figure out why the third level isn't cleared. Wondering whether there might be an issue
    // with populateDynamicChoices
    //    @Test
    //    public void clearingParentSelect_ShouldUpdateAllDependentLevels() {
    //        onView(withId(R.id.menu_goto)).perform(click());
    //        onView(withId(R.id.menu_go_up)).perform(click());
    //        onView(allOf(withText("Cascading select"), isDisplayed())).perform(click());
    //        onView(withText(startsWith("Level1"))).perform(click());
    //
    //        onView(withText("A")).perform(click());
    //        onView(withText("A1")).perform(click());
    //        onView(withText("A1B")).perform(click());
    //
    //        onView(withText("A")).perform(longClick());
    //        onView(withText(R.string.clear_answer)).perform(click());
    //        onView(withText(R.string.discard_answer)).perform(click());
    //
    //        onView(withIndex(withClassName(endsWith("RadioButton")), 0)).check(matches(isNotChecked()));
    //        onView(withText("A1")).check(doesNotExist());
    //        onView(withText("A1B")).check(doesNotExist());
    //    }

    @Test
    public void selectionChangeAtOneCascadeLevelWithMinimalAppearance_ShouldUpdateNextLevels() {
        onView(withId(R.id.menu_goto)).perform(click());
        onView(withId(R.id.menu_go_up)).perform(click());
        onView(allOf(withText("Cascading select minimal"), isDisplayed())).perform(click());
        onView(withText(startsWith("Level1"))).perform(click());

        // No choices should be shown for levels 2 and 3 when no selection is made for level 1
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting C for level 1 should only reveal options for C at level 2
        onView(withIndex(withText(R.string.select_one), 0)).perform(click());
        onView(withText("C")).perform(click());
        onView(withText("A1")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withIndex(withText(R.string.select_one), 0)).perform(click());
        onView(withText("C1")).perform(click());
        onView(withText("A1A")).check(doesNotExist());

        // Selecting A for level 1 should reveal options for A at level 2
        onView(withText("C")).perform(click());
        onView(withText("A")).perform(click());
        onView(withIndex(withText(R.string.select_one), 0)).perform(click());
        onView(withText("A1")).check(matches(isDisplayed()));
        onView(withText("A1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());

        // Selecting A1 for level 2 should reveal options for A1 at level 3
        onView(withText("A1")).perform(click());
        onView(withIndex(withText(R.string.select_one), 0)).perform(click());
        onView(withText("A1A")).check(matches(isDisplayed()));
        onView(withText("B1A")).check(doesNotExist());
        onView(withText("B1")).check(doesNotExist());
        onView(withText("C1")).check(doesNotExist());
    }

    private static class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {
        FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Intent intent = new Intent(ApplicationProvider.getApplicationContext(), FormEntryActivity.class);
            intent.putExtra(EXTRA_TESTING_PATH, FORMS_PATH + FIELD_LIST_TEST_FORM);

            return intent;
        }
    }

    // https://stackoverflow.com/a/39756832
    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }
}
