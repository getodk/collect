package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankInvalidFormTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(Manifest.permission.READ_PHONE_STATE))
            .around(new ResetStateRule())
            .around(new CopyFormRule("invalid-events.xml"))
            .around(new CopyFormRule("invalid-form.xml"))
            .around(new CopyFormRule("setlocation-and-audit-location.xml"))
            .around(new CopyFormRule("setlocation-action-instance-load.xml"))
            .around(rule);


    @Test
    public void brokenForms_shouldNotBeVisibleOnFOrmList() {
        //TestCase53
        new MainMenuPage()
                .clickFillBlankForm()
                .checkIsSnackbarErrorVisible()
                .assertTextDoesNotExist("Invalid events")
                .assertTextDoesNotExist("invalid-form")
                .assertTextDoesNotExist("setlocation-and-audit-location")
                .assertTextDoesNotExist("setlocation-action-instance-load");
    }

}
