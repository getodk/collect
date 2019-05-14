package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

// Issue number NODK-211
@RunWith(AndroidJUnit4.class)
public class SignatureWidgetTest extends BaseFormTest {

    @Test
    public void testActivityOpen() {

        //TestCase1
        clickFillBlankForm();
        clickOnText("All widgets");
        clickGoToIcon();
        clickOnText("Image widgets");
        clickOnText("Signature widget");
        onView(withId(R.id.simple_button)).perform(click());
        pressBack();
        checkIsDisplayed("Exit Gather Signature");
        checkIsDisplayed("Save Changes");
        checkIsDisplayed("Ignore Changes");
        clickOnText("Ignore Changes");
        onView(withId(R.id.simple_button)).perform(click());
        pressBack();
        clickOnText("Save Changes");

        //TestCase2
        onView(withId(R.id.simple_button)).perform(click());
        onView(withId(R.id.fab_actions)).perform(click());
        onView(withId(R.id.fab_save_and_close)).check(matches(isDisplayed()));
        onView(withId(R.id.fab_set_color)).perform(click());
        clickOnText("OK");
        onView(withId(R.id.fab_actions)).perform(click());
        onView(withId(R.id.fab_set_color)).check(matches(isDisplayed()));
        pressBack();
        clickOnText("Save Changes");
        clickGoToIcon();
        clickJumpEndButton();
        clickSaveAndExit();
    }
}
