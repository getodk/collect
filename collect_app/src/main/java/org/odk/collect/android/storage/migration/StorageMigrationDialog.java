package org.odk.collect.android.storage.migration;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProviders;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;

import timber.log.Timber;

public class StorageMigrationDialog extends DialogFragment {

    public interface OnMigrationCompleteListener {
        void onMigrationComplete(StorageMigrationResult result);
    }

    private OnMigrationCompleteListener listener;

    private StorageMigrationViewModel viewModel;

    public static StorageMigrationDialog create() {
        return new StorageMigrationDialog();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(this).get(StorageMigrationViewModel.class);
        try {
            listener = (OnMigrationCompleteListener) getActivity();
        } catch (ClassCastException e) {
            Timber.w(e);
        }
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

        Button cancel = view.findViewById(R.id.cancel);
        Button migrate = view.findViewById(R.id.migrate);

        cancel.setOnClickListener(view1 -> dismiss());
        migrate.setOnClickListener(v -> {
            cancel.setEnabled(false);
            cancel.setAlpha(.4f);
            migrate.setEnabled(false);
            migrate.setAlpha(.4f);

            view.findViewById(R.id.progress_bar).setVisibility(View.VISIBLE);

            viewModel.performMigration().observe(this, result -> listener.onMigrationComplete(result));
        });
    }

}
