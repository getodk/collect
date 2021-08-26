package org.odk.collect.android.widgets.utilities;

import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.method.DigitsKeyListener;
import android.widget.EditText;

import org.javarosa.core.model.data.DecimalData;
import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.javarosa.core.model.data.StringData;
import org.javarosa.form.api.FormEntryPrompt;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;
import org.odk.collect.android.utilities.Appearances;

import java.text.NumberFormat;
import java.util.Locale;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public final class StringWidgetUtils {

    private StringWidgetUtils() {
    }

    @SuppressFBWarnings("BX_UNBOXING_IMMEDIATELY_REBOXED")
    public static Integer getIntegerAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue instanceof Double) {
                return ((Double) dataValue).intValue();
            } else if (dataValue instanceof Integer) {
                return (Integer) dataValue;
            } else if (dataValue instanceof String) {
                try {
                    return Integer.parseInt((String) dataValue);
                } catch (NumberFormatException ignored) {
                    // ignored
                }
            }
        }
        return null;
    }

    @SuppressFBWarnings({"BX_UNBOXING_IMMEDIATELY_REBOXED", "BX_UNBOXED_AND_COERCED_FOR_TERNARY_OPERATOR"})
    public static Double getDoubleAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue instanceof Double) {
                return (Double) dataValue;
            } else if (dataValue instanceof Integer) {
                return Double.valueOf((Integer) dataValue);
            } else if (dataValue instanceof String) {
                try {
                    return Double.parseDouble((String) dataValue);
                } catch (NumberFormatException ignored) {
                    // ignored
                }
            }
        }
        return null;
    }

    public static IntegerData getIntegerData(String answer, FormEntryPrompt prompt) {
        if (Appearances.useThousandSeparator(prompt)) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;
        } else {
            try {
                return new IntegerData(Integer.parseInt(answer));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static DecimalData getDecimalData(String answer, FormEntryPrompt prompt) {
        if (Appearances.useThousandSeparator(prompt)) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;

        } else {
            try {
                return new DecimalData(Double.parseDouble(answer));
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static StringData getStringNumberData(String answer, FormEntryPrompt prompt) {
        if (Appearances.useThousandSeparator(prompt)) {
            answer = ThousandsSeparatorTextWatcher.getOriginalString(answer);
        }

        if (answer.isEmpty()) {
            return null;
        } else {
            try {
                return new StringData(answer);
            } catch (Exception numberFormatException) {
                return null;
            }
        }
    }

    public static void adjustEditTextAnswerToIntegerWidget(EditText answerText, FormEntryPrompt prompt) {
        boolean useThousandSeparator = Appearances.useThousandSeparator(prompt);
        if (Appearances.useThousandSeparator(prompt)) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }
        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        // only allows numbers and no periods
        answerText.setKeyListener(new DigitsKeyListener(true, false));
        // ints can only hold 2,147,483,648. we allow 999,999,999
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(9);
        if (useThousandSeparator) {
            //11 since for a nine digit number , their will be 2 separators.
            fa[0] = new InputFilter.LengthFilter(11);
        }
        answerText.setFilters(fa);

        Integer i = getIntegerAnswerValueFromIAnswerData(prompt.getAnswerValue());

        if (i != null) {
            answerText.setText(String.format(Locale.US, "%d", i));
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }

    public static void adjustEditTextAnswerToDecimalWidget(EditText answerText, FormEntryPrompt prompt) {
        boolean useThousandSeparator = Appearances.useThousandSeparator(prompt);
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
        // only numbers are allowed
        answerText.setKeyListener(new DigitsKeyListener(true, true));

        // only 15 characters allowed
        InputFilter[] fa = new InputFilter[1];
        fa[0] = new InputFilter.LengthFilter(15);
        if (useThousandSeparator) {
            fa[0] = new InputFilter.LengthFilter(19);
        }
        answerText.setFilters(fa);

        Double d = getDoubleAnswerValueFromIAnswerData(prompt.getAnswerValue());

        if (d != null) {
            // truncate to 15 digits max in US locale
            // use US locale because DigitsKeyListener can't be localized before API 26
            NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
            nf.setMaximumFractionDigits(15);
            nf.setMaximumIntegerDigits(15);
            nf.setGroupingUsed(false);

            String formattedValue = nf.format(d);
            answerText.setText(formattedValue);

            Selection.setSelection(answerText.getText(), answerText.getText().length());
        }
    }

    public static void adjustEditTextAnswerToStringNumberWidget(EditText answerText, FormEntryPrompt prompt) {
        answerText.setInputType(InputType.TYPE_NUMBER_FLAG_SIGNED);
        boolean useThousandSeparator = Appearances.useThousandSeparator(prompt);
        if (useThousandSeparator) {
            answerText.addTextChangedListener(new ThousandsSeparatorTextWatcher(answerText));
        }

        answerText.setKeyListener(new DigitsKeyListener() {
            @Override
            protected char[] getAcceptedChars() {
                return new char[]{
                        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', '.', '-', '+', ' ', ','
                };
            }
        });

        String s = null;
        IAnswerData answerData = prompt.getAnswerValue();
        if (answerData != null) {
            s = (String) answerData.getValue();
        }

        if (s != null) {
            answerText.setText(s);
            Selection.setSelection(answerText.getText(), answerText.getText().toString().length());
        }
    }
}
