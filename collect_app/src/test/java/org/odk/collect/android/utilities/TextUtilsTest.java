package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


@RunWith(RobolectricTestRunner.class)
public class TextUtilsTest {

    /**
     * Should return null if provided with null and not throw a NPE.
     */
    @Test
    public void textToHtml_BounceNullInput() {
        String input = null;
        CharSequence observed = TextUtils.textToHtml(input);
        assertNull(observed);
    }

    /**
     * Should do something not-null-ish if provided with non-null string.
     */
    @Test
    public void textToHtml_CallsHTMLOnRealInput() {
        String input = "This *bold* markdown is \n- nice,\n- efficient.\n";
        CharSequence result = TextUtils.textToHtml(input);
        assertNotNull(result);
    }
}
