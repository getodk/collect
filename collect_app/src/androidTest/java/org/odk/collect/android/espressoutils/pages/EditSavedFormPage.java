/*
 * Copyright 2019 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.espressoutils.pages;

import android.widget.RelativeLayout;

import androidx.appcompat.widget.Toolbar;
import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.InstanceListCursorAdapter;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.Matchers.not;

public class EditSavedFormPage extends Page<EditSavedFormPage> {
    public EditSavedFormPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public EditSavedFormPage assertOnPage() {
        checkIsStringDisplayed(R.string.review_data);
        return this;
    }

    public EditSavedFormPage checkInstanceState(String instanceName, String desiredStatus) {
        int desiredImageId = InstanceListCursorAdapter.getFormStateImageResourceIdForStatus(desiredStatus);

        onView(allOf(instanceOf(RelativeLayout.class),
                    hasDescendant(withText(instanceName)),
                    not(hasDescendant(instanceOf(Toolbar.class)))))
                .check(matches(hasDescendant(withTagValue(equalTo(desiredImageId)))));
        return this;
    }

    public OkDialog clickSavedFormWithDialog(String instanceName) {
        onView(withText(instanceName)).perform(click());
        return new OkDialog(rule).assertOnPage();
    }
}
