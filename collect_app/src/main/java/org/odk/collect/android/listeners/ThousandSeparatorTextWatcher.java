package org.odk.collect.android.listeners;

import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.StringTokenizer;

/**
 * Created by srv_twry on 4/12/17.
 * The custom TextWatcher that automatically adds thousand separators in EditText.
 */

public class ThousandSeparatorTextWatcher implements TextWatcher {

    private DecimalFormat df;
    private EditText editText;
    private static String thousandSeparator;

    public ThousandSeparatorTextWatcher(EditText editText){
        this.editText = editText;
        df = new DecimalFormat("#,###.##");
        df.setDecimalSeparatorAlwaysShown(true);
        thousandSeparator = Character.toString(df.getDecimalFormatSymbols().getGroupingSeparator());
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        editText.removeTextChangedListener(this);

        try {
            String originalString = s.toString();

            Long longVal;
            if (originalString.contains(thousandSeparator)) {
                originalString = originalString.replaceAll(thousandSeparator, "");
            }
            longVal = Long.parseLong(originalString);

            DecimalFormat formatter = (DecimalFormat) NumberFormat.getInstance(Locale.US);
            formatter.applyPattern("#,###.##");
            String formattedString = formatter.format(longVal);

            //setting text after format to EditText
            editText.setText(formattedString);
            editText.setSelection(editText.getText().length());

        } catch (NumberFormatException nfe) {
            nfe.printStackTrace();
        }

        editText.addTextChangedListener(this);
    }

    /*
    * Returns the string back after removing all the thousand separators.
    * */
    public static String getOriginalString(String string) {
        //String returnString;
        if(string.contains(thousandSeparator)){
            return string.replace(thousandSeparator,"");}
        else {
            return string;
        }

    }
}
