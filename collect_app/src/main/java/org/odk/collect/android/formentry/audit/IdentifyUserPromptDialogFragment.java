package org.odk.collect.android.formentry.audit;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import org.odk.collect.android.R;

public class IdentifyUserPromptDialogFragment extends DialogFragment {

    public static final String TAG = "IdentifyUserPromptDialogFragment";
    private static final String ARG_FORM_NAME = "ArgFormName";

    public static IdentifyUserPromptDialogFragment create(String formName) {
        IdentifyUserPromptDialogFragment dialog = new IdentifyUserPromptDialogFragment();

        Bundle bundle = new Bundle();
        bundle.putString(IdentifyUserPromptDialogFragment.ARG_FORM_NAME, formName);
        dialog.setArguments(bundle);

        return dialog;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.identify_user_dialog, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.setTitle(getArguments().getString(ARG_FORM_NAME));
        toolbar.inflateMenu(R.menu.menu_ak);

        EditText identityField = view.findViewById(R.id.identity);
        identityField.setOnEditorActionListener((textView, i, keyEvent) -> {
            dismiss();
            return true;
        });
    }

    @Override
    public void onStart() {
        super.onStart();

        Dialog dialog = getDialog();
        if (dialog != null) {
            int width = ViewGroup.LayoutParams.MATCH_PARENT;
            int height = ViewGroup.LayoutParams.MATCH_PARENT;
            dialog.getWindow().setLayout(width, height);
        }
    }
}
