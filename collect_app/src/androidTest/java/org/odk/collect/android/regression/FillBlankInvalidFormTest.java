package org.odk.collect.android.regression;

import android.Manifest;

import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

//Issue NODK-244
@RunWith(AndroidJUnit4.class)
public class FillBlankInvalidFormTest extends BaseRegressionTest {
    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("invalid-events.xml"))
            .around(new CopyFormRule("invalid-form.xml"))
            .around(new CopyFormRule("setlocation-and-audit-location.xml"))
            .around(new CopyFormRule("setlocation-action-instance-load.xml"));


    @Test
    public void brokenForms_shouldNotBeVisibleOnFOrmList() {
        //TestCase53
        new MainMenuPage(rule)
                .clickFillBlankForm()
                .checkIsSnackbarErrorVisible()
                .checkIfTextDoesNotExist("Invalid events")
                .checkIfTextDoesNotExist("invalid-form")
                .checkIfTextDoesNotExist("setlocation-and-audit-location")
                .checkIfTextDoesNotExist("setlocation-action-instance-load");
    }

}