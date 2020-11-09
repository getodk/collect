package org.odk.collect.android.feature.storage;

import android.Manifest;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.ActivityTestRule;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.config.AppDependencyModule;
import org.odk.collect.android.storage.migration.StorageMigrationRepository;
import org.odk.collect.android.storage.migration.StorageMigrationResult;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import javax.inject.Singleton;

import dagger.Provides;

@RunWith(AndroidJUnit4.class)
public class StorageMigrationCompletedBannerTest {

    public ActivityTestRule<MainMenuActivity> rule = new ActivityTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule(new AppDependencyModule() {
                @Provides
                @Singleton
                public StorageMigrationRepository providesStorageMigrationRepository() {
                    StorageMigrationRepository storageMigrationRepository = new StorageMigrationRepository();
                    storageMigrationRepository.setResult(StorageMigrationResult.SUCCESS);
                    return storageMigrationRepository;
                }
            }))
            .around(rule);

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndDisappearsAfterScreenRotation() {
        new MainMenuPage(rule)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .rotateToLandscape(new MainMenuPage(rule))
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }

    @Test
    public void when_storageMigrationCompleted_should_bannerBeVisibleAndDismissForEverIfAUserClicksDismissButton() {
        new MainMenuPage(rule)
                .assertStorageMigrationCompletedBannerIsDisplayed()
                .clickDismissButton()
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToLandscape(new MainMenuPage(rule))
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .rotateToPortrait(new MainMenuPage(rule))
                .assertStorageMigrationCompletedBannerIsNotDisplayed()
                .recreateActivity()
                .assertStorageMigrationCompletedBannerIsNotDisplayed();
    }
}
