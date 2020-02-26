package org.odk.collect.android.storage;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.StorageMigrationNotPerformedRule;
import org.odk.collect.android.support.pages.MainMenuPage;

public class StorageMigrationBannerTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public StorageMigrationNotPerformedRule storageMigrationNotPerformedRule = new StorageMigrationNotPerformedRule();

    @Test
    public void when_storageMigrationNotPerformed_should_bannerBeVisible() {
        new MainMenuPage(main)
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToLandscape(new MainMenuPage(main))
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToPortrait(new MainMenuPage(main))
                .assertStorageMigrationBannerIsDisplayed()
                .reopenApp()
                .assertStorageMigrationBannerIsDisplayed();
    }

    @Test
    public void when_learMoreButtonClicked_should_storageMigrationDialogAppear() {
        new MainMenuPage(main)
                .clickLearnMoreButton();
    }
}
