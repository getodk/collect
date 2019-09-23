package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TextWidgetUtilsTest {

    private FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

    @Test
    public void getIntegerAnswerValueFromIAnswerDataTest() {
        assertNull(TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(null));
        assertEquals(new Integer(0), TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(0)));
        assertEquals(new Integer(-15), TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(-15)));
        assertEquals(new Integer(15), TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(15)));
    }

    @Test
    public void getDoubleAnswerValueFromIAnswerDataTest() {
        assertNull(TextWidgetUtils.getIntegerAnswerValueFromIAnswerData(null));
        assertEquals(new Double(0), TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(0)));
        assertEquals(new Double(-15), TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(-15)));
        assertEquals(new Double(-15.123), TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(-15.123)));
        assertEquals(new Double(15), TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(15)));
        assertEquals(new Double(15.123), TextWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(15.123)));
    }

    @Test
    public void getIntegerDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(TextWidgetUtils.getIntegerData("", formEntryPrompt));
        assertNull(TextWidgetUtils.getIntegerData("5.5", formEntryPrompt));
        assertEquals("0", TextWidgetUtils.getIntegerData("0", formEntryPrompt).getDisplayText());
        assertEquals("7", TextWidgetUtils.getIntegerData("7", formEntryPrompt).getDisplayText());
        assertEquals("-22", TextWidgetUtils.getIntegerData("-22", formEntryPrompt).getDisplayText());
        assertEquals("1000000", TextWidgetUtils.getIntegerData("1000000", formEntryPrompt).getDisplayText());
    }

    @Test
    public void getDecimalDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(TextWidgetUtils.getDecimalData("", formEntryPrompt));
        assertEquals("0.0", TextWidgetUtils.getDecimalData("0", formEntryPrompt).getDisplayText());
        assertEquals("50.0", TextWidgetUtils.getDecimalData("50", formEntryPrompt).getDisplayText());
        assertEquals("7.75", TextWidgetUtils.getDecimalData("7.75", formEntryPrompt).getDisplayText());
        assertEquals("-22.123", TextWidgetUtils.getDecimalData("-22.123", formEntryPrompt).getDisplayText());
    }

    @Test
    public void getStringNumberDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(TextWidgetUtils.getDecimalData("", formEntryPrompt));
        assertEquals("0", TextWidgetUtils.getStringNumberData("0", formEntryPrompt).getDisplayText());
        assertEquals("50", TextWidgetUtils.getStringNumberData("50", formEntryPrompt).getDisplayText());
        assertEquals("7.75", TextWidgetUtils.getStringNumberData("7.75", formEntryPrompt).getDisplayText());
        assertEquals("-22.123", TextWidgetUtils.getStringNumberData("-22.123", formEntryPrompt).getDisplayText());
    }
}
