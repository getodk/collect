package org.odk.collect.android.support.assertions;

import android.widget.FrameLayout;

import androidx.test.espresso.ViewAssertion;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;

public class RecyclerViewAssertions {

    private RecyclerViewAssertions() {
    }

    public static ViewAssertion isItemChecked() {
        return (view, noViewFoundException) -> assertThat(((FrameLayout) view.getParent()).getBackground(), is(notNullValue()));
    }

    public static ViewAssertion isItemNotChecked() {
        return (view, noViewFoundException) -> assertThat(((FrameLayout) view.getParent()).getBackground(), is(nullValue()));
    }
}
