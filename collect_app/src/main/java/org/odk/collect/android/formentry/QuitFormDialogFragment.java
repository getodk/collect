package org.odk.collect.android.formentry;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.common.collect.ImmutableList;

import org.odk.collect.android.R;
import org.odk.collect.android.adapters.IconMenuListAdapter;
import org.odk.collect.android.adapters.model.IconMenuItem;
import org.odk.collect.android.logic.FormController;
import org.odk.collect.android.preferences.AdminKeys;
import org.odk.collect.android.preferences.AdminSharedPreferences;
import org.odk.collect.android.utilities.DialogUtils;

import java.util.List;

public class QuitFormDialogFragment extends DialogFragment {

    private FormController formController;
    private Listener listener;
    private ListView listView;
    private String title;

    public QuitFormDialogFragment(FormController formController, Listener listener) {
        this.formController = formController;
        this.listener = listener;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof Listener) {
            listener = (Listener) context;
        }

        title = (formController == null) ? null : formController.getFormTitle();
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
                listener.onSaveChangedClicked();
            } else {
                listener.onIgnoreChangesClicked();
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


    public interface Listener {
        void onSaveChangedClicked();

        void onIgnoreChangesClicked();
    }
}
