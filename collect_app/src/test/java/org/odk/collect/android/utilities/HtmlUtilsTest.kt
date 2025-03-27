package org.odk.collect.android.utilities

import androidx.test.ext.junit.runners.AndroidJUnit4
import org.hamcrest.CoreMatchers.equalTo
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HtmlUtilsTest {
    @Test
    fun `#textToHtml null becomes empty string`() {
        val observed = HtmlUtils.textToHtml(null)
        assertThat(observed, equalTo(""))
    }

    @Test
    fun `#textToHtml should be trimmed`() {
        val observed = HtmlUtils.textToHtml("<p style=\"text-align:center\">Text</p>")
        assertThat(observed.toString(), equalTo("Text"))
    }

    @Test
    fun `#textToHtml ignores invalid styles`() {
        var observed = HtmlUtils.textToHtml("<p style=>Text</p>")
        assertThat(observed.toString(), equalTo("Text"))

        observed = HtmlUtils.textToHtml("<span style=\"color:\">Text</span>")
        assertThat(observed.toString(), equalTo("Text"))

        observed = HtmlUtils.textToHtml("<span style=\"font-family\">Text</span>")
        assertThat(observed.toString(), equalTo("Text"))
    }

    @Test
    fun `#markDownToHtml escapes backslash`() {
        val tests = arrayOf(
            arrayOf("A\\_B\\_C", "A_B_C"),
            arrayOf("_A\\_B\\_C_", "<em>A_B_C</em>"),
            arrayOf("A_B\\_C", "A_B_C"),
            arrayOf("A\\_B_C", "A_B_C"),
            arrayOf("A_B_C", "A_B_C"),
            arrayOf("A _B_ C", "A <em>B</em> C"),
            arrayOf("A_B_ C", "A_B_ C"),
            arrayOf("A _B_C", "A _B_C"),
            arrayOf("_A_5", "_A_5"),
            arrayOf("_A_", "<em>A</em>"),
            arrayOf("(_A_)", "(<em>A</em>)"),
            arrayOf("_A_?", "<em>A</em>?"),
            arrayOf("*_A_", "*<em>A</em>"),
            arrayOf("blah _A_!", "blah <em>A</em>!"),
            arrayOf(" _A_! blah", " <em>A</em>! blah"),
            arrayOf("\\_ _AB\\__", "_ <em>AB_</em>"),
            arrayOf("\\\\ _AB\\_\\\\_", "\\ <em>AB_\\</em>"),
            arrayOf("A\\*B\\*C", "A*B*C"),
            arrayOf("*A\\*B\\*C*", "<em>A*B*C</em>"),
            arrayOf("A*B\\*C", "A*B*C"),
            arrayOf("A*B*C", "A<em>B</em>C"),
            arrayOf("\\**AB\\**", "*<em>AB*</em>"),
            arrayOf("\\\\*AB\\*\\\\*", "\\<em>AB*\\</em>"),
            arrayOf("\\a\\ b\\*c\\d\\_e", "\\a\\ b*c\\d_e"),
            arrayOf("\\#1", "#1"),
            arrayOf("\\#\\# 2", "## 2"),
            arrayOf("works \\#when not required too", "works #when not required too"),
            arrayOf("\\", "\\"),
            arrayOf("\\\\", "\\"),
            arrayOf("\\\\\\", "\\\\")
        )

        for (testCase in tests) {
            assertThat(testCase[1], equalTo(HtmlUtils.markdownToHtml(testCase[0])))
        }
    }

    @Test
    fun `#markDownToHtml escapes less than`() {
        val tests = arrayOf(
            arrayOf("<1", "&lt;1"),
            arrayOf("<1>", "&lt;1>"),
            arrayOf("< span>", "&lt; span>"),
            arrayOf("< 1", "&lt; 1"),
            arrayOf("< 1/>", "&lt; 1/>"),
            arrayOf("test< 1/>", "test&lt; 1/>"),
            arrayOf("test < 1/>", "test &lt; 1/>")
        )
        for (testCase in tests) {
            assertThat(testCase[1], equalTo(HtmlUtils.markdownToHtml(testCase[0])))
        }
    }

    @Test
    fun `#markDownToHtml supports HTML`() {
        val tests = arrayOf(
            "<span",
            "<span>",
            "<notarealtag>",
            "<CAPSTAG",
            "</closetag>"
        )
        for (testCase in tests) {
            assertThat(testCase, equalTo(HtmlUtils.markdownToHtml(testCase)))
        }
    }

    @Test
    fun `#textToHtml supports escaped lt`() {
        val tests = arrayOf(
            "<1",
        )

        for (testCase in tests) {
            assertThat(testCase, equalTo(HtmlUtils.textToHtml(testCase).toString()))
        }
    }
}
