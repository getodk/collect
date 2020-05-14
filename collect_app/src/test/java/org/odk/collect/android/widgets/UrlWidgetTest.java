package org.odk.collect.android.widgets;

import android.view.View;
import android.widget.Button;

import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.odk.collect.android.R;
import org.odk.collect.android.formentry.questions.QuestionDetails;
import org.robolectric.RobolectricTestRunner;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.promptWithReadOnly;
import static org.odk.collect.android.widgets.support.QuestionWidgetHelpers.widgetTestActivity;

/**
 * @author James Knight
 */

@RunWith(RobolectricTestRunner.class)
public class UrlWidgetTest {

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        UrlWidget widget = createWidget(promptWithReadOnly());
        Button urlButton = widget.findViewById(R.id.url_button);
        assertThat(urlButton.getVisibility(), equalTo(View.GONE));
    }

    private UrlWidget createWidget(FormEntryPrompt prompt) {
        return new UrlWidget(widgetTestActivity(), new QuestionDetails(prompt, "formAnalyticsID"));
    }
}
