package org.odk.collect.android.utilities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Uses a fragment for launching external app intents. This prevents the onActivityResult
 * callback from being received in the FormEntryActivity and can be caught in the widget directly
 */
public final class ActivityResultHelper {

    private ActivityResultHelper() {

    }

    public static void startActivityForResult(AppCompatActivity activity, Intent intent, int requestCode,
                                              OnActivityResult callback) throws ActivityNotFoundException {
        Fragment aux = new FragmentForResult(callback);
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction().add(aux, "FRAGMENT_TAG").commit();
        fm.executePendingTransactions();
        aux.startActivityForResult(intent, requestCode);
    }

    public interface OnActivityResult {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    @SuppressWarnings("ValidFragment")
    public static class FragmentForResult extends Fragment {
        private OnActivityResult callback;

        public FragmentForResult(OnActivityResult callback) {
            this.callback = callback;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (callback != null) {
                callback.onActivityResult(requestCode, resultCode, data);
            }

            super.onActivityResult(requestCode, resultCode, data);

            if (getActivity() != null) {
                getActivity()
                        .getSupportFragmentManager()
                        .beginTransaction()
                        .remove(this)
                        .commit();
            }
        }
    }
}
