package org.odk.collect.android.feature.smoke;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.odk.collect.android.support.TestDependencies;
import org.odk.collect.android.support.TranslatedStringBuilder;
import org.odk.collect.android.support.pages.MainMenuPage;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;

import java.util.Arrays;

@RunWith(AndroidJUnit4.class)
public class BadServerTest {

    private final CollectTestRule rule = new CollectTestRule(false);
    private final TestDependencies testDependencies = new TestDependencies();


    @Rule
    public RuleChain chain = TestRuleChain.chain(testDependencies).around(rule);

    @Test
    /*
     The hash from the form list wasn't used for a long time so some server implementations
     omitted it even though it's required by the spec. Now we explicitly show an error.
    */
    public void whenHashNotIncludedInFormList_showError() {
        testDependencies.server.removeHashInFormList();
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml");

        rule.withProject(testDependencies.server.getURL())
                .clickGetBlankForm()
                .clickGetSelected()
                .assertMessage("1 of 1 downloads failed!")
                .showDetails()
                .assertError(new TranslatedStringBuilder()
                        .addString(org.odk.collect.strings.R.string.form_with_no_hash_error)
                        .addString(org.odk.collect.strings.R.string.report_to_project_lead)
                        .build()
                )
                .navigateBack();
    }

    @Test
    /*
     A server that doesn't return hashes correctly for media files would fool Collect into thinking
     there was a new one each time. This happens because we would compare the existing file's
     (computed) hash with the server one assuming there is a prefix and end up comparing against
     a substring.
    */
    public void whenMediaFileHasMissingPrefix_showsAsUpdated() {
        testDependencies.server.removeMediaFileHashPrefix();
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml", Arrays.asList("fruits.csv"));

        rule.withProject(testDependencies.server.getURL())
                .copyForm("one-question.xml", Arrays.asList("fruits.csv"), testDependencies.server.getHostName())
                .clickGetBlankForm()
                .assertText(org.odk.collect.strings.R.string.newer_version_of_a_form_info);
    }

    @Test
    /*
     A server that doesn't return hashes based on the md5 of the file for media files
     would fool Collect into thinking there was a new one each time. Collect should still redownload
     the file in this case (there's nothing else it can do), but it should only identify the form
     as being updated if the file actually changed.
    */
    public void whenMediaFileHasUnstableHash_butIsIdentical_doesNotShowAsUpdatedAfterRedownload() {
        testDependencies.server.returnRandomMediaFileHash();
        testDependencies.server.addForm("One Question", "one_question", "1", "one-question.xml", Arrays.asList("fruits.csv"));

        rule.withProject(testDependencies.server.getURL())
                .copyForm("one-question.xml", Arrays.asList("fruits.csv"), testDependencies.server.getHostName())
                .clickGetBlankForm()
                .assertText(org.odk.collect.strings.R.string.newer_version_of_a_form_info)
                .clickGetSelected()
                .clickOKOnDialog(new MainMenuPage())
                .clickFillBlankForm()
                .assertTextThatContainsDoesNoExist("Updated on")
                .assertTextThatContainsExists("Added on");
    }
}
