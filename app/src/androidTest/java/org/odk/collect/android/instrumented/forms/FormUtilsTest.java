package org.odk.collect.android.instrumented.forms;

import org.javarosa.core.reference.RootTranslator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.odk.collect.android.support.rules.CollectTestRule;
import org.odk.collect.android.support.rules.TestRuleChain;
import org.odk.collect.android.utilities.FormUtils;
import org.odk.collect.android.storage.StoragePathProvider;
import org.odk.collect.android.storage.StorageSubdirectory;
import org.odk.collect.android.tasks.FormLoaderTask;
import org.odk.collect.android.utilities.FileUtils;

import java.io.File;
import java.util.List;

public class FormUtilsTest {
    private static final String BASIC_FORM = "basic.xml";
    private final CollectTestRule rule = new CollectTestRule();

    @Rule
    public RuleChain copyFormChain = TestRuleChain.chain()
            .around(rule);

    @Before
    public void setUp() {
        rule.startAtFirstLaunch()
                .copyForm(BASIC_FORM);
    }

    /* Verify that each host string matches only a single root translator, allowing for them to
     be defined in any order. See: https://github.com/getodk/collect/issues/3334
    */
    @Test
    public void sessionRootTranslatorOrderDoesNotMatter() throws Exception {
        final String formPath = new StoragePathProvider().getOdkDirPath(StorageSubdirectory.FORMS) + File.separator + BASIC_FORM;
        // Load the form in order to populate the ReferenceManager
        FormLoaderTask formLoaderTask = new FormLoaderTask(formPath, null, null);
        formLoaderTask.execute(formPath).get();

        final File formXml = new File(formPath);
        final File formMediaDir = FileUtils.getFormMediaDir(formXml);
        List<RootTranslator> rootTranslators = FormUtils.buildSessionRootTranslators(formMediaDir.getName(), FormUtils.enumerateHostStrings());

        // Check each type of host string to determine that only one match is resolved.
        for (String hostString : FormUtils.enumerateHostStrings()) {
            String uri = String.format("jr://%s/test", hostString);
            int matchCount = 0;
            for (RootTranslator rootTranslator : rootTranslators) {
                if (rootTranslator.derives(uri)) {
                    matchCount++;
                }
            }
            Assert.assertEquals("Expected only a single match for URI: " + uri, 1, matchCount);
        }
    }

    /* Verify that the host strings appear in an order that does not allow for greedy matches, e.g.
     matching 'file' instead of 'file-csv'. According to the behavior in the test above,
     sessionRootTranslatorOrderDoesNotMatter, it is not actually a requirement to have the test
     below pass. This simply follows the cautionary remarks in the following issue:
     https://github.com/getodk/collect/issues/3334
     */
    @Test
    public void hostStringsOrderedCorrectly() throws Exception {
        String[] hostStrings = FormUtils.enumerateHostStrings();
        // No host string should be a substring of the subsequent ones.
        for (int i = 0; i < hostStrings.length; ++i) {
            String currentHostString = hostStrings[i];
            for (int j = i + 1; j < hostStrings.length; ++j) {
                String subsequentHostString = hostStrings[j];
                Assert.assertFalse(subsequentHostString.contains(currentHostString));
            }
        }
    }
}
