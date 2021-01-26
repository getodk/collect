package org.odk.collect.android.feature.storage;

import androidx.test.espresso.intent.rule.IntentsTestRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.application.AppStateProvider;
import org.odk.collect.android.storage.StorageStateProvider;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.pages.StorageMigrationDialogPage;

import static java.util.Arrays.asList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class StorageMigrationTest {

    final TestDependencies testDependencies = new TestDependencies() {
        @Override
        public StorageStateProvider providesStorageStateProvider() {
            StorageStateProvider storageStateProvider = spy(new StorageStateProvider());
            when(storageStateProvider.shouldPerformAutomaticMigration()).thenReturn(false);
            return storageStateProvider;
        }

        @Override
        public AppStateProvider providesAppStateProvider() {
            AppStateProvider appStateProvider = spy(new AppStateProvider());
            when(appStateProvider.isFreshInstall(any())).thenReturn(false);
            return appStateProvider;
        }
    };

    final IntentsTestRule<MainMenuActivity> rule = new IntentsTestRule<>(MainMenuActivity.class);

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain(false, testDependencies)
            .around(new CopyFormRule("formWithExternalFiles.xml", asList("formWithExternalFiles-media/itemsets.csv", "formWithExternalFiles-media/fruits.xml", "formWithExternalFiles-media/fruits.csv", "formWithExternalFiles-media/last-saved.xml"), true))
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

