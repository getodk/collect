package org.odk.collect.android;

import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

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
