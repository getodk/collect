package org.odk.collect.android.widgets.base;

import static junit.framework.Assert.assertTrue;

import android.view.View;

import org.javarosa.core.model.data.IAnswerData;
import org.junit.Test;
import org.odk.collect.android.R;
import org.odk.collect.android.support.WidgetTestActivity;
import org.odk.collect.android.widgets.ExStringWidget;
import org.odk.collect.android.widgets.StringWidget;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.when;

import java.util.List;

/**
 * @author James Knight
 */

public abstract class GeneralExStringWidgetTest<W extends ExStringWidget, A extends IAnswerData> extends BinaryWidgetTest<W, A> {

    @Override
    public Object createBinaryData(A answerData) {
        return answerData.getDisplayText();
    }

    // TODO we should have such tests for every widget like we have to confirm readOnly option
    @Test
    public void testElementsVisibilityAndAvailability() {
        assertThat(getSpyWidget().launchIntentButton.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().launchIntentButton.isEnabled(), is(Boolean.TRUE));
        assertThat(getSpyWidget().answerText.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().answerText.isEnabled(), is(Boolean.FALSE));
    }

    @Test
    public void usingReadOnlyOptionShouldMakeAllClickableElementsDisabled() {
        when(formEntryPrompt.isReadOnly()).thenReturn(true);

        assertThat(getSpyWidget().launchIntentButton.getVisibility(), is(View.GONE));
        assertThat(getSpyWidget().answerText.getVisibility(), is(View.VISIBLE));
        assertThat(getSpyWidget().answerText.isEnabled(), is(Boolean.FALSE));
    }

    /**
     * Unlike other widgets, String widgets that contain EditText should not be registered to
     * context menu as a whole because the Clipboard menu would be broken.
     *
     * https://github.com/getodk/collect/pull/4860
     */
    @Test
    public void widgetShouldBeRegisteredForContextMenu() {
        StringWidget widget = createWidget();
        List<View> viewsRegisterForContextMenu = ((WidgetTestActivity) activity).viewsRegisterForContextMenu;

        assertThat(viewsRegisterForContextMenu.size(), is(3));

        assertTrue(viewsRegisterForContextMenu.contains(widget.findViewWithTag(R.id.question_label)));
        assertTrue(viewsRegisterForContextMenu.contains(widget.findViewWithTag(R.id.help_text)));
        assertTrue(viewsRegisterForContextMenu.contains(widget.findViewWithTag(R.id.space_box)));

        assertThat(viewsRegisterForContextMenu.get(0).getId(), is(widget.getId()));
        assertThat(viewsRegisterForContextMenu.get(1).getId(), is(widget.getId()));
        assertThat(viewsRegisterForContextMenu.get(2).getId(), is(widget.getId()));
    }
}
