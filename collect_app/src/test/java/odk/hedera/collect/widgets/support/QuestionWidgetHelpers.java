package odk.hedera.collect.widgets.support;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import odk.hedera.collect.listeners.WidgetValueChangedListener;
import org.odk.hedera.collect.support.MockFormEntryPromptBuilder;
import org.odk.hedera.collect.support.RobolectricHelpers;
import org.odk.hedera.collect.support.TestScreenContextActivity;
import odk.hedera.collect.widgets.TriggerWidget;

import static org.mockito.Mockito.mock;

public class QuestionWidgetHelpers {

    private QuestionWidgetHelpers() {

    }

    public static TestScreenContextActivity widgetTestActivity() {
        return RobolectricHelpers.buildThemedActivity(TestScreenContextActivity.class).get();
    }

    public static WidgetValueChangedListener mockValueChangedListener(TriggerWidget widget) {
        WidgetValueChangedListener valueChangedListener = mock(WidgetValueChangedListener.class);
        widget.setValueChangedListener(valueChangedListener);
        return valueChangedListener;
    }

    public static FormEntryPrompt promptWithAnswer(IAnswerData answer) {
        return new MockFormEntryPromptBuilder()
                .withAnswer(answer)
                .build();
    }

    public static FormEntryPrompt promptWithReadOnly() {
        return new MockFormEntryPromptBuilder()
                .withReadOnly(true)
                .build();
    }
}
