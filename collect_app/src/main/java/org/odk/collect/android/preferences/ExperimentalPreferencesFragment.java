package org.odk.collect.android.preferences;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.SwitchPreferenceCompat;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.formmanagement.DiskFormsSynchronizer;
import org.odk.collect.android.formmanagement.FormDownloader;
import org.odk.collect.android.formmanagement.ServerFormListSynchronizer;
import org.odk.collect.android.forms.FormRepository;
import org.odk.collect.android.forms.MediaFileRepository;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.openrosa.api.FormApiException;
import org.odk.collect.android.openrosa.api.FormListApi;
import org.odk.collect.async.Scheduler;
import org.odk.collect.async.TaskSpec;
import org.odk.collect.async.WorkerAdapter;

import javax.inject.Inject;

import timber.log.Timber;

public class ExperimentalPreferencesFragment extends PreferenceFragmentCompat {

    @Inject
    Scheduler scheduler;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.experimental_preferences, rootKey);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        DaggerUtils.getComponent(context).inject(this);
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
                scheduler.scheduleInBackground("match_exactly", new SyncTaskSpec(), 900000L);
            }

            return true;
        });
    }

    public static class SyncTaskSpec implements TaskSpec {

        @Inject
        FormRepository formRepository;

        @Inject
        MediaFileRepository mediaFileRepository;

        @Inject
        FormListApi formAPI;

        @Inject
        FormDownloader formDownloader;

        @NotNull
        @Override
        public Runnable getTask(@NotNull Context context) {
            DaggerUtils.getComponent(context).inject(this);

            return () -> {
                try {
                    new ServerFormListSynchronizer(formRepository, mediaFileRepository, formAPI, formDownloader, new DiskFormsSynchronizer()).synchronize();
                } catch (FormApiException formAPIError) {
                    Timber.e(formAPIError);
                }
            };
        }

        @NotNull
        @Override
        public Class<? extends WorkerAdapter> getWorkManagerAdapter() {
            return Adapter.class;
        }

        public static class Adapter extends WorkerAdapter {

            public Adapter(@NotNull Context context, @NotNull WorkerParameters workerParams) {
                super(new SyncTaskSpec(), context, workerParams);
            }
        }
    }
}
