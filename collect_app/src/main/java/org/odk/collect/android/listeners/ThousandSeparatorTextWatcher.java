package org.odk.collect.android.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.util.StringTokenizer;

/**
 * Created by srv_twry on 4/12/17.
 * The custom TextWatcher that automatically adds thousand separators in EditText.
 */

public class ThousandSeparatorTextWatcher implements TextWatcher {

    private DecimalFormat df;
    private EditText editText;
    private static String thousandSeparator;
    private int cursorPosition,beforeLength,afterLength;

    public ThousandSeparatorTextWatcher(EditText editText) {
        this.editText = editText;
        df = new DecimalFormat("#,###.##");
        df.setDecimalSeparatorAlwaysShown(true);
        thousandSeparator = Character.toString(df.getDecimalFormatSymbols().getGroupingSeparator());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        cursorPosition = editText.getSelectionStart()-1;
        final StringBuilder sb = new StringBuilder(charSequence.length());
        sb.append(charSequence);
        String beforeString = sb.toString();

        //saving length after removing separators
        beforeLength = ThousandSeparatorTextWatcher.getOriginalString(beforeString).length();
    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}

    @Override
    public void afterTextChanged(Editable s) {
        try {
            editText.removeTextChangedListener(this);
            String value = editText.getText().toString();

            if (value != null && !value.equals("")) {
                if (value.startsWith(".")) {
                    editText.setText("0.");
                }
                if (value.startsWith("0") && !value.startsWith("0.")) {
                    int index = 0;
                    while (index < value.length() && value.charAt(index) == '0') {
                        index++;
                    }
                    String newValue = Character.toString(value.charAt(0));
                    if (index != 0) {
                        newValue = value.charAt(0) + value.substring(index);
                    }
                    editText.setText(newValue);
                }
                String str = editText.getText().toString().replaceAll(thousandSeparator, "");
                if (!value.equals("")) {
                    editText.setText(getDecimalFormattedString(str));
                    afterLength = str.length();
                }
                editText.setSelection(editText.getText().toString().length());
            }

            //setting the cursor back to where it was
            if (beforeLength > afterLength) {
                editText.setSelection(cursorPosition);
            }
            editText.addTextChangedListener(this);
        } catch (Exception ex) {
            ex.printStackTrace();
            editText.addTextChangedListener(this);
        }
    }

    public static String getDecimalFormattedString(String value) {
        StringTokenizer lst = new StringTokenizer(value, ".");
        String str1 = value;
        String str2 = "";
        if (lst.countTokens() > 1) {
            str1 = lst.nextToken();
            str2 = lst.nextToken();
        }
        String str3 = "";
        int i = 0;
        int j = -1 + str1.length();
        if (str1.charAt(-1 + str1.length()) == '.') {
            j--;
            str3 = ".";
        }
        for (int k = j;; k--) {
            if (k < 0) {
                if (str2.length() > 0) {
                    str3 = str3 + "." + str2;
                }
                return str3;
            }
            if (i == 3) {
                str3 = thousandSeparator + str3;
                i = 0;
            }
            str3 = str1.charAt(k) + str3;
            i++;
        }
    }

    /*
    * Returns the string back after removing all the thousand separators.
    * */
    public static String getOriginalString(String string) {
        //String returnString;
        if (string.contains(thousandSeparator)) {
            return string.replace(thousandSeparator,"");
        } else {
            return string;
        }

    }
}
