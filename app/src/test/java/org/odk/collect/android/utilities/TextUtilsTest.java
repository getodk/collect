package org.odk.collect.android.utilities;

import static org.junit.Assert.assertNull;

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
        assertNull(observed);
    }

}
