package org.odk.collect.android.utilities;

import org.javarosa.core.model.data.IAnswerData;
import org.javarosa.core.model.data.IntegerData;
import org.odk.collect.android.listeners.ThousandsSeparatorTextWatcher;

public class TextWidgetUtils {

    public static Integer getIntegerAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        Integer d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Double) {
                    d = ((Double) dataValue).intValue();
                } else {
                    d = (Integer) dataValue;
                }
            }
        }
        return d;
    }

    public static Double getDoubleAnswerValueFromIAnswerData(IAnswerData dataHolder) {
        Double d = null;
        if (dataHolder != null) {
            Object dataValue = dataHolder.getValue();
            if (dataValue != null) {
                if (dataValue instanceof Integer) {
                    d = (double) (Integer) dataValue;
                } else {
                    d = (Double) dataValue;
                }
            }
        }
        return d;
    }

    public static IAnswerData getIAnswerDataFromInteger(String answer, boolean useThousandSeparator) {
        if (useThousandSeparator) {
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
}
