package org.odk.collect.android.storage.migration;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;

public class StorageMigrationDialog extends DialogFragment {

    private TextView statusTextView;

    public static StorageMigrationDialog create() {
        return new StorageMigrationDialog();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.Theme_Collect_Dialog_FullScreen);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.storage_migration_dialog, container, false);
    }

    @Override
    public void onViewCreated(@NotNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((Toolbar) view.findViewById(R.id.toolbar)).setTitle(R.string.migration_dialog_title);

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

    public void setStatus(String status) {
        statusTextView.setText(status);
    }
}
