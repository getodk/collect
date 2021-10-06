package org.odk.collect.android.feature.smoke;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.CollectTestRule;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TestRuleChain;

@RunWith(AndroidJUnit4.class)
public class BadServerTest {


    private final CollectTestRule rule = new CollectTestRule(false);
    private final TestDependencies testDependencies = new TestDependencies();


    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies).around(rule);

    @Test
    // The hash from the form list wasn't used for a long time so some server implementations omitted it even though
    // it's required by the spec. Now we explicitly show an error.
    public void whenHashNotIncludedInFormList_showError() {
        testDependencies.server.removeHashInFormList();
        testDependencies.server.addForm("One Question", "one-question", "1", "one-question.xml");

        rule.withProject(testDependencies.server.getURL())
                .clickGetBlankForm()
                .clickGetSelected()
                .assertMessage("1 of 1 downloads failed!")
                .showDetails()
                .assertError("The server did not provide a hash for this form. If you keep having this problem, report it to the person who asked you to collect data.")
                .navigateBack();
    }
}
