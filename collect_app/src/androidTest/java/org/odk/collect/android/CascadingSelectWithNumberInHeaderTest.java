package org.odk.collect.android;

import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.runner.AndroidJUnit4;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class CascadingSelectWithNumberInHeaderTest extends BaseFormTest {

    @Test
    public void testActivityOpen() {

        clickFillBlankForm();
        clickOnText("numberInCSV");
        onView(withText("number")).perform(swipeLeft());
        clickOnText("Venda de animais");
        onView(withText("1a")).perform(swipeLeft());
        clickOnText("Vendas agrícolas");
        onView(withText("2a")).perform(swipeLeft());
        clickOnText("Pensão");
        onView(withText("3a")).perform(swipeLeft());
        onView(withText("Thank you for taking the time to complete this form!")).perform(swipeLeft());
        clickSaveAndExit();

    }
}
