package org.odk.collect.android.support;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.odk.collect.android.audio.ScreenContext;

public class WidgetTestActivity extends FragmentActivity implements ScreenContext {

    @Override
    public FragmentActivity getActivity() {
        return this;
    }

    @Override
    public LifecycleOwner getViewLifecycle() {
        return this;
    }
}
