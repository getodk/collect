package org.odk.collect.android.utilities;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.odk.collect.android.TestSettingsProvider;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.keys.ProjectKeys.KEY_FONT_SIZE;

@RunWith(AndroidJUnit4.class)
public class QuestionFontSizeUtilsTest {

    @Test
    public void whenFontSizeNotSpecified_shouldReturnDefaultValue() {
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
    }

    @Test
    public void whenFontSizeSpecified_shouldReturnSelectedValue() {
        TestSettingsProvider.getGeneralSettings().save(KEY_FONT_SIZE, "30");
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(30));
    }
}
