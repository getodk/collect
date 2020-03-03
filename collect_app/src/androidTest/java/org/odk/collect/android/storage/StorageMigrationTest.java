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
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.StorageMigrationDialogPage;

import java.util.Arrays;

public class StorageMigrationTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ))
            .around(new ResetStateRule())
            .around(new CopyFormRule("formWithExternalFiles.xml", Arrays.asList("formWithExternalFiles-media/itemsets.csv", "formWithExternalFiles-media/fruits.xml", "formWithExternalFiles-media/fruits.csv", "formWithExternalFiles-media/last-saved.xml"), true));

    @Rule
    public IntentsTestRule<MainMenuActivity> main = new IntentsTestRule<MainMenuActivity>(MainMenuActivity.class) {
        @Override
        protected void beforeActivityLaunched() {
            new StorageStateProvider().disableUsingScopedStorage();
        }
    };

    @Test
    public void when_migrationFinishedWithSuccess_should_formsWorkAsBefore() {
        // Fill the form with external files and migrate
        new MainMenuPage(main)
                .startBlankForm("formWithExternalFiles")
                .putTextOnIndex(0, "John")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .clickLearnMoreButton()
                .clickMigrate()
                .assertStorageMigrationCompletedBannerIsDisplayed();

        // Open the saved form
        new MainMenuPage(main)
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
                .swipeToNextQuestion()
                .clickSaveAndExit();

        // Fill another form
        new MainMenuPage(main)
                .startBlankForm("formWithExternalFiles")
                .putTextOnIndex(0, "John")
                .swipeToNextQuestion()
                .clickOnText("Apple")
                .swipeToNextQuestion()
                .clickOnText("Mango")
                .swipeToNextQuestion()
                .clickOnText("Cherry")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit();
    }

    @Test
    public void when_storageMigrationNotPerformed_should_bannerBeVisible() {
        new MainMenuPage(main)
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToLandscape(new MainMenuPage(main))
                .assertStorageMigrationBannerIsDisplayed()
                .rotateToPortrait(new MainMenuPage(main))
                .assertStorageMigrationBannerIsDisplayed()
                .recreateActivity()
                .assertStorageMigrationBannerIsDisplayed();
    }

    @Test
    public void when_thereAreNoSavedForms_should_thePromptToSubmitFormsBeNotVisible() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .assertStorageMigrationContentWithoutSavedFormsIsVisible();
    }

    @Test
    public void when_savedFormsExist_should_thePromptToSubmitFormsBeVisible() {
        new MainMenuPage(main)
                .startBlankForm("formWithExternalFiles")
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .swipeToNextQuestion()
                .clickSaveAndExit()
                .clickLearnMoreButton()
                .assertStorageMigrationContentWithSavedFormsIsVisible();
    }

    @Test
    public void when_moreDetailsButtonClicked_should_odkForumPageAppear() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .clickMoreDetails()
                .assertWebViewOpen();
    }

    @Test
    public void when_backButtonPressed_should_storageMigrationDialogPersist() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .pressBack(new StorageMigrationDialogPage(main));
    }

    @Test
    public void when_cancelButtonPressed_should_storageMigrationDialogBeClosed() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .clickCancel();
    }

    @Test
    public void when_rotationHappens_should_storageMigrationDialogPersist() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .rotateToLandscape(new StorageMigrationDialogPage(main))
                .rotateToPortrait(new StorageMigrationDialogPage(main));
    }
}
