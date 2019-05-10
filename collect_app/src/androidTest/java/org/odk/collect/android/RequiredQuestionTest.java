package org.odk.collect.android;

import org.junit.Test;

import androidx.test.espresso.assertion.ViewAssertions;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class RequiredQuestionTest extends BaseFormTest {

    //region Main test block.
    @Test
    public void testActivityOpen() {

        //TestCase1
        clickFillBlankForm();
        clickOnText("required");
        onView(withText("* Foo")).check(ViewAssertions.matches(isDisplayed())).perform(swipeLeft());

        //TestCase2
        checkIsToastWithMessageDisplayes("Custom required message");
    }
}
