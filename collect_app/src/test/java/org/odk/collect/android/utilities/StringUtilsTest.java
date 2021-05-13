package org.odk.collect.android.utilities;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
public class StringUtilsTest {

    @Test
    public void textToHtml_nullBecomesEmptyString() {
        CharSequence observed = StringUtils.textToHtml(null);
        assertThat(observed, equalTo(""));
    }

    @Test
    public void textToHtml_shouldBeTrimmed() {
        CharSequence observed = StringUtils.textToHtml("<p style=\"text-align:center\">Text</p>");
        assertThat(observed.toString(), equalTo("Text"));
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
            assertEquals(testCase[1], StringUtils.markdownToHtml(testCase[0]));
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
            assertEquals(testCase[1], StringUtils.markdownToHtml(testCase[0]));
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
            assertEquals(testCase, StringUtils.markdownToHtml(testCase));
        }
    }

    @Test
    public void textToHtml_SupportsEscapedLt() {
        String[] tests = {
                "<1",
        };

        for (String testCase: tests) {
            assertEquals(testCase, StringUtils.textToHtml(testCase).toString());
        }
    }

    @Test
    public void ellipsizeBeginningTest() {
        //50 chars
        assertEquals("Lorem ipsum dolor sit amet, consectetur massa nunc",
                StringUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur massa nunc"));
        //100 chars
        assertEquals("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis",
                StringUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Integer tempus, risus ac cursus turpis duis"));
        //101 chars
        assertEquals("...m ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam",
                StringUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Cras finibus, augue a imperdiet orci aliquam"));
        //150 chars
        assertEquals("...it. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam",
                StringUtils.ellipsizeBeginning("Lorem ipsum dolor sit amet, consectetur adipiscing elit. Donec cursus condimentum sagittis. Ut condimentum efficitur libero, vitae volutpat dui nullam"));
    }

    @Test
    public void whenStringIsJustWhitespace_returnsTrue() {
        assertTrue(StringUtils.isBlank(" "));
    }

    @Test
    public void whenStringContainsWhitespace_returnsFalse() {
        assertFalse(StringUtils.isBlank(" hello "));
    }

    @Test
    public void whenCharSequenceContainWhitespaces_shouldTrimReturnTrimmedCharSequence() {
        CharSequence result = StringUtils.trim("\n\t <p style=\"text-align:center\">Text</p> \t\n");
        assertThat(result, equalTo("<p style=\"text-align:center\">Text</p>"));
    }

    @Test
    public void whenCharSequenceContainOnlyWhitespaces_shouldTrimReturnOriginalCharSequence() {
        CharSequence result = StringUtils.trim("\n\t \t\n");
        assertThat(result, equalTo("\n\t \t\n"));
    }

    @Test
    public void whenCharSequenceIsNull_shouldTrimReturnNull() {
        CharSequence result = StringUtils.trim(null);
        assertThat(result, equalTo(null));
    }

    @Test
    public void whenCharSequenceIsEmpty_shouldTrimReturnEmptyCharSequence() {
        CharSequence result = StringUtils.trim("");
        assertThat(result, equalTo(""));
    }
}
