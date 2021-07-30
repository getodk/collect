package org.odk.collect.android.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.util.regex.Pattern;

import timber.log.Timber;

/**
 * Created by srv_twry on 4/12/17.
 * Source: https://stackoverflow.com/a/34265406/137744
 * The custom TextWatcher that automatically adds thousand separators in EditText.
 */

public class ThousandsSeparatorTextWatcher implements TextWatcher {
    private final EditText editText;
    private static String thousandSeparator;
    private int cursorPosition;

    public ThousandsSeparatorTextWatcher(EditText editText) {
        this.editText = editText;
        DecimalFormat df = new DecimalFormat();
        df.setDecimalSeparatorAlwaysShown(true);
        thousandSeparator = Character.toString(df.getDecimalFormatSymbols().getGroupingSeparator());

        // The decimal marker is always "." (see DecimalWidget) so avoid it as thousands separator
        if (thousandSeparator.equals(".")) {
            thousandSeparator = " ";
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int start, int count, int after) {
        cursorPosition = editText.getText().toString().length() - editText.getSelectionStart();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable s) {
        try {
            editText.removeTextChangedListener(this);
            String value = editText.getText().toString();

            if (!value.equals("")) {
                String str = editText.getText().toString().replaceAll(Pattern.quote(thousandSeparator), "");
                if (!value.equals("")) {
                    editText.setText(getDecimalFormattedString(str));
                }
                editText.setSelection(editText.getText().toString().length());
            }

            //setting the cursor back to where it was
            int selectionIndex = editText.getText().toString().length() - cursorPosition;
            editText.setSelection(Math.max(selectionIndex, 0));
            editText.addTextChangedListener(this);
        } catch (Exception ex) {
            Timber.e(ex);
            editText.addTextChangedListener(this);
        }
    }

    private static String getDecimalFormattedString(String value) {
        // Always use a period because keyboard isn't localized. See DecimalWidget.
        String decimalMarker = ".";

        String[] splitValue = value.split(Pattern.quote(decimalMarker));
        String beforeDecimal = value;
        String afterDecimal = null;
        String finalResult = "";

        if (splitValue.length == 2) {
            beforeDecimal = splitValue[0];
            afterDecimal = splitValue[1];
        }

        int count = 0;
        for (int i = beforeDecimal.length() - 1; i >= 0; i--) {
            finalResult = beforeDecimal.charAt(i) + finalResult;
            count++;
            if (count == 3 && i > 0) {
                finalResult = thousandSeparator + finalResult;
                count = 0;
            }
        }

        if (afterDecimal != null) {
            finalResult = finalResult + decimalMarker + afterDecimal;
        }

        return finalResult;
    }

    /*
    * Returns the string after removing all the thousands separators.
    * */
    public static String getOriginalString(String string) {
        return string.replace(thousandSeparator, "");
    }
}
