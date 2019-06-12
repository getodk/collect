package org.odk.collect.android.regression;

import androidx.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;

import static androidx.test.espresso.Espresso.pressBack;


// Issue number NODK-238
@RunWith(AndroidJUnit4.class)
public class UserAndDeviceIdentityTest extends BaseFormTest {

    @Test
    public void setEmail_ShouldRequireAtSign() {

        //TestCase1
        EspressoTestUtilities.resetSettings();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.clickUserAndDeviceIdentity();
        EspressoTestUtilities.clickFormMetadata();
        EspressoTestUtilities.clickMetadataEmail();
        EspressoTestUtilities.putText("aabb");
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.ok);
        EspressoTestUtilities.checkIsToastWithStringDisplayes(R.string.invalid_email_address, main);
        EspressoTestUtilities.clickMetadataEmail();
        EspressoTestUtilities.putText("aa@bb");
        pressBack();
        EspressoTestUtilities.clickOnString(R.string.ok);
        EspressoTestUtilities.checkIsTextDisplayed("aa@bb");
        pressBack();
        pressBack();
        pressBack();

    }

    @Test
    public void emptyUsername_ShouldNotDisplayUsernameInForm() {

        //TestCase2
        EspressoTestUtilities.resetSettings();
        EspressoTestUtilities.startBlankForm("Test");
        EspressoTestUtilities.checkIsDisplayedInTextClassAndSwipe("");
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void setMetadataUsername_ShouldDisplayMetadataUsernameInForm() {

        //TestCase3
        EspressoTestUtilities.resetSettings();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.clickUserAndDeviceIdentity();
        EspressoTestUtilities.clickFormMetadata();
        EspressoTestUtilities.clickMetadataUsername();
        EspressoTestUtilities.putText("AAA");
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        EspressoTestUtilities.startBlankForm("Test");
        EspressoTestUtilities.checkIsDisplayedInTextClassAndSwipe("AAA");
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void setAggregateUsername_ShouldDisplayAggregateUsernameInForm() {

        //TestCase4
        EspressoTestUtilities.resetSettings();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.clickUserAndDeviceIdentity();
        EspressoTestUtilities.clickFormMetadata();
        EspressoTestUtilities.clickMetadataUsername();
        EspressoTestUtilities.putText("");
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.openServerSettings();
        EspressoTestUtilities.clickOnServerType();
        EspressoTestUtilities.clickOnString(R.string.server_platform_odk_aggregate);
        EspressoTestUtilities.clickAggregateUsername();
        EspressoTestUtilities.putText("BBB");
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        EspressoTestUtilities.startBlankForm("Test");
        EspressoTestUtilities.checkIsDisplayedInTextClassAndSwipe("BBB");
        EspressoTestUtilities.clickSaveAndExit();

    }

    @Test
    public void setBothUsernames_ShouldDisplayMetadataUsernameInForm() {

        //TestCase5
        EspressoTestUtilities.resetSettings();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.clickUserAndDeviceIdentity();
        EspressoTestUtilities.clickFormMetadata();
        EspressoTestUtilities.clickMetadataUsername();
        EspressoTestUtilities.putText("CCC");
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        pressBack();
        EspressoTestUtilities.clickOnMenu();
        EspressoTestUtilities.clickGeneralSettings();
        EspressoTestUtilities.openServerSettings();
        EspressoTestUtilities.clickOnServerType();
        EspressoTestUtilities.clickOnString(R.string.server_platform_odk_aggregate);
        EspressoTestUtilities.clickAggregateUsername();
        EspressoTestUtilities.putText("DDD");
        EspressoTestUtilities.clickOnString(R.string.ok);
        pressBack();
        pressBack();
        EspressoTestUtilities.startBlankForm("Test");
        EspressoTestUtilities.checkIsDisplayedInTextClassAndSwipe("CCC");
        EspressoTestUtilities.clickSaveAndExit();

    }
}