package org.odk.collect.android.storage;

import android.Manifest;

import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import javax.inject.Singleton;

import dagger.Provides;

import static org.odk.collect.android.support.CollectHelpers.overrideAppDependencyModule;

public class StorageMigrationCompletedBannerTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule());

    @Rule
    public ActivityTestRule<MainMenuActivity> main = new ActivityTestRule<MainMenuActivity>(MainMenuActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            overrideAppDependencyModule(new AppDependencyModule() {
                @Provides
                @Singleton
                public StorageMigrationRepository providesStorageMigrationRepository() {
                    StorageMigrationRepository storageMigrationRepository = new StorageMigrationRepository();
                    storageMigrationRepository.setResult(StorageMigrationResult.SUCCESS);
                    return storageMigrationRepository;
                }
            });
            new StorageStateProvider().enableUsingScopedStorage();
        }
    };

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
                .recreateActivity()
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndDismissAfterReopeningApp() {
        new MainMenuPage(main)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .recreateActivity()
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }
}
