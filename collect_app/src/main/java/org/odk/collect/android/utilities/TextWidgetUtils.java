package org.odk.collect.android.utilities;

import org.javarosa.core.model.data.IAnswerData;

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
}
