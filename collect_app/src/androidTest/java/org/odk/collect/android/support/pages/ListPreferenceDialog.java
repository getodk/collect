package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

public class ListPreferenceDialog<T extends Page<T>> extends Page<ListPreferenceDialog<T>> {

    private final int title;
    private final T page;

    ListPreferenceDialog(int title, T page, ActivityTestRule rule) {
        super(rule);
        this.title = title;
        this.page = page;
    }

    @Override
    public ListPreferenceDialog<T> assertOnPage() {
        assertText(title);
        return this;
    }

    public T clickOption(int option) {
        clickOnString(option);
        return page;
    }
}
