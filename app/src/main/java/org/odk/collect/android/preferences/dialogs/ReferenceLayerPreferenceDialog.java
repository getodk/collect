package org.odk.collect.android.preferences.dialogs;

import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.ListPreferenceDialogFragmentCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.preferences.CaptionedListPreference;
import org.odk.collect.android.utilities.ExternalWebPageHelper;

public class ReferenceLayerPreferenceDialog extends ListPreferenceDialogFragmentCompat implements DialogInterface.OnClickListener {

    /**
     * Views should not be stored statically like this. The relationship on how the list is setup
     * here should be inverted - {@link ReferenceLayerPreferenceDialog} should be asking
     * {@link CaptionedListPreference} for items  rather than having them pushed through this static
     * field.
     */
    @Deprecated
    public static ViewGroup listView;

    public static ReferenceLayerPreferenceDialog newInstance(String key) {
        ReferenceLayerPreferenceDialog fragment = new ReferenceLayerPreferenceDialog();
        Bundle b = new Bundle(1);
        b.putString(ARG_KEY, key);
        fragment.setArguments(b);
        return fragment;
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        // Selecting an item will close the dialog, so we don't need the "OK" button.
        builder.setPositiveButton(null, null);
    }

    /** Called just after the dialog's main view has been created. */
    @Override
    protected void onBindDialogView(View view) {
        CaptionedListPreference preference = null;
        if (getPreference() instanceof CaptionedListPreference) {
            preference = (CaptionedListPreference) getPreference();
        }

        addHelpFooter(view);

        listView = view.findViewById(R.id.list);

        if (preference != null) {
            preference.updateContent();
        }

        super.onBindDialogView(view);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        super.onClick(dialog, which);
        listView = null;
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        listView = null;
        if (getDialog() != null) {
            getDialog().dismiss();
        }
    }

    private void addHelpFooter(View view) {
        LinearLayout layout = (LinearLayout) view;
        View helpFooter = LayoutInflater.from(requireContext()).inflate(R.layout.reference_layer_help_footer, layout, false);
        helpFooter.findViewById(R.id.help_button).setOnClickListener(v -> {
            new ExternalWebPageHelper().openWebPageInCustomTab(requireActivity(), Uri.parse("https://docs.getodk.org/collect-offline-maps/#transferring-offline-tilesets-to-devices"));
        });
        layout.addView(helpFooter);
    }
}
