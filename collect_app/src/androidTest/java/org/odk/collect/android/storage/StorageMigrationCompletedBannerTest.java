package org.odk.collect.android.storage;

import androidx.test.rule.ActivityTestRule;

import org.junit.Rule;
import org.junit.Test;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.support.StorageMigrationCompletedRule;
import org.odk.collect.android.support.pages.MainMenuPage;

public class StorageMigrationCompletedBannerTest {

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public StorageMigrationCompletedRule storageMigrationCompletedRule = new StorageMigrationCompletedRule();

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndPersistScreenRotation() {
        new MainMenuPage(main)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .rotateToLandscape(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .rotateToPortrait(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .clickDismissButton()
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToLandscape(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToPortrait(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndDismissForEverIfAUserClicksDismissButton() {
        new MainMenuPage(main)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .clickDismissButton()
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToLandscape(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToPortrait(new MainMenuPage(main))
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .reopenApp()
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndDismissAfterReopeningApp() {
        new MainMenuPage(main)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .reopenApp()
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }
}
