package org.odk.collect.android.support;

import androidx.fragment.app.FragmentActivity;

import org.odk.collect.android.audio.AndroidScreen;

public class TestScreen implements AndroidScreen {

    private final FragmentActivity activity;

    public TestScreen(FragmentActivity activity) {
        this.activity = activity;
    }

    @Override
    public FragmentActivity getActivity() {
        return activity;
    }
}