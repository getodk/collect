package org.odk.collect.android.support.actions;

import android.view.View;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

public class RecyclerViewAction {

    private RecyclerViewAction() {
    }

    public static ViewAction clickItemWithId(int id) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return null;
            }

            @Override
            public String getDescription() {
                return "Click on a child view with specified id";
            }

            @Override
            public void perform(UiController uiController, View view) {
                view.findViewById(id).performClick();
            }
        };
    }
}
