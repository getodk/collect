package org.odk.collect.android.utilities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;

import java.io.Serializable;

/**
 * Uses a fragment for launching external app intents. This prevents the onActivityResult
 * callback from being received in the FormEntryActivity and can be caught in the widget directly
 */
public final class ActivityResultHelper {

    private ActivityResultHelper() {

    }

    public static Fragment getAuxFragment(AppCompatActivity activity, OnActivityResult callback) {
        Fragment aux = new FragmentForResult(callback);
        FragmentManager fm = activity.getSupportFragmentManager();
        fm.beginTransaction().add(aux, "FRAGMENT_TAG").commit();
        fm.executePendingTransactions();
        return aux;
    }

    public interface OnActivityResult extends Serializable {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    @SuppressLint("ValidFragment")
    public static class FragmentForResult extends Fragment {
        private final OnActivityResult callback;

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
