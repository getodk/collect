package org.odk.collect.android.formentry.saving;

import android.net.Uri;

import org.odk.collect.android.javarosawrapper.FormController;
import org.odk.collect.android.tasks.SaveFormToDisk;
import org.odk.collect.android.tasks.SaveToDiskResult;
import org.odk.collect.android.utilities.MediaUtils;
import org.odk.collect.entities.EntitiesRepository;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.ArrayList;

public class DiskFormSaver implements FormSaver {

    @Override
    public SaveToDiskResult save(Uri instanceContentURI, FormController formController, MediaUtils mediaUtils, boolean shouldFinalize, boolean exitAfter,
                                 String updatedSaveName, ProgressListener progressListener, ArrayList<String> tempFiles, String currentProjectId, EntitiesRepository entitiesRepository, InstancesRepository instancesRepository) {
        SaveFormToDisk saveFormToDisk = new SaveFormToDisk(formController, mediaUtils, exitAfter, shouldFinalize,
                updatedSaveName, instanceContentURI, tempFiles, currentProjectId, entitiesRepository, instancesRepository);
        return saveFormToDisk.saveForm(progressListener);
    }
}
