package org.odk.collect.android.regression;

import android.Manifest;
import androidx.test.rule.GrantPermissionRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.support.pages.FormEntryPage;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.CopyFormRule;
import org.odk.collect.android.support.ResetStateRule;


// Issue number NODK-219
@RunWith(AndroidJUnit4.class)
public class SpinnerWidgetTest extends BaseRegressionTest {

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(GrantPermissionRule.grant(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_PHONE_STATE)
            )
            .around(new ResetStateRule())
            .around(new CopyFormRule("selectOneMinimal.xml"));

    @Test
    public void spinnerList_ShouldDisplay() {
        new MainMenuPage(rule)
                .startBlankForm("selectOneMinimal")
                .clickOnString(R.string.select_one)
                .clickOnAreaWithIndex("TextView", 2)
                .clickOnText("c")
                .assertText("c")
                .checkIfTextDoesNotExist("a")
                .checkIfTextDoesNotExist("b")
                .pressBack(new FormEntryPage("selectOneMinimal", rule))
                .swipeToEndScreen()
                .clickSaveAndExit();
    }
}
