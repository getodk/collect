package org.odk.collect.android.support;

import android.view.View;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

import org.odk.collect.android.utilities.ScreenContext;

import java.util.ArrayList;
import java.util.List;

public class TestScreenContextActivity extends FragmentActivity implements ScreenContext {
    public final List<View> viewsRegisterForContextMenu = new ArrayList<>();

    @Override
    public FragmentActivity getActivity() {
        return this;
    }

    @Override
    public LifecycleOwner getViewLifecycle() {
        return this;
    }

    @Override
    public void registerForContextMenu(View view) {
        super.registerForContextMenu(view);
        viewsRegisterForContextMenu.add(view);
    }
}
