package org.odk.collect.androidtest;

import static androidx.test.espresso.matcher.ViewMatchers.Visibility;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.util.HumanReadables.describe;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;

import android.graphics.Rect;
import android.view.View;
import android.widget.HorizontalScrollView;
import android.widget.ScrollView;

import androidx.core.widget.NestedScrollView;
import androidx.test.espresso.PerformException;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.ScrollToAction;

import org.hamcrest.Matcher;

/**
 * Required for scrolling to items within a {@link NestedScrollView}.
 * Code is copied from {@link ScrollToAction} as for some reason that class is final.
 */

public class NestedScrollToAction implements ViewAction {

    public static NestedScrollToAction nestedScrollTo() {
        return new NestedScrollToAction();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Matcher<View> getConstraints() {
        return allOf(withEffectiveVisibility(Visibility.VISIBLE), isDescendantOfA(anyOf(
                isAssignableFrom(ScrollView.class),
                isAssignableFrom(HorizontalScrollView.class),
                isAssignableFrom(NestedScrollView.class)
        )));
    }

    @Override
    public void perform(UiController uiController, View view) {
        if (isDisplayingAtLeast(90).matches(view)) {
            return;
        }
        Rect rect = new Rect();
        view.getDrawingRect(rect);
        view.requestRectangleOnScreen(rect, true /* immediate */);

        uiController.loopMainThreadUntilIdle();
        if (!isDisplayingAtLeast(90).matches(view)) {
            throw new PerformException.Builder()
                    .withActionDescription(this.getDescription())
                    .withViewDescription(describe(view))
                    .withCause(
                            new RuntimeException(
                                    "Scrolling to view was attempted, but the view is not displayed"))
                    .build();
        }
    }

    @Override
    public String getDescription() {
        return "scroll to";
    }
}

