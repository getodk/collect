package org.odk.collect.android.storage;

import android.Manifest;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.StorageMigrationNotPerformedRule;
import org.odk.collect.android.support.pages.MainMenuPage;

public class StorageMigrationTest {

    @Rule
    public IntentsTestRule<MainMenuActivity> main = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new StorageMigrationNotPerformedRule())
            .around(new ResetStateRule())
            .around(new CopyFormRule("basic.xml", true));

    @Test
    public void when_migrationIsFinishedWIthSuccess_should_storageMigrationDialogDisappear() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .clickMigrate()
                .assertStorageMigrationCompletedBannerIsDisplayed();
    }
}
