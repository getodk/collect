package org.odk.collect.android.fragments;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.jetbrains.annotations.NotNull;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.tasks.MediaLoadingTask;

public class MediaLoadingFragment extends Fragment {

    private MediaLoadingTask mediaLoadingTask;

    public void beginMediaLoadingTask(Uri uri, FormController formController) {
        mediaLoadingTask = new MediaLoadingTask((FormEntryActivity) getActivity(), formController.getInstanceFile());
        mediaLoadingTask.execute(uri);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onAttach(@NotNull Context context) {
        super.onAttach(context);
        if (mediaLoadingTask != null) {
            mediaLoadingTask.onAttach((FormEntryActivity) getActivity());
        }
    }

    public boolean isMediaLoadingTaskRunning() {
        return mediaLoadingTask != null && mediaLoadingTask.getStatus() == AsyncTask.Status.RUNNING;
    }
}
