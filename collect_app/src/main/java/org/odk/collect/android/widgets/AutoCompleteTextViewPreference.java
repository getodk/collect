package org.odk.collect.android.widgets;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.EditTextPreference;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;


import java.util.HashSet;


/**
 * Created by bityarn on 21/11/2016.
 */

public class AutoCompleteTextViewPreference extends EditTextPreference {
    private AutoCompleteTextView mEditText;
    private String mText;
    private boolean mTextSet;
    private HashSet<String> mHistory = new HashSet<String>();
    private ViewGroup edViewGroup;




    public AutoCompleteTextViewPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        initEditText(context,attrs);

    }

    public AutoCompleteTextViewPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initEditText(context,attrs);
    }

    public AutoCompleteTextViewPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initEditText(context,attrs);

    }

    private void initEditText(Context context,AttributeSet attrs){
        mEditText  =  new AutoCompleteTextView(context,attrs);
        mEditText.setEnabled(true);
        mEditText.setThreshold(0);


    }



    public AutoCompleteTextViewPreference(Context context) {
        super(context);

    }


    public void setText(String text) {
        // Always persist/notify the first time.
        final boolean changed = !TextUtils.equals(mText, text);
        if (changed || !mTextSet) {
            mText = text;
            mTextSet = true;
            persistString(text);
            mHistory.add(text);
            storeHistory();
            if(changed) {
                loadHistory();
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }


    public String getText() {
        return mText;
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);

        // find the current EditText object
        final EditText editText = (EditText) view.findViewById(android.R.id.edit);
        // copy its layout params
        ViewGroup.LayoutParams params = editText.getLayoutParams();
        ViewGroup vg = (ViewGroup) editText.getParent();
        String curVal = editText.getText().toString();
        // remove it from the existing layout hierarchy
        vg.removeView(editText);

        // construct a new editable autocomplete object with the appropriate params
        // and id that the TextEditPreference is expecting
        mEditText = new AutoCompleteTextView(getContext());
        mEditText.setLayoutParams(params);
        mEditText.setId(android.R.id.edit);
        mEditText.setText(curVal);

        loadHistory();

        mEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus){
                    AutoCompleteTextViewPreference.this.mEditText.showDropDown();
                }else {
                    AutoCompleteTextViewPreference.this.mEditText.dismissDropDown();
                }
            }
        });

        // add the new view to the layout
        vg.addView(mEditText);




    }


    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        if (positiveResult) {
            String value = mEditText.getText().toString();
            if (callChangeListener(value)) {
                setText(value);
            }
        }
        mEditText.dismissDropDown();
    }

    @Override
    protected View onCreateDialogView() {
        mEditText.showDropDown();
        return super.onCreateDialogView();
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getString(index);
    }

    @Override
    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {

        setText(restorePersistedValue ? getPersistedString(mText) : (String) defaultValue);
    }

    @Override
    public boolean shouldDisableDependents() {
        return TextUtils.isEmpty(mText) || super.shouldDisableDependents();
    }

    public AutoCompleteTextView getmEditText() {
        return mEditText;
    }

    public void setmEditText(AutoCompleteTextView mEditText) {
        this.mEditText = mEditText;
    }


    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (isPersistent()) {
            // No need to save instance state since it's persistent
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.text = getText();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            // Didn't save state for us in onSaveInstanceState
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setText(myState.text);
    }

    public AutoCompleteTextView getEditText() {
        return mEditText;
    }

    private static class SavedState extends BaseSavedState {
        String text;

        public SavedState(Parcel source) {
            super(source);
            text = source.readString();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeString(text);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }


    private void loadHistory(){

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        int size = pref.getInt("array_size", 0);

        for(int i=0; i<size; i++) {
            mHistory.add(pref.getString("array_" + i, null));
        }

        ArrayAdapter autocompletetextAdapter =  new ArrayAdapter<String>(getContext(),android.R.layout.simple_dropdown_item_1line, (String[]) mHistory.toArray(new String[mHistory.size()]));



        mEditText.setAdapter(autocompletetextAdapter);



    }

    private void storeHistory(){

        SharedPreferences pref = PreferenceManager
                .getDefaultSharedPreferences(getContext());

        SharedPreferences.Editor edit = pref.edit();
        edit.putInt("array_size", mHistory.size());

        int index = 0;
        for(String val:mHistory){
            edit.putString("array_" +index , val);
            index++;
        }

    }




}
