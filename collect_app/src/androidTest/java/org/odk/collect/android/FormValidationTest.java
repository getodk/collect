package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static org.hamcrest.Matchers.endsWith;

// Issue number NODK-251
@RunWith(AndroidJUnit4.class)
public class FormValidationTest extends BaseFormTest {

    //region Main test block.
    @Test
    public void testActivityOpen() {

        //TestCase1
        clickFillBlankForm();
        clickOnText("OnePageFormShort");
        onView(withIndex(withClassName(endsWith("Text")), 0)).perform(replaceText("A"));
        clickGoToIcon();
        clickJumpEndButton();
        clickSaveAndExit();
        checkIsToastWithMessageDisplayes("Response length must be between 5 and 15");
        checkIsDisplayed("Integer");

        //TestCase2

        //TestCase3
        clickGoToIcon();
        checkIsDisplayed("YY MM");
        checkIsDisplayed("YY");

    }
}