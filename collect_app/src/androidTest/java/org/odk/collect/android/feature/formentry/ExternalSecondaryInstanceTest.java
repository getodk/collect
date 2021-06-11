package org.odk.collect.android.feature.formentry;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.ResetStateRule;
import org.odk.collect.android.support.pages.MainMenuPage;

import java.util.Collections;

/**
 * This tests the "Multiple choice from file" feature of XLSForms which is also referred to as just
 * "external datasets" in ODK docs and "External secondary instances" in XForm docs.
 *
 * @see <a href="https://xlsform.org/en/#multiple-choice-from-file">Multiple choice from file</a>
 * @see <a href="https://docs.getodk.org/form-datasets/">Form datasets</a>
 * @see <a href="https://getodk.github.io/xforms-spec/#secondary-instances---external">External secondary  instances</a>
 */

// Issue number NODK-377
@RunWith(AndroidJUnit4.class)
public class ExternalSecondaryInstanceTest {

    public CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = RuleChain
            .outerRule(new ResetStateRule())
            .around(rule);

    @Test
    public void displaysAllOptionsFromSecondaryInstance() {
        //TestCase1
        new MainMenuPage()
                .copyForm("external_select_10.xml", Collections.singletonList("external_data_10.xml"))
                .startBlankForm("external select 10")
                .clickOnText("a")
                .swipeToNextQuestion("Second")
                .assertText("aa")
                .assertText("ab")
                .assertText("ac");
    }
}
