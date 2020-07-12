package org.odk.collect.android.preferences;

import android.os.Bundle;
import android.text.InputFilter;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListPopupWindow;

import androidx.preference.EditTextPreferenceDialogFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.filters.ControlCharacterFilter;
import org.odk.collect.android.preferences.filters.WhitespaceFilter;
import org.odk.collect.android.preferences.utilities.ChangingServerUrlUtils;
import org.odk.collect.android.utilities.SoftKeyboardUtils;

public class ServerUrlEditTextPreferenceDialog extends EditTextPreferenceDialogFragmentCompat implements View.OnTouchListener {

    private CustomEditTextPreference serverUrlEditTextPreference;
    private ListPopupWindow listPopupWindow;

    public static ServerUrlEditTextPreferenceDialog newInstance(String key) {
        ServerUrlEditTextPreferenceDialog fragment = new ServerUrlEditTextPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onBindDialogView(View view) {
        if (getPreference() instanceof CustomEditTextPreference) {
            serverUrlEditTextPreference = (CustomEditTextPreference) getPreference();
        }
        EditText editText = (EditText) view.findViewById(android.R.id.edit);
        urlDropdownSetup(editText);

        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_arrow_drop_down, 0);
        editText.setFilters(new InputFilter[]{new ControlCharacterFilter(), new WhitespaceFilter()});
        editText.setOnTouchListener((View.OnTouchListener) this);

        serverUrlEditTextPreference.setOnPreferenceClickListener(preference -> {
            editText.requestFocus();
            return true;
        });

        super.onBindDialogView(view);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        final int DRAWABLE_RIGHT = 2;
        if (event.getAction() == MotionEvent.ACTION_UP) {
            if (event.getX() >= (v.getWidth() - ((EditText) v)
                    .getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                SoftKeyboardUtils.hideSoftKeyboard(v);
                listPopupWindow.show();
                return true;
            }
        }
        return false;
    }

    private void urlDropdownSetup(EditText editText) {
        listPopupWindow = new ListPopupWindow(getActivity());
        setupUrlDropdownAdapter();
        listPopupWindow.setAnchorView(editText);
        listPopupWindow.setModal(true);
        listPopupWindow.setOnItemClickListener((parent, view, position, id) -> {
            editText.setText(ChangingServerUrlUtils.getUrlList().get(position));
            listPopupWindow.dismiss();
        });
    }

    public void setupUrlDropdownAdapter() {
        ArrayAdapter adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, ChangingServerUrlUtils.getUrlList());
        listPopupWindow.setAdapter(adapter);
    }
}