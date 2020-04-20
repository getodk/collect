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
import androidx.lifecycle.ViewModelProviders;

import com.google.common.collect.ImmutableList;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.dao.helpers.InstancesDaoHelper;
import org.odk.collect.android.external.ExternalDataManager;
import org.odk.collect.android.formentry.audit.AuditEvent;
import org.odk.collect.android.formentry.saving.FormSaveViewModel;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MediaManager;

import java.util.List;

import static android.app.Activity.RESULT_OK;

public class QuitFormDialogFragment extends DialogFragment {

    private FormSaveViewModel viewModel;
    private final ViewModelProvider.Factory viewModelFactory = new FormSaveViewModel.Factory();

    private ListView listView;
    private String title;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        viewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(FormSaveViewModel.class);

        title = viewModel.getFormName();
        if (title == null) {
            title = context.getString(R.string.no_form_loaded);
        }

        List<IconMenuItem> items;
        if ((boolean) AdminSharedPreferences.getInstance().get(AdminKeys.KEY_SAVE_MID)) {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_save, R.string.keep_changes),
                    new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        } else {
            items = ImmutableList.of(new IconMenuItem(R.drawable.ic_delete, R.string.do_not_save));
        }

        listView = DialogUtils.createActionListView(context);

        final IconMenuListAdapter adapter = new IconMenuListAdapter(context, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener((parent, view, position, id) -> {
            IconMenuItem item = (IconMenuItem) adapter.getItem(position);

            if (item.getTextResId() == R.string.keep_changes) {
                viewModel.saveForm(getActivity().getIntent().getData(), InstancesDaoHelper.isInstanceComplete(false),
                        null, true);

            } else {
                ExternalDataManager manager = Collect.getInstance().getExternalDataManager();
                if (manager != null) {
                    manager.close();
                }

                if (viewModel.getAuditEventLogger() != null) {
                    viewModel.getAuditEventLogger().logEvent(AuditEvent.AuditEventType.FORM_EXIT, true, System.currentTimeMillis());
                }

                viewModel.removeTempInstance();
                MediaManager.INSTANCE.revertChanges();

                String action = getActivity().getIntent().getAction();
                if (Intent.ACTION_PICK.equals(action) || Intent.ACTION_EDIT.equals(action)) {
                    // caller is waiting on a picked form
                    Uri uri = InstancesDaoHelper.getLastInstanceUri(viewModel.getAbsoluteInstancePath());
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
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState);
        setRetainInstance(true);
        AlertDialog alertDialog = (AlertDialog) getDialog();

        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(getActivity())
                    .setTitle(
                            getActivity().getString(R.string.quit_application, title))
                    .setPositiveButton(getActivity().getString(R.string.do_not_exit), (dialog, id) -> {
                        dialog.cancel();
                        dismiss();
                    })
                    .setView(listView)
                    .create();
        }

        return alertDialog;
    }

    @Override
    public void onDestroyView() {
        AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null && getRetainInstance()) {
            dialog.setDismissMessage(null);
            dialog.dismiss();
        }
        super.onDestroyView();
    }

}
