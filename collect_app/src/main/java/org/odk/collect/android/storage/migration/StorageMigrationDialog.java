package org.odk.collect.android.storage.migration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

public class StorageMigrationDialog extends MaterialFullScreenDialogFragment {

    private TextView statusTextView;

    public static StorageMigrationDialog create() {
        return new StorageMigrationDialog();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.storage_migration_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getToolbar().setTitle(R.string.migration_dialog_title);
        getToolbar().setNavigationIcon(null);

        statusTextView = view.findViewById(R.id.status);
        Button cancel = view.findViewById(R.id.cancel);
        Button migrate = view.findViewById(R.id.migrate);
        TextView message = view.findViewById(R.id.message);

        cancel.setOnClickListener(view1 -> dismiss());
        migrate.setOnClickListener(v -> {
            cancel.setEnabled(false);
            cancel.setAlpha(.4f);
            migrate.setEnabled(false);
            migrate.setAlpha(.4f);
            message.setAlpha(.4f);

            view.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

            Activity activity = getActivity();
            if (activity != null) {
                activity.startService(new Intent(activity, StorageMigrationService.class));
            }
        });
    }

    @Override
    protected void onCloseClicked() {
    }

    @Override
    protected void onBackPressed() {
    }

    @Override
    protected boolean shouldShowSoftKeyboard() {
        return false;
    }

    public void setStatus(String status) {
        statusTextView.setText(status);
    }
}
