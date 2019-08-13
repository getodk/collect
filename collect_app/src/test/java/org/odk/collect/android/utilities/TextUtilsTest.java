package org.odk.collect.android.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class TextUtilsTest {

    /**
     * Should return null if provided with null and not throw a NPE.
     * <p>
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
    public void markDownToHtmlEscapesBackslash() {
        String[][] tests = {
                {"A\\_B\\_C", "A_B_C"},
                {"_A\\_B\\_C_", "<em>A_B_C</em>"},
                {"A_B\\_C", "A_B_C"},
                {"A\\_B_C", "A_B_C"},
                {"A_B_C", "A<em>B</em>C"},
                {"\\__AB\\__", "_<em>AB_</em>"},
                {"\\\\_AB\\_\\\\_", "\\<em>AB_\\</em>"},
                {"A\\*B\\*C", "A*B*C"},
                {"*A\\*B\\*C*", "<em>A*B*C</em>"},
                {"A*B\\*C", "A*B*C"},
                {"A*B*C", "A<em>B</em>C"},
                {"\\**AB\\**", "*<em>AB*</em>"},
                {"\\\\*AB\\*\\\\*", "\\<em>AB*\\</em>"},
                {"\\a\\ b\\*c\\d\\_e", "\\a\\ b*c\\d_e"},
                {"\\#1", "#1"},
                {"\\#\\# 2", "## 2"},
                {"works \\#when not required too", "works #when not required too"},
                {"\\", "\\"},
                {"\\\\", "\\"},
                {"\\\\\\", "\\\\"}};

        for (String[] testCase : tests) {
            assertEquals(testCase[1], TextUtils.markdownToHtml(testCase[0]));
        }
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
            assertEquals(testCase[1], TextUtils.markdownToHtml(testCase[0]));
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
            assertEquals(testCase, TextUtils.markdownToHtml(testCase));
        }
    }

    @Test
    public void textToHtml_SupportsEscapedLt() {
        String[] tests = {
                "<1",
        };

        for (String testCase: tests) {
            assertEquals(testCase, TextUtils.textToHtml(testCase).toString());
        }
    }

    @Test
    public void ellipsizeBeginningTest() {
        //50 chars
        assertEquals("Lorem ipsum dolor sit amet, consectetur massa nunc",
                TextUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur massa nunc"));
        //100 chars
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis",
                TextUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis"));
        //101 chars
        assertEquals("...m ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam",
                TextUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam"));
        //150 chars
        assertEquals("...it. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam",
                TextUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam"));
    }
}
