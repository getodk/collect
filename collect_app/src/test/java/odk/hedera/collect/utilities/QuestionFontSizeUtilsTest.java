package odk.hedera.collect.utilities;

import org.junit.Test;
import org.junit.runner.RunWith;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static odk.hedera.collect.preferences.GeneralKeys.KEY_FONT_SIZE;

@RunWith(RobolectricTestRunner.class)
public class QuestionFontSizeUtilsTest {

    @Test
    public void whenFontSizeNotSpecified_shouldReturnDefaultValue() {
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
    }

    @Test
    public void whenFontSizeSpecified_shouldReturnSelectedValue() {
        GeneralSharedPreferences.getInstance().save(KEY_FONT_SIZE, "30");
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(30));
    }
}
