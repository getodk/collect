package org.odk.collect.android.fragments;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.odk.collect.android.R;


public class Tab extends android.support.v4.app.Fragment{

    public static Tab newInstance() {
        Tab tab = new Tab();
        return tab;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_layout, container, false);
        return v;
    }
}
