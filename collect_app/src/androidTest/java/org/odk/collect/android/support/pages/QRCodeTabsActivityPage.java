package org.odk.collect.android.support.pages;

import android.view.View;
import android.widget.ImageView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.odk.collect.android.R;
import org.odk.collect.android.preferences.qr.QRCodeTabsActivity;
import org.odk.collect.android.support.ActivityHelpers;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

public class QRCodeTabsActivityPage extends Page<QRCodeTabsActivityPage>{
    public QRCodeTabsActivityPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public QRCodeTabsActivityPage assertOnPage() {
        checkIsStringDisplayed(R.string.configure_via_qr_code);
        return this;
    }

    public QRCodeTabsActivityPage clickScanFragment() {
        onView(withText(R.string.scan_qr_code_fragment_title)).perform(click());
        return this;
    }

    public QRCodeTabsActivityPage clickViewQRFragment() {
        onView(withText(R.string.view_qr_code_fragment_title)).perform(click());
        return this;
    }

    public QRCodeTabsActivityPage assertImageViewShowsImage(int resource) {
        onView(withId(resource)).check(matches(DrawableMatcher.withDrawable(DrawableMatcher.ANY)));
        return this;
    }

    public QRCodeTabsActivityPage clickOnMenu() {
        Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
        return this;
    }

    private static class DrawableMatcher {
        private static final int ANY = -2;
        private static final int NONE = -1;

        private static Matcher<View> withDrawable(int match) {
            return new BoundedMatcher<View, ImageView>(ImageView.class) {
                @Override
                public void describeTo(Description description) {
                    description.appendText("expected " + match);
                }

                @Override
                protected boolean matchesSafely(ImageView item) {
                    if (match == NONE) {
                        return item.getDrawable() == null;
                    } else if (match == ANY) {
                        return item.getDrawable() != null;
                    }

                    return false;
                }
            };
        }
    }

}
