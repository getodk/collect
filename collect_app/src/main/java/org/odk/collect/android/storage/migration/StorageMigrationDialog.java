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
import org.odk.collect.android.activities.WebViewActivity;
import org.odk.collect.android.material.MaterialFullScreenDialogFragment;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StorageMigrationDialog extends MaterialFullScreenDialogFragment {

    private final int unsentInstancesNumber;

    @BindView(R.id.cancelButton)
    Button cancelButton;

    @BindView(R.id.migrateButton)
    Button migrateButton;

    @BindView(R.id.messageText1)
    TextView messageText1;

    @BindView(R.id.messageText2)
    TextView messageText2;

    @BindView(R.id.messageText3)
    TextView messageText3;

    @BindView(R.id.moreDetailsButton)
    TextView moreDetailsButton;

    @BindView(R.id.errorText)
    TextView errorText;

    @BindView(R.id.progressBar)
    LinearLayout progressBar;

    public static StorageMigrationDialog create(int unsentInstances) {
        return new StorageMigrationDialog(unsentInstances);
    }

    private StorageMigrationDialog(int unsentInstancesNumber) {
        this.unsentInstancesNumber = unsentInstancesNumber;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.storage_migration_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);

        setUpToolbar();
        setUpMessageAboutUnsetSubmissions();

        moreDetailsButton.setOnClickListener(view1 -> showMoreDetails());
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

    private void setUpToolbar() {
        getToolbar().setTitle(R.string.storage_migration_dialog_title);
        getToolbar().setNavigationIcon(null);
    }

    private void setUpMessageAboutUnsetSubmissions() {
        if (unsentInstancesNumber > 0) {
            messageText2.setVisibility(View.VISIBLE);
            messageText2.setText(getString(R.string.storage_migration_dialog_message2, unsentInstancesNumber));
        }
    }

    private void showMoreDetails() {
        Intent intent = new Intent(getContext(), WebViewActivity.class);
        intent.putExtra("url", "https://forum.opendatakit.org/t/24159");
        startActivity(intent);
    }

    private void disableDialog() {
        messageText1.setAlpha(.5f);
        messageText2.setAlpha(.5f);
        messageText3.setAlpha(.5f);

        moreDetailsButton.setEnabled(false);
        moreDetailsButton.setAlpha(.5f);

        cancelButton.setEnabled(false);
        cancelButton.setAlpha(.5f);

        migrateButton.setEnabled(false);
        migrateButton.setAlpha(.5f);

        errorText.setVisibility(View.GONE);
    }

    private void enableDialog() {
        messageText1.setAlpha(1);
        messageText2.setAlpha(1);
        messageText3.setAlpha(1);

        moreDetailsButton.setEnabled(true);
        moreDetailsButton.setAlpha(1);

        cancelButton.setEnabled(true);
        cancelButton.setAlpha(1);

        migrateButton.setEnabled(true);
        migrateButton.setAlpha(1);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

    private void startStorageMigrationService() {
        Activity activity = getActivity();
        if (activity != null) {
            Intent intent = new Intent(activity, StorageMigrationService.class);
            activity.startService(intent);
        }
    }

    public void handleMigrationError(StorageMigrationResult result) {
        hideProgressBar();
        enableDialog();

        errorText.setVisibility(View.VISIBLE);
        errorText.setText(result.getErrorResultMessage(result, getContext()));
        migrateButton.setText(R.string.try_again);
    }
}
