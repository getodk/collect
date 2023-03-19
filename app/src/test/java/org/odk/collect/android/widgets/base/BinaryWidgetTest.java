package org.odk.collect.android.widgets.base;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.fakes.FakePermissionsProvider;
import org.odk.collect.android.widgets.QuestionWidget;
import org.odk.collect.android.widgets.interfaces.WidgetDataReceiver;
import org.odk.collect.android.widgets.interfaces.Widget;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

/**
 * @author James Knight
 */
public abstract class BinaryWidgetTest<W extends Widget, A extends IAnswerData>
        extends QuestionWidgetTest<W, A> {

    private final FakePermissionsProvider permissionsProvider;

    public BinaryWidgetTest() {
        permissionsProvider = new FakePermissionsProvider();
    }

    protected void stubAllRuntimePermissionsGranted(boolean isGranted) {
        permissionsProvider.setPermissionGranted(isGranted);
        ((QuestionWidget) getWidget()).setPermissionsProvider(permissionsProvider);
    }

    protected Intent getIntentLaunchedByClick(int buttonId) {
        ((QuestionWidget) getSpyWidget()).findViewById(buttonId).performClick();
        return shadowOf(activity).getNextStartedActivity();
    }

    protected void assertComponentEquals(String pkg, String cls, Intent intent) {
        assertEquals(new ComponentName(pkg, cls), intent.getComponent());
    }

    protected void assertComponentEquals(Context context, Class<?> cls, Intent intent) {
        assertEquals(new ComponentName(context, cls), intent.getComponent());
    }

    protected void assertActionEquals(String expectedAction, Intent intent) {
        assertEquals(expectedAction, intent.getAction());
    }

    protected void assertTypeEquals(String type, Intent intent) {
        assertEquals(type, intent.getType());
    }

    protected void assertExtraEquals(String key, Object value, Intent intent) {
        assertEquals(intent.getExtras().get(key), value);
    }

    public abstract Object createBinaryData(A answerData);

    @Test
    public void getAnswerShouldReturnCorrectAnswerAfterBeingSet() {
        W widget = getSpyWidget();
        assertNull(widget.getAnswer());

        A answer = getNextAnswer();
        Object binaryData = createBinaryData(answer);

        ((WidgetDataReceiver) widget).setData(binaryData);

        IAnswerData answerData = widget.getAnswer();

        assertNotNull(answerData);
        assertEquals(answerData.getDisplayText(), answer.getDisplayText());
    }

    @Test
    public void settingANewAnswerShouldRemoveTheOldAnswer() {
        A answer = getInitialAnswer();
        when(formEntryPrompt.getAnswerText()).thenReturn(answer.getDisplayText());

        W widget = getSpyWidget();

        A newAnswer = getNextAnswer();
        Object binaryData = createBinaryData(newAnswer);

        ((WidgetDataReceiver) widget).setData(binaryData);

        IAnswerData answerData = widget.getAnswer();

        assertNotNull(answerData);
        assertEquals(answerData.getDisplayText(), newAnswer.getDisplayText());
    }
}