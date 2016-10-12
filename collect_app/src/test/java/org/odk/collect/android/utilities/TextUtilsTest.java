package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


@RunWith(RobolectricTestRunner.class)
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
        assertNull(observed);
    }

    /**
     * Should convert Markdown to HTML.
     *
     * In the real app this call would produce the following string, but
     * because the implementation uses android.Html.fromHtml, we end up with
     * robolectric's implementation of fromHtml which isn't exactly the same.
     * See opendatakit/collect#204.
     *
     * <p>This <em>bold</em> markdown is</p><p>- nice,</p><p>- efficient.</p>
     *
     * The main thing is that it's clear this interface processes Markdown to
     * HTML, which is evident from the removal of the stars on *bold* and
     * doubling of line returns (which is due to \n to <p></p> conversion).
     */
    @Test
    public void textToHtml_ConvertsMarkdownToHtml() {
        String input = "This *bold* markdown is \n- nice,\n- efficient.\n";
        String expected = "This bold markdown is\n\n- nice,\n\n- efficient.\n\n";
        CharSequence actual = TextUtils.textToHtml(input);
        assertEquals(expected, actual.toString());
    }
}
