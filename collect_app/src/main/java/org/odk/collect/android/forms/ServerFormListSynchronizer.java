package org.odk.collect.android.forms;

import org.odk.collect.android.logic.ManifestFile;
import org.odk.collect.android.logic.MediaFile;
import org.odk.collect.android.openrosa.api.FormAPI;
import org.odk.collect.android.openrosa.api.FormAPIError;
import org.odk.collect.android.openrosa.api.FormListItem;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FormDownloader;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import timber.log.Timber;

import static java.util.Arrays.asList;
import static org.odk.collect.android.logic.FormDetails.toFormDetails;

public class ServerFormListSynchronizer {

    private final FormRepository formRepository;
    private final MediaFileRepository mediaFileRepository;
    private final FormAPI formAPI;
    private final FormDownloader formDownloader;

    public ServerFormListSynchronizer(FormRepository formRepository, MediaFileRepository mediaFileRepository, FormAPI formAPI, FormDownloader formDownloader) {
        this.formRepository = formRepository;
        this.mediaFileRepository = mediaFileRepository;
        this.formAPI = formAPI;
        this.formDownloader = formDownloader;
    }

    public void synchronize() {
        try {
            List<FormListItem> formsOnServer = formAPI.fetchFormList();
            List<Form> formsOnDevice = formRepository.getAll();

            deleteFormsNotServer(formsOnServer, formsOnDevice);
            downloadNewAndUpdatedForms(formsOnServer, formsOnDevice);
        } catch (FormAPIError error) {
            Timber.e(error);
        }
    }

    private void deleteFormsNotServer(List<FormListItem> formList, List<Form> formsOnDevice) {
        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getJrFormId().equals(f.getFormID()))) {
                formRepository.delete(form.getId());
            }
        });
    }

    private void downloadNewAndUpdatedForms(List<FormListItem> formsOnServer, List<Form> formsOnDevice) throws FormAPIError {
        for (FormListItem formOnServer : formsOnServer) {
            Optional<Form> formOnDevice = formsOnDevice.stream().filter(f -> f.getJrFormId().equals(formOnServer.getFormID())).findFirst();

            if (!formOnDevice.isPresent() || !formOnDevice.get().getMD5Hash().equals(formOnServer.getHashWithPrefix().split(":")[1])) {
                formDownloader.downloadForms(asList(toFormDetails(formOnServer, null, false, false)), null);
            } else if (formOnServer.getManifestURL() != null) {
                ManifestFile serverManifest = formAPI.fetchManifest(formOnServer.getManifestURL());

                if (isManifestDifferent(formOnDevice.get(), serverManifest)) {
                    formDownloader.downloadForms(asList(toFormDetails(formOnServer, null, false, false)), null);
                }
            }
        }
    }

    private boolean isManifestDifferent(Form formOnDevice, ManifestFile serverManifest) {
        List<File> mediaFilesOnDevice = mediaFileRepository.getAll(formOnDevice.getJrFormId(), formOnDevice.getJrVersion());
        List<String> deviceHashes = mediaFilesOnDevice.stream().map(FileUtils::getMd5Hash).collect(Collectors.toList());

        for (MediaFile mediaFile : serverManifest.getMediaFiles()) {
            if (!deviceHashes.contains(mediaFile.getHash())) {
                return true;
            }
        }

        return false;
    }
}
