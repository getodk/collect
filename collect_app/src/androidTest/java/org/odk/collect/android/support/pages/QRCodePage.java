package org.odk.collect.android.support.pages;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.odk.collect.android.R;
import org.odk.collect.android.support.ActivityHelpers;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

public class QRCodePage extends Page<QRCodePage> {
    @Override
    public QRCodePage assertOnPage() {
        assertText(R.string.reconfigure_with_qr_code_settings_title);
        return this;
    }

    public QRCodePage clickScanFragment() {
        onView(withText(R.string.scan_qr_code_fragment_title)).perform(click());
        return this;
    }

    public QRCodePage clickView() {
        // Switching tabs doesn't seem to work sometimes
        waitFor(() -> {
            onView(withText(R.string.view_qr_code_fragment_title)).perform(click());
            onView(withText(R.string.barcode_scanner_prompt)).check(doesNotExist());
            return null;
        });

        return this;
    }

    public QRCodePage assertImageViewShowsImage(int resourceid, Bitmap image) {
        onView(withId(resourceid)).check(matches(DrawableMatcher.withBitmap(image)));
        return this;
    }

    public QRCodePage clickOnMenu() {
        tryAgainOnFail(() -> {
            Espresso.openActionBarOverflowOrOptionsMenu(ActivityHelpers.getActivity());
            onView(withText(getTranslatedString(R.string.import_qrcode_sd))).check(matches(isDisplayed()));
        });

        return this;
    }

    // Matcher class to match the contents of a ImageView and compare with a bitmap
    private static class DrawableMatcher {
        private static Matcher<View> withBitmap(Bitmap match) {
            return new BoundedMatcher<View, ImageView>(ImageView.class) {
                @Override
                public void describeTo(Description description) {
                    description.appendText("bitmaps did not match");
                }

                @Override
                protected boolean matchesSafely(ImageView imageView) {
                    Drawable drawable = imageView.getDrawable();
                    if (drawable == null && match == null) {
                        return true;
                    } else if (drawable != null && match == null) {
                        return false;
                    } else if (drawable == null && match != null) {
                        return false;
                    }

                    Bitmap actual = ((BitmapDrawable) drawable).getBitmap();

                    return actual.sameAs(match);
                }
            };
        }
    }

}
