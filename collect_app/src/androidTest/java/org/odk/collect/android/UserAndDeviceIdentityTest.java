package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.StringEndsWith.endsWith;

// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest extends BaseFormTest {

    @Test
    public void testActivityOpen() {

        //TestCase1
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("User and device identity");
        clickOnText("Form metadata");
        clickOnText("Email address");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("aabb"));
        onView(withText("OK")).perform(click());
        checkIsToastWithMessageDisplayes("Invalid email address!");
        clickOnText("Email address");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("aa@bb"));
        onView(withText("OK")).perform(click());
        checkIsDisplayed("aa@bb");
        pressBack();
        pressBack();
        pressBack();

        //TestCase2
        clickFillBlankForm();
        clickOnText("Test");
        onView(withClassName(endsWith("EditText"))).check(matches(withText("")));
        onView(withText("Username")).perform(swipeLeft());
        clickSaveAndExit();

        //TestCase3
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("User and device identity");
        clickOnText("Form metadata");
        clickOnText("Username");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("AAA"));
        onView(withText("OK")).perform(click());
        pressBack();
        pressBack();
        pressBack();
        clickFillBlankForm();
        clickOnText("Test");
        onView(withClassName(endsWith("EditText"))).check(matches(withText("AAA")));
        onView(withText("Username")).perform(swipeLeft());
        clickSaveAndExit();

        //TestCase4
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("User and device identity");
        clickOnText("Form metadata");
        clickOnText("Username");
        onView(withClassName(endsWith("EditText"))).perform(replaceText(""));
        onView(withText("OK")).perform(click());
        pressBack();
        pressBack();
        pressBack();
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("Server");
        clickOnText("Type");
        clickOnText("ODK Aggregate");
        clickOnText("Username");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("BBB"));
        onView(withText("OK")).perform(click());
        pressBack();
        pressBack();
        clickFillBlankForm();
        clickOnText("Test");
        onView(withClassName(endsWith("EditText"))).check(matches(withText("BBB")));
        onView(withText("Username")).perform(swipeLeft());
        clickSaveAndExit();

        //TestCase5
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("User and device identity");
        clickOnText("Form metadata");
        clickOnText("Username");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("CCC"));
        onView(withText("OK")).perform(click());
        pressBack();
        pressBack();
        pressBack();
        clickOptionsIcon();
        clickGeneralSettings();
        clickOnText("Server");
        clickOnText("Type");
        clickOnText("ODK Aggregate");
        clickOnText("Username");
        onView(withClassName(endsWith("EditText"))).perform(replaceText("DDD"));
        onView(withText("OK")).perform(click());
        pressBack();
        pressBack();
        clickFillBlankForm();
        clickOnText("Test");
        onView(withClassName(endsWith("EditText"))).check(matches(withText("CCC")));
        onView(withText("Username")).perform(swipeLeft());
        clickSaveAndExit();

    }
}
