package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

// Issue number NODK-377
@RunWith(AndroidJUnit4.class)
public class ExternalSecondaryInstancesTest extends BaseFormTest {

    //region Main test block.
    @Test
    public void testActivityOpen() {

        //TestCase1
        clickFillBlankForm();
        clickOnText("external select 10");
        onView(withText("b")).perform(click()).perform(swipeLeft());
        onView(withText("ba")).perform(click()).perform(swipeLeft());
        clickSaveAndExit();

        //TestCase2
        clickFillBlankForm();
        clickOnText("internal select 10");
        onView(withText("c")).perform(click()).perform(swipeLeft());
        onView(withText("ca")).perform(click()).perform(swipeLeft());
        clickSaveAndExit();
    }
}