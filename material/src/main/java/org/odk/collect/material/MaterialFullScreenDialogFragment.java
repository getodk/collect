package org.odk.collect.material;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.androidshared.ui.OnBackPressedKeyListener;

/**
 * Provides an implementation of Material's "Full Screen Dialog"
 * (https://material.io/components/dialogs/#full-screen-dialog) as no implementation currently
 * exists in the Material Components framework
 */
public abstract class MaterialFullScreenDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_MaterialComponents_Dialog_FullScreen);
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);

            if (shouldShowSoftKeyboard()) {
                // Make sure soft keyboard shows for focused field - annoyingly needed
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }

            setCancelable(false);
            dialog.setOnKeyListener(new OnBackPressedKeyListener(() -> {
                onBackPressed();
                return null;
            }));
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (getToolbar() != null) {
            getToolbar().setNavigationOnClickListener(v -> {
                onCloseClicked();
            });
        }
    }

    protected abstract void onCloseClicked();

    protected abstract void onBackPressed();

    @Nullable
    protected abstract Toolbar getToolbar();

    protected boolean shouldShowSoftKeyboard() {
        return false;
    }
}
