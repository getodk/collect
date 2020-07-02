package odk.hedera.collect.preferences.utilities;

import android.app.Activity;

import org.odk.hedera.collect.R;
import odk.hedera.collect.activities.MainMenuActivity;
import odk.hedera.collect.application.Collect;
import odk.hedera.collect.listeners.ActionListener;
import odk.hedera.collect.preferences.AdminSharedPreferences;
import odk.hedera.collect.preferences.GeneralSharedPreferences;
import odk.hedera.collect.preferences.PreferenceSaver;
import odk.hedera.collect.utilities.LocaleHelper;
import odk.hedera.collect.utilities.ToastUtils;

import timber.log.Timber;

import static odk.hedera.collect.activities.ActivityUtils.startActivityAndCloseAllOthers;

public class SettingsUtils {

    private SettingsUtils() {
    }

    public static void applySettings(Activity activity, String content) {
        new PreferenceSaver(GeneralSharedPreferences.getInstance(), AdminSharedPreferences.getInstance()).fromJSON(content, new ActionListener() {
            @Override
            public void onSuccess() {
                Collect.getInstance().initializeJavaRosa();
                ToastUtils.showLongToast(Collect.getInstance().getString(R.string.successfully_imported_settings));
                final LocaleHelper localeHelper = new LocaleHelper();
                localeHelper.updateLocale(activity);
                startActivityAndCloseAllOthers(activity, MainMenuActivity.class);
            }

            @Override
            public void onFailure(Exception exception) {
                if (exception instanceof GeneralSharedPreferences.ValidationException) {
                    ToastUtils.showLongToast(Collect.getInstance().getString(R.string.invalid_qrcode));
                } else {
                    Timber.e(exception);
                }
            }
        });
    }
}
