package org.odk.collect.android.utilities;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertEquals;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class HtmlUtilsTest {

    @Test
    public void textToHtml_nullBecomesEmptyString() {
        CharSequence observed = HtmlUtils.textToHtml(null);
        assertThat(observed, equalTo(""));
    }

    @Test
    public void textToHtml_shouldBeTrimmed() {
        CharSequence observed = HtmlUtils.textToHtml("<p style=\"text-align:center\">Text</p>");
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
            assertEquals(testCase[1], HtmlUtils.markdownToHtml(testCase[0]));
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
            assertEquals(testCase[1], HtmlUtils.markdownToHtml(testCase[0]));
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
            assertEquals(testCase, HtmlUtils.markdownToHtml(testCase));
        }
    }

    @Test
    public void textToHtml_SupportsEscapedLt() {
        String[] tests = {
                "<1",
        };

        for (String testCase: tests) {
            assertEquals(testCase, HtmlUtils.textToHtml(testCase).toString());
        }
    }
}
