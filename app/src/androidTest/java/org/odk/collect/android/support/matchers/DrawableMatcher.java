package org.odk.collect.android.support.matchers;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.test.espresso.matcher.BoundedMatcher;

import org.hamcrest.Description;
import org.hamcrest.Matcher;

public final class DrawableMatcher  {

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

    public static Matcher<View> withBitmap(Bitmap match) {
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