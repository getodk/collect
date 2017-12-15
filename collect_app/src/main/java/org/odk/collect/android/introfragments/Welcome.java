package org.odk.collect.android.introfragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

import com.heinrichreimersoftware.materialintro.app.SlideFragment;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.PreferenceKeys;

/**
 * Created on 15/12/17.
 */

public class Welcome extends SlideFragment {
    private CheckBox checkBox;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private boolean showIntro;

    public Welcome() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = sharedPreferences.edit();
        showIntro = sharedPreferences.getBoolean(PreferenceKeys.KEY_SHOW_INTRO, true);
        return inflater.inflate(R.layout.intro_layout_welocme,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        checkBox = (CheckBox) view.findViewById(R.id.checkbox_intro_show);

        if (showIntro) {
            checkBox.setChecked(true);
        } else {
            checkBox.setChecked(false);
        }


        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if (isChecked) {
                    editor.putBoolean(PreferenceKeys.KEY_SHOW_INTRO,true);
                    showIntro = true;
                } else {
                    editor.putBoolean(PreferenceKeys.KEY_SHOW_INTRO,false);
                    showIntro = false;
                }
                editor.apply();
            }
        });
    }

    public static Welcome newInstance() {
        return new Welcome();
    }

}
