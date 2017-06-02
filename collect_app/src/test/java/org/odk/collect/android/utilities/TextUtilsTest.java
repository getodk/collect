package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = "src/main/AndroidManifest.xml",
        packageName = "org.odk.collect")
public class TextUtilsTest {

    /**
     * Should return null if provided with null and not throw a NPE.
     *
     * This is a silly test but it is here to guarantee this behaviour,
     * since without it the method causes a crash when processing text for
     * questions with no plain text label. See opendatakit/opendatakit#1247.
     */
    @Test
    public void textToHtml_BouncesNullInput() {
        String input = null;
        CharSequence observed = TextUtils.textToHtml(input);
        Assert.assertNull(observed);
    }

    @Test
    public void markDownToHtml_EscapesLessThan() {
        String[][] tests = {
            {"<1", "&lt;1"},
            {"<1>", "&lt;1>"},
            {"< span>", "&lt; span>"},
            {"< 1", "&lt; 1"},
            {"< 1/>", "&lt; 1/>"},
            {"test< 1/>", "test&lt; 1/>"},
            {"test < 1/>", "test &lt; 1/>"}
        };
        for (String[] testCase: tests) {
            Assert.assertEquals(testCase[1], TextUtils.markdownToHtml(testCase[0]));
        }
    }

    @Test
    public void markDownToHtml_SupportsHtml() {
        String[] tests = {
                "<span",
                "<span>",
                "<notarealtag>",
                "<CAPSTAG",
                "</closetag>"
        };
        for (String testCase: tests) {
            Assert.assertEquals(testCase, TextUtils.markdownToHtml(testCase));
        }
    }

    @Test
    public void textToHtml_SupportsEscapedLt() {
        String[] tests = {
                "<1",
        };

        for (String testCase: tests) {
            Assert.assertEquals(testCase, TextUtils.textToHtml(testCase).toString());
        }
    }

}
