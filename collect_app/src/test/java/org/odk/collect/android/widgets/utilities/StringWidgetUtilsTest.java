package org.odk.collect.android.widgets.utilities;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class StringWidgetUtilsTest {

    private final FormEntryPrompt formEntryPrompt = mock(FormEntryPrompt.class);

    @Test
    public void getIntegerAnswerValueFromIAnswerDataTest() {
        assertNull(StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(null));
        assertEquals(Integer.valueOf(0), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(0)));
        assertEquals(Integer.valueOf(-15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(-15)));
        assertEquals(Integer.valueOf(15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(15)));
        assertEquals(Integer.valueOf(15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new DecimalData(15.0)));
        assertEquals(Integer.valueOf(15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new StringData("15")));
        assertNull(StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new StringData("abc")));
    }

    @Test
    public void getDoubleAnswerValueFromIAnswerDataTest() {
        assertNull(StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(null));
        assertEquals(Double.valueOf(0), StringWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(0)));
        assertEquals(Double.valueOf(-15), StringWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(-15)));
        assertEquals(Double.valueOf(-15.123), StringWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(-15.123)));
        assertEquals(Double.valueOf(15), StringWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(15)));
        assertEquals(Double.valueOf(15.123), StringWidgetUtils.getDoubleAnswerValueFromIAnswerData(new DecimalData(15.123)));
        assertEquals(Integer.valueOf(15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new IntegerData(15)));
        assertEquals(Integer.valueOf(15), StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new StringData("15")));
        assertNull(StringWidgetUtils.getIntegerAnswerValueFromIAnswerData(new StringData("abc")));
    }

    @Test
    public void getIntegerDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(StringWidgetUtils.getIntegerData("", formEntryPrompt));
        assertNull(StringWidgetUtils.getIntegerData("5.5", formEntryPrompt));
        assertEquals("0", StringWidgetUtils.getIntegerData("0", formEntryPrompt).getDisplayText());
        assertEquals("7", StringWidgetUtils.getIntegerData("7", formEntryPrompt).getDisplayText());
        assertEquals("-22", StringWidgetUtils.getIntegerData("-22", formEntryPrompt).getDisplayText());
        assertEquals("1000000", StringWidgetUtils.getIntegerData("1000000", formEntryPrompt).getDisplayText());
    }

    @Test
    public void getDecimalDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(StringWidgetUtils.getDecimalData("", formEntryPrompt));
        assertEquals("0.0", StringWidgetUtils.getDecimalData("0", formEntryPrompt).getDisplayText());
        assertEquals("50.0", StringWidgetUtils.getDecimalData("50", formEntryPrompt).getDisplayText());
        assertEquals("7.75", StringWidgetUtils.getDecimalData("7.75", formEntryPrompt).getDisplayText());
        assertEquals("-22.123", StringWidgetUtils.getDecimalData("-22.123", formEntryPrompt).getDisplayText());
    }

    @Test
    public void getStringNumberDataTest() {
        when(formEntryPrompt.getAppearanceHint()).thenReturn(null);
        assertNull(StringWidgetUtils.getDecimalData("", formEntryPrompt));
        assertEquals("0", StringWidgetUtils.getStringNumberData("0", formEntryPrompt).getDisplayText());
        assertEquals("50", StringWidgetUtils.getStringNumberData("50", formEntryPrompt).getDisplayText());
        assertEquals("7.75", StringWidgetUtils.getStringNumberData("7.75", formEntryPrompt).getDisplayText());
        assertEquals("-22.123", StringWidgetUtils.getStringNumberData("-22.123", formEntryPrompt).getDisplayText());
    }
}
