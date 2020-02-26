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
import org.odk.collect.android.support.pages.StorageMigrationDialogPage;

public class StorageMigrationDialogTest {

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
    public void when_thereAreNoSavedForms_should_thePromptToSubmitFormsBeNotVisible() {
        new MainMenuPage(main)
                .clickLearnMoreButton()
                .assertStorageMigrationContentWithoutSavedFormsIsVisible();
    }

    @Test
    public void when_savedFormsExist_should_thePromptToSubmitFormsBeVisible() {
        new MainMenuPage(main)
                .startBlankForm("basic")
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
