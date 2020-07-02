package odk.hedera.collect.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;

import odk.hedera.collect.activities.FormEntryActivity;
import odk.hedera.collect.network.NetworkStateProvider;
import odk.hedera.collect.tasks.MediaLoadingTask;

public class MediaLoadingFragment extends Fragment {

    private MediaLoadingTask mediaLoadingTask;
    private FormEntryActivity formEntryActivity;

    public void beginMediaLoadingTask(Uri uri, NetworkStateProvider connectivityProvider) {
        mediaLoadingTask = new MediaLoadingTask(formEntryActivity, connectivityProvider);
        mediaLoadingTask.execute(uri);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.formEntryActivity = (FormEntryActivity) activity;
        if (mediaLoadingTask != null) {
            mediaLoadingTask.onAttach(formEntryActivity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (mediaLoadingTask != null) {
            mediaLoadingTask.onDetach();
        }
    }

    public boolean isMediaLoadingTaskRunning() {
        return mediaLoadingTask != null && mediaLoadingTask.getStatus() == AsyncTask.Status.RUNNING;
    }
}
