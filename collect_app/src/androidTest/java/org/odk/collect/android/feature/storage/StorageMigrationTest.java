package org.odk.collect.android.feature.storage;

import android.Manifest;

import androidx.arch.core.executor.testing.CountingTaskExecutorRule;
import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.storage.migration.StorageMigrationService;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.CountingTaskExecutorIdlingResource;
import org.odk.collect.android.support.IdlingResourceRule;
import org.odk.collect.android.support.IntentServiceIdlingResource;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.StorageMigrationDialogPage;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class StorageMigrationTest {

    public final IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);
    private final CountingTaskExecutorRule countingTaskExecutorRule = new CountingTaskExecutorRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule(false))
            .around(countingTaskExecutorRule)
            .around(new IdlingResourceRule(new IntentServiceIdlingResource(StorageMigrationService.SERVICE_NAME)))
            .around(new IdlingResourceRule(new CountingTaskExecutorIdlingResource(countingTaskExecutorRule)))
            .around(new CopyFormRule("formWithExternalFiles.xml", Arrays.asList("formWithExternalFiles-media/itemsets.csv", "formWithExternalFiles-media/fruits.xml", "formWithExternalFiles-media/fruits.csv", "formWithExternalFiles-media/last-saved.xml"), true))
            .around(rule);

    @Test
    public void when_storageMigrationNotPerformed_should_bannerBeVisible() {
        new MainMenuPage(rule)
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToLandscape(new MainMenuPage(rule))
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToPortrait(new MainMenuPage(rule))
                .assertStorageMigrationBannerIsDisplayed()
                .recreateActivity()
                .assertStorageMigrationBannerIsDisplayed();
    }

    @Test
    public void when_migrationFinishedWithSuccess_should_formsWorkAsBefore() {
        // Fill the form with external files and migrate
        new MainMenuPage(rule)
                .startBlankForm("formWithExternalFiles")
                .putTextOnIndex(0, "John")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickLearnMoreButton()
                .clickMigrate()
                .assertStorageMigrationCompletedBannerIsDisplayed();

        // Open the saved form
        new MainMenuPage(rule)
                .clickEditSavedForm()
                .clickOnForm("formWithExternalFiles")
                .clickGoToStart()
                .assertText("John")
                .swipeToNextQuestion()
                .assertText("Apple", "Melon")
                .swipeToNextQuestion()
                .assertText("Mango", "Oranges")
                .swipeToNextQuestion()
                .assertText("Plum", "Cherry")
                .swipeToNextQuestion()
                .assertText("The fruit Cherry from pulldata function")
                .swipeToEndScreen()
                .clickSaveAndExit();

        // Fill another form
        new MainMenuPage(rule)
                .startBlankForm("formWithExternalFiles")
                .putTextOnIndex(0, "John")
                .swipeToNextQuestion()
                .clickOnText("Apple")
                .swipeToNextQuestion()
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .clickOnText("Cherry")
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit();
    }

    @Test
    public void when_thereAreNoSavedForms_should_thePromptToSubmitFormsBeNotVisible() {
        new MainMenuPage(rule)
                .clickLearnMoreButton()
                .assertStorageMigrationContentWithoutSavedFormsIsVisible();
    }

    @Test
    public void when_savedFormsExist_should_thePromptToSubmitFormsBeVisible() {
        new MainMenuPage(rule)
                .startBlankForm("formWithExternalFiles")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToEndScreen()
                .clickSaveAndExit()
                .clickLearnMoreButton()
                .assertStorageMigrationContentWithSavedFormsIsVisible();
    }

    @Test
    public void when_moreDetailsButtonClicked_should_odkForumPageAppear() {
        new MainMenuPage(rule)
                .clickLearnMoreButton()
                .clickMoreDetails()
                .assertForumPostOpen();
    }

    @Test
    public void when_backButtonPressed_should_storageMigrationDialogPersist() {
        new MainMenuPage(rule)
                .clickLearnMoreButton()
                .pressBack(new StorageMigrationDialogPage(rule));
    }

    @Test
    public void when_cancelButtonPressed_should_storageMigrationDialogBeClosed() {
        new MainMenuPage(rule)
                .clickLearnMoreButton()
                .clickCancel();
    }

    @Test
    public void when_rotationHappens_should_storageMigrationDialogPersist() {
        new MainMenuPage(rule)
                .clickLearnMoreButton()
                .rotateToLandscape(new StorageMigrationDialogPage(rule))
                .rotateToPortrait(new StorageMigrationDialogPage(rule));
    }
}

