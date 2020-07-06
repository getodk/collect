package org.odk.collect.android.preferences;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormListSynchronizer;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListApi;

import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import timber.log.Timber;

public class ExperimentalPreferencesFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);
    }



    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        FragmentActivity activity = getActivity();
        if (activity instanceof CollectAbstractActivity) {
            ((CollectAbstractActivity) activity).initToolbar(getPreferenceScreen().getTitle());
        }

        SwitchPreferenceCompat matchExactly = findPreference("match_exactly");
        matchExactly.setOnPreferenceChangeListener((preference, newValue) -> {
            if ((Boolean) newValue) {
                Constraints constraints = new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build();

                PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SyncWorker.class, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build();

                WorkManager.getInstance(activity).enqueueUniquePeriodicWork("match_exactly", ExistingPeriodicWorkPolicy.REPLACE, workRequest);

                Toast.makeText(activity, "Enqueuing work...", Toast.LENGTH_LONG).show();
            }

            return true;
        });
    }

    public static class SyncWorker extends Worker {

        @Inject
        FormRepository formRepository;

        @Inject
        MediaFileRepository mediaFileRepository;

        @Inject
        FormListApi formAPI;

        @Inject
        FormDownloader formDownloader;

        public SyncWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
            super(context, workerParams);
            DaggerUtils.getComponent(context).inject(this);
        }

        @NonNull
        @Override
        public Result doWork() {
            try {
                new ServerFormListSynchronizer(formRepository, mediaFileRepository, formAPI, formDownloader).synchronize();
            } catch (FormApiException formAPIError) {
                Timber.e(formAPIError);
            }

            return Result.success();
        }
    }
}
