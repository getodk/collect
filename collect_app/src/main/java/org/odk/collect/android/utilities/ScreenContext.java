package org.odk.collect.android.utilities;

import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LifecycleOwner;

/**
 * An object that holds both a {@link FragmentActivity} and a {@link LifecycleOwner} that
 * represents the lifecycle of a {@link android.view.View}. This can be useful when views needing
 * a {@link LifecycleOwner} reference (for use with {@link androidx.lifecycle.LiveData} for
 * example) have a "lifecycle" that is separate from their parent {@link android.app.Activity} or
 * {@link androidx.fragment.app.Fragment}. These views would ideally be contained in a
 * {@link androidx.fragment.app.Fragment} that can determine their lifecycle but in cases where that
 * isn't possible {@link ScreenContext} can provide a polyfill.
 * <p>
 * The simplest way to use {@link ScreenContext} is have a view's parent
 * {@link android.app.Activity} implement it so the view can access it from its
 * {@link android.content.Context} reference like so:
 * <p>
 * {@code
 * public class MyView {
 * <p>
 * private final ScreenContext screenContext;
 * <p>
 * public MyView(Context context) {
 * this.screenContext = (ScreenContext) context;
 * }
 * }
 * }
 */
public interface ScreenContext {

    FragmentActivity getActivity();

    LifecycleOwner getViewLifecycle();
}
