package org.odk.collect.android.widgets;

import org.javarosa.core.model.IFormElement;
import org.javarosa.core.model.QuestionDef;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.form.api.FormEntryPrompt;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.odk.collect.android.BuildConfig;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class DecimalWidgetTest {
    @Mock
    FormEntryPrompt formEntryPrompt;

    @Mock
    IAnswerData answerData;

    @Mock
    IFormElement element;

    @Mock
    QuestionDef question;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);

        when(formEntryPrompt.getLongText()).thenReturn("A decimal question");

        when(formEntryPrompt.getFormElement()).thenReturn(element);
        when(element.getAdditionalAttribute(null, "playColor")).thenReturn(null);

        when(formEntryPrompt.getQuestion()).thenReturn(question);
        when(question.getAdditionalAttribute(null, "rows")).thenReturn(null);
    }

    @Test
    // Fails when double is turned to string with toString or String.format(Locale.US, "%f", d))
    public void integerValueShouldDisplayNoDecimalPoint() {
        Double integerDouble = 0.;
        String integerString = "0";
        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(integerDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(), is(equalTo(integerString)));
    }

    @Test
    // Fails when double is turned to string using String.format(Locale.US, "%f", d)
    // because default precision for %f is 6
    // NOTE: in the case of a decimal value with trailing 0s, it's probably not possible to maintain
    // that precision. For example, 9.00 becomes 9
    public void decimalValueShouldNotAddPrecision() {
        Double twoDecimalDouble = 9.99;
        String twoDecimalString = "9.99";
        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(twoDecimalDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(), is(equalTo(twoDecimalString)));
    }

    @Test
    // Fails when double is turned to string with toString or String.format(Locale.US, "%f", d)
    public void negativeIntegerShouldDisplayNegativeWithNoDecimalPoint() {
        Double negativeIntegerDouble = -999.;
        String negativeIntegerString = "-999";
        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(negativeIntegerDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(), is(equalTo(negativeIntegerString)));
    }

    @Test
    // Fails when double is turned to string with toString because of scientific notation
    // https://docs.oracle.com/javase/7/docs/api/java/lang/Double.html#toString(double)
    public void fifteenDigitValueShouldDisplayAllDigits() {
        Double fifteenDigitDouble = 999999999999999.;
        String fifteenDigitString = "999999999999999";
        assertTrue(fifteenDigitString.length() == 15);

        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(fifteenDigitDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(), is(equalTo(fifteenDigitString)));
    }

    @Test
    // Fails when double is turned to string with toString because of scientific notation
    // https://docs.oracle.com/javase/7/docs/api/java/lang/Double.html#toString(double)
    public void fifteenDigitNegativeValueShouldDisplayAllDigits() {
        Double fifteenDigitNegativeDouble = -99999999999999.;
        String fifteenDigitNegativeString = "-99999999999999";
        assertTrue(fifteenDigitNegativeString.length() == 15);

        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(fifteenDigitNegativeDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(),
                is(equalTo(fifteenDigitNegativeString)));
    }

    @Test
    // Fails when double is turned to string using String.format(Locale.US, "%f", d) because default
    // precision for %f is 6
    public void fifteenDigitDecimalValueShouldDisplayAllDigits() {
        Double fifteenDigitDecimalDouble = 0.9999999999999;
        String fifteenDigitDecimalString = "0.9999999999999";
        assertTrue(fifteenDigitDecimalString.length() == 15);

        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(fifteenDigitDecimalDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(),
                is(equalTo(fifteenDigitDecimalString)));
    }

    @Test
    // This should never be possible because the EditText has a limit on it
    public void digitsAboveLimitOfFifteenShouldBeTruncatedFromLeft() {
        Double eighteenDigitDouble = 9999999999999994.;
        String fifteenDigitString = "999999999999994";
        assertTrue(fifteenDigitString.length() == 15);

        when(formEntryPrompt.getAnswerValue()).thenReturn(answerData);
        when(answerData.getValue()).thenReturn(eighteenDigitDouble);

        DecimalWidget decimalWidget = new DecimalWidget(RuntimeEnvironment.application,
                formEntryPrompt, false);

        assertThat(decimalWidget.answer.getText().toString(), is(equalTo(fifteenDigitString)));
    }
}
