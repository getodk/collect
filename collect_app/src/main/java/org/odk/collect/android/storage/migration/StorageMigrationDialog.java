package org.odk.collect.android.storage.migration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StorageMigrationDialog extends MaterialFullScreenDialogFragment {

    @BindView(R.id.cancel)
    Button cancelButton;

    @BindView(R.id.migrate)
    Button migrateButton;

    @BindView(R.id.message)
    TextView messageText;

    @BindView(R.id.progress_bar)
    LinearLayout progressBar;

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
        ButterKnife.bind(this, view);

        updateToolbar();

        cancelButton.setOnClickListener(v -> dismiss());
        migrateButton.setOnClickListener(v -> {
            disableDialog();
            showProgressBar();
            startStorageMigrationService();
        });
    }

    @Override
    protected void onCloseClicked() {
    }

    @Override
    protected void onBackPressed() {
    }

    private void updateToolbar() {
        getToolbar().setTitle(R.string.migration_dialog_title);
        getToolbar().setNavigationIcon(null);
    }

    private void disableDialog() {
        cancelButton.setEnabled(false);
        cancelButton.setAlpha(.4f);
        migrateButton.setEnabled(false);
        migrateButton.setAlpha(.4f);
        messageText.setAlpha(.4f);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void startStorageMigrationService() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, StorageMigrationService.class);
            activity.startService(intent);
        }
    }
}
