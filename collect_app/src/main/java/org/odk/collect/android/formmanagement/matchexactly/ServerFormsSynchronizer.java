package org.odk.collect.android.formmanagement.matchexactly;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odk.collect.android.formmanagement.LocalFormUseCases;
import org.odk.collect.android.formmanagement.ServerFormDetails;
import org.odk.collect.android.formmanagement.download.FormDownloadException;
import org.odk.collect.android.formmanagement.download.FormDownloader;
import org.odk.collect.android.formmanagement.download.FormDownloaderKt;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.FormSource;
import org.odk.collect.forms.FormSourceException;
import org.odk.collect.forms.FormsRepository;
import org.odk.collect.forms.instances.InstancesRepository;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ServerFormsSynchronizer {

    private final FormsRepository formsRepository;
    private final InstancesRepository instancesRepository;
    private final FormDownloader formDownloader;
    private final ServerFormsDetailsFetcher serverFormsDetailsFetcher;
    private final FormSource formSource;

    public ServerFormsSynchronizer(ServerFormsDetailsFetcher serverFormsDetailsFetcher, FormsRepository formsRepository, InstancesRepository instancesRepository, FormDownloader formDownloader, FormSource formSource) {
        this.serverFormsDetailsFetcher = serverFormsDetailsFetcher;
        this.formsRepository = formsRepository;
        this.instancesRepository = instancesRepository;
        this.formDownloader = formDownloader;
        this.formSource = formSource;
    }

    public void synchronize() throws FormSourceException {
        List<ServerFormDetails> formList = serverFormsDetailsFetcher.fetchFormDetails(formsRepository, formSource);
        List<Form> formsOnDevice = formsRepository.getAll();
        formsOnDevice.stream().forEach(form -> {
            if (formList.stream().noneMatch(f -> form.getFormId().equals(f.getFormId()))) {
                LocalFormUseCases.deleteForm(formsRepository, instancesRepository, form.getDbId());
            }
        });

        List<ServerFormDetails> formsToDownload = formList
                .stream()
                .filter(form -> form.getType() != ServerFormDetails.Type.OnDevice)
                .toList();

        Map<@NotNull ServerFormDetails, @Nullable FormDownloadException> downloadResult = FormDownloaderKt.downloadForms(formDownloader, formsToDownload, null, null);
        boolean isError = downloadResult.values().stream().anyMatch(Objects::nonNull);
        if (isError) {
            throw new FormSourceException.FetchError();
        }
    }

    public interface ServerFormsDetailsFetcher {
        List<ServerFormDetails> fetchFormDetails(FormsRepository formsRepository, FormSource formSource) throws FormSourceException;
    }
}
