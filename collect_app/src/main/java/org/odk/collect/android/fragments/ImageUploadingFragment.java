package org.odk.collect.android.fragments;

import android.app.Activity;
import android.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;

import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.tasks.ImageUploadingTask;

public class ImageUploadingFragment extends Fragment {

    private ImageUploadingTask imageUploadingTask;
    private FormEntryActivity formEntryActivity;

    public void beginImageUploadingTask(Uri imageURi) {
        imageUploadingTask = new ImageUploadingTask(formEntryActivity);
        imageUploadingTask.execute(imageURi);
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
        if (imageUploadingTask != null) {
            imageUploadingTask.onAttach(formEntryActivity);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        if (imageUploadingTask != null) {
            imageUploadingTask.onDetach();
        }
    }
}
