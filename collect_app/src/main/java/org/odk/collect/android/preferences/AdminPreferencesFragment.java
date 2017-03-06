package org.odk.collect.android.preferences;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.ToastUtils;

import static android.content.Context.MODE_PRIVATE;
import static android.content.Context.MODE_WORLD_READABLE;
import static org.odk.collect.android.preferences.AdminKeys.KEY_ADMIN_PW;
import static org.odk.collect.android.preferences.AdminKeys.KEY_CHANGE_ADMIN_PASSWORD;
import static org.odk.collect.android.preferences.AdminKeys.KEY_FORM_PROCESSING_LOGIC;


public class AdminPreferencesFragment extends PreferenceFragment implements Preference.OnPreferenceClickListener, Preference.OnPreferenceChangeListener {

    public static String ADMIN_PREFERENCES = "admin_prefs";

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PreferenceManager prefMgr = getPreferenceManager();
        prefMgr.setSharedPreferencesName(ADMIN_PREFERENCES);
        prefMgr.setSharedPreferencesMode(MODE_WORLD_READABLE);

        addPreferencesFromResource(R.xml.admin_preferences);

        ListPreference mFormProcessingLogicPreference = (ListPreference) findPreference(KEY_FORM_PROCESSING_LOGIC);
        mFormProcessingLogicPreference.setSummary(mFormProcessingLogicPreference.getEntry());
        mFormProcessingLogicPreference.setOnPreferenceChangeListener(this);

        Preference mChangeAdminPwPreference = findPreference(KEY_CHANGE_ADMIN_PASSWORD);
        mChangeAdminPwPreference.setOnPreferenceClickListener(this);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {

        if (preference.getKey().equals(KEY_CHANGE_ADMIN_PASSWORD)) {
            AlertDialog.Builder b = new AlertDialog.Builder(getActivity());

            LayoutInflater factory = LayoutInflater.from(getActivity());
            final View dialogView = factory.inflate(R.layout.password_dialog_layout, null);

            final EditText passwordEditText = (EditText) dialogView.findViewById(R.id.pwd_field);
            final EditText verifyEditText = (EditText) dialogView.findViewById(R.id.verify_field);

            b.setTitle(R.string.change_admin_password);
            b.setView(dialogView);
            b.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String pw = passwordEditText.getText().toString();
                    String ver = verifyEditText.getText().toString();

                    if (!pw.equalsIgnoreCase("") && !ver.equalsIgnoreCase("") && pw.equals(ver)) {
                        // passwords are the same
                        SharedPreferences.Editor editor = getActivity().
                                getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString(KEY_ADMIN_PW, pw);
                        ToastUtils.makeShortText(String.valueOf(R.string.admin_password_changed));
                        editor.commit();
                        dialog.dismiss();
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "AdminPasswordDialog", "CHANGED");
                    } else if (pw.equalsIgnoreCase("") && ver.equalsIgnoreCase("")) {
                        SharedPreferences.Editor editor = getActivity().
                                getSharedPreferences(ADMIN_PREFERENCES, MODE_PRIVATE).edit();
                        editor.putString(KEY_ADMIN_PW, "");
                        editor.commit();
                        ToastUtils.makeShortText(String.valueOf(R.string.admin_password_disabled));
                        dialog.dismiss();
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "AdminPasswordDialog", "DISABLED");
                    } else {
                        ToastUtils.makeShortText(String.valueOf(R.string.admin_password_mismatch));
                        Collect.getInstance().getActivityLogger()
                                .logAction(this, "AdminPasswordDialog", "MISMATCH");
                    }
                }
            });
            b.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Collect.getInstance().getActivityLogger().logAction(this, "AdminPasswordDialog", "CANCELLED");
                }
            });

            b.setCancelable(false);
            AlertDialog dialog = b.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            dialog.show();
        }
        return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {

        if (preference.getKey().equals(KEY_FORM_PROCESSING_LOGIC)) {
            int index = ((ListPreference) preference).findIndexOfValue(
                    newValue.toString());
            String entry = (String) ((ListPreference) preference).getEntries()[index];
            preference.setSummary(entry);
        }
        return true;
    }


}
