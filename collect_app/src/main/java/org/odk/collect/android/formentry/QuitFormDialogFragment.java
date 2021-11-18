package org.odk.collect.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.common.collect.ImmutableList;

import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.preferences.keys.ProtectedProjectKeys;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.InstancesRepositoryProvider;
import org.odk.collect.async.Scheduler;
import org.odk.collect.forms.instances.Instance;

import java.util.List;

import javax.inject.Inject;

import static android.app.Activity.RESULT_OK;

public class QuitFormDialogFragment extends DialogFragment {

    @Inject
    Analytics analytics;

    @Inject
    Scheduler scheduler;

    @Inject
    FormSaveViewModel.FactoryFactory formSaveViewModelFactoryFactory;

    @Inject
    SettingsProvider settingsProvider;

    @Inject
    CurrentProjectProvider currentProjectProvider;

    private FormSaveViewModel formSaveViewModel;
    private Listener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);

        ViewModelProvider.Factory factory = formSaveViewModelFactoryFactory.create(requireActivity(), null);
        formSaveViewModel = new ViewModelProvider(requireActivity(), factory).get(FormSaveViewModel.class);

        if (context instanceof Listener) {
            listener = (Listener) context;
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);

        String title = formSaveViewModel.getFormName() == null ? getActivity().getString(R.string.no_form_loaded) : formSaveViewModel.getFormName();

        List<IconMenuItem> items;
        if (settingsProvider.getProtectedSettings().getBoolean(ProtectedProjectKeys.KEY_SAVE_MID)) {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_save, R.string.keep_changes),
                    new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        } else {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        }

        ListView listView = DialogUtils.createActionListView(getActivity());

        final IconMenuListAdapter adapter = new IconMenuListAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            IconMenuItem item = (IconMenuItem) adapter.getItem(position);

            if (item.getTextResId() == R.string.keep_changes) {
                if (listener != null) {
                    listener.onSaveChangesClicked();
                }
            } else {
                formSaveViewModel.ignoreChanges();

                String action = getActivity().getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_EDIT.equals(action)) {
                    // caller is waiting on a picked form
                    Uri uri = null;
                    String path = formSaveViewModel.getAbsoluteInstancePath();
                    if (path != null) {
                        Instance instance = new InstancesRepositoryProvider(requireContext()).get().getOneByPath(path);
                        if (instance != null) {
                            uri = InstancesContract.getUri(currentProjectProvider.getCurrentProject().getUuid(), instance.getDbId());
                        }
                    }

                    if (uri != null) {
                        getActivity().setResult(RESULT_OK, new Intent().setData(uri));
                    }
                }
                getActivity().finish();
            }

            if (getDialog() != null) {
                getDialog().dismiss();
            }
        });

        return new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.quit_application, title))
                .setNegativeButton(getActivity().getString(R.string.do_not_exit), (dialog, id) -> {
                    dialog.cancel();
                    dismiss();
                })
                .setView(listView)
                .create();
    }

    public interface Listener {
        void onSaveChangesClicked();
    }
}
