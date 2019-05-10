package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;


// Issue number NODK-209
@RunWith(AndroidJUnit4.class)
public class DrawWidgetTest extends BaseFormTest {

    @Test
    public void testActivityOpen() {

        //TestCase1
        clickFillBlankForm();
        clickOnText("All widgets");
        clickGoToIcon();
        clickOnText("Image widgets");
        clickOnText("Draw widget");
        onView(withId(R.id.simple_button)).perform(click());
        pressBack();
        checkIsDisplayed("Exit Sketch Image");
        checkIsDisplayed("Save Changes");
        checkIsDisplayed("Ignore Changes");
        clickOnText("Ignore Changes");
        onView(withId(R.id.simple_button)).perform(click());
        pressBack();
        clickOnText("Save Changes");

        //TestCase2
        onView(withId(R.id.simple_button)).perform(click());
        onView(withId(R.id.fab_actions)).perform(click());
        clickOnText("Set Color");
        clickOnText("OK");
        pressBack();
        clickOnText("Save Changes");

        //TestCase3
        onView(withId(R.id.simple_button)).perform(click());
        onView(withId(R.id.fab_actions)).perform(click());
        checkIsDisplayed("Set Color");
        onView(withId(R.id.fab_actions)).perform(click());
        checkIsDisplayed("Set Color");
        onView(withId(R.id.fab_actions)).perform(click());
        checkIsDisplayed("Set Color");
        pressBack();
        clickOnText("Save Changes");
        clickGoToIcon();
        clickJumpEndButton();
        clickSaveAndExit();

    }
}
