package org.odk.collect.android.support.pages;

import androidx.test.rule.ActivityTestRule;

import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class StorageMigrationDialogPage<D extends Page<D>> extends Page<StorageMigrationDialogPage<D>> {

    public StorageMigrationDialogPage(ActivityTestRule rule) {
        super(rule);
    }

    @Override
    public StorageMigrationDialogPage<D> assertOnPage() {
        checkIsStringDisplayed(R.string.storage_migration_dialog_title);
        return this;
    }

    public MainMenuPage clickMigrate() {
        onView(withId(R.id.migrateButton)).perform(click());
        return new MainMenuPage(rule).assertOnPage();
    }
}