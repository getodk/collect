package org.odk.collect.android.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

@RunWith(RobolectricTestRunner.class)
public class QuestionFontSizeProviderTest {

    @Test
    public void whenFontSizeNotSpecified_shouldReturnDefaultValue() {
        assertThat(QuestionFontSizeProvider.getQuestionFontSize(), is(QuestionFontSizeProvider.DEFAULT_FONT_SIZE));
    }

    @Test
    public void whenFontSizeSpecified_shouldReturnSelectedValue() {
        GeneralSharedPreferences.getInstance().save(KEY_FONT_SIZE, "30");
        assertThat(QuestionFontSizeProvider.getQuestionFontSize(), is(30));
    }
}
