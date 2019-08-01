package org.odk.collect.android.audio;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

public interface ScreenContext {

    FragmentActivity getActivity();

    LifecycleOwner getViewLifecycle();
}
