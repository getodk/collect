package org.odk.collect.android.utilities;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.injection.config.AppDependencyComponent;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_FONT_SIZE;

@RunWith(RobolectricTestRunner.class)
public class QuestionFontSizeUtilsTest {

    @Test
    public void whenFontSizeNotSpecified_shouldReturnDefaultValue() {
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(QuestionFontSizeUtils.DEFAULT_FONT_SIZE));
    }

    @Test
    public void whenFontSizeSpecified_shouldReturnSelectedValue() {
        AppDependencyComponent component = DaggerUtils.getComponent(ApplicationProvider.<Collect>getApplicationContext());
        component.preferencesRepository().getGeneralPreferences().save(KEY_FONT_SIZE, "30");
        assertThat(QuestionFontSizeUtils.getQuestionFontSize(), is(30));
    }
}
