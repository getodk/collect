package org.odk.collect.android.preferences;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.inputmethod.EditorInfo;

import androidx.fragment.app.FragmentActivity;
import androidx.preference.EditTextPreference;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.listeners.PermissionListener;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.metadata.InstallIDProvider;
import org.odk.collect.android.utilities.PermissionUtils;
import org.odk.collect.android.utilities.ToastUtils;
import org.odk.collect.android.utilities.Validator;

import javax.inject.Inject;

import static org.odk.collect.android.logic.PropertyManager.PROPMGR_DEVICE_ID;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_PHONE_NUMBER;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SIM_SERIAL;
import static org.odk.collect.android.logic.PropertyManager.PROPMGR_SUBSCRIBER_ID;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_EMAIL;
import static org.odk.collect.android.preferences.GeneralKeys.KEY_METADATA_PHONENUMBER;
import static org.odk.collect.android.preferences.MetaKeys.KEY_INSTALL_ID;

public class FormMetadataFragment extends PreferenceFragmentCompat {

    @Inject
    InstallIDProvider installIDProvider;

    @Inject
    PermissionUtils permissionUtils;

    private Preference emailPreference;
    private EditTextPreference phonePreference;
    private Preference installIDPreference;
    private Preference deviceIDPreference;
    private Preference simSerialPrererence;
    private Preference subscriberIDPreference;


    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.form_metadata_preferences, rootKey);

        emailPreference = findPreference(KEY_METADATA_EMAIL);
        phonePreference = findPreference(KEY_METADATA_PHONENUMBER);
        installIDPreference = findPreference(KEY_INSTALL_ID);
        deviceIDPreference = findPreference(PROPMGR_DEVICE_ID);
        simSerialPrererence = findPreference(PROPMGR_SIM_SERIAL);
        subscriberIDPreference = findPreference(PROPMGR_SUBSCRIBER_ID);

        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPrefs();

        if (permissionUtils.isReadPhoneStatePermissionGranted(getActivity())) {
            setupPrefsWithPermissions();
        } else if (savedInstanceState == null) {
            permissionUtils.requestReadPhoneStatePermission(getActivity(), true, new PermissionListener() {
                @Override
                public void granted() {
                    setupPrefsWithPermissions();
                }

                @Override
                public void denied() {
                }
            });
        }
    }

    private void setupPrefs() {
        emailPreference.setOnPreferenceChangeListener((preference, newValue) -> {
            String newValueString = newValue.toString();
            if (!newValueString.isEmpty() && !Validator.isEmailAddressValid(newValueString)) {
                ToastUtils.showLongToast(R.string.invalid_email_address);
                return false;
            }

            return true;
        });

        phonePreference.setOnBindEditTextListener(editText -> editText.setInputType(EditorInfo.TYPE_CLASS_PHONE));
        installIDPreference.setSummaryProvider(preference -> installIDProvider.getInstallID());
    }

    private void setupPrefsWithPermissions() {
        PropertyManager propertyManager = new PropertyManager(getActivity());
        deviceIDPreference.setSummaryProvider(new PropertyManagerPropertySummaryProvider(propertyManager, PROPMGR_DEVICE_ID));
        simSerialPrererence.setSummaryProvider(new PropertyManagerPropertySummaryProvider(propertyManager, PROPMGR_SIM_SERIAL));
        subscriberIDPreference.setSummaryProvider(new PropertyManagerPropertySummaryProvider(propertyManager, PROPMGR_SUBSCRIBER_ID));
        phonePreference.setSummaryProvider(new PropertyManagerPropertySummaryProvider(propertyManager, PROPMGR_PHONE_NUMBER));
    }

    private class PropertyManagerPropertySummaryProvider implements Preference.SummaryProvider<EditTextPreference> {

        private final PropertyManager propertyManager;
        private final String propertyKey;

        PropertyManagerPropertySummaryProvider(PropertyManager propertyManager, String propertyName) {
            this.propertyManager = propertyManager;
            this.propertyKey = propertyName;
        }

        @Override
        public CharSequence provideSummary(EditTextPreference preference) {
            String value = propertyManager.reload(getActivity()).getSingularProperty(propertyKey);
            if (!TextUtils.isEmpty(value)) {
                return value;
            } else {
                return getString(R.string.preference_not_available);
            }
        }
    }
}
