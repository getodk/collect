package org.odk.collect.android.support.matchers;

import android.view.View;
import android.widget.ImageView;

import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public class DrawableMatcher  {

    private DrawableMatcher() {
    }

    public static Matcher<View> withImageDrawable(final int expectedResourceId) {
        return new BoundedMatcher<View, ImageView>(ImageView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has image drawable resource " + expectedResourceId);
            }

            @Override
            public boolean matchesSafely(ImageView imageView) {
                return expectedResourceId == (Integer) imageView.getTag();
            }
        };
    }
}