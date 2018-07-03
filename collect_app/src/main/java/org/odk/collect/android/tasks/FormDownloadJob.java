package org.odk.collect.android.tasks;

import android.os.Bundle;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;

import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FormDownloadBroadcastHelper;
import org.odk.collect.android.utilities.FormDownloader;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

/**
 * Created by Ephraim Kigamba - ekigamba@ona.io on 03/07/2018
 */

public class FormDownloadJob extends Job {

    public static final String TAG = "FORM_DOWNLOAD_TAG";
    private String formId;

    @NonNull
    @Override
    protected Result onRunJob(@NonNull Params params) {
        Bundle bundle = params.getTransientExtras();
        if (bundle.containsKey(ApplicationConstants.BundleKeys.FORM_ID)) {
            formId = bundle.getString(ApplicationConstants.BundleKeys.FORM_ID);

            Timber.i("STARTED RUNNING JOB -> Download Form %s", formId);

            HashMap<String, FormDetails> formDetailsHashMap = DownloadFormListUtils.downloadFormList(false);

            if (formDetailsHashMap.containsKey(formId)) {
                FormDetails formDetails = formDetailsHashMap.get(formId);

                ArrayList<FormDetails> formDetailsArrayList = new ArrayList<>();
                formDetailsArrayList.add(formDetails);

                FormDownloader formDownloader = new FormDownloader();
                formDownloader.downloadForms(formDetailsArrayList);

                Timber.i("FINISHED DOWNLOADING FORM : %s", formId);
                FormDownloadBroadcastHelper.sendDownloadServiceBroadcastResult(getContext(), formId, true, null);
            } else {
                Timber.e("DOWNLOAD FORM FAILED BECAUSE FORM DOES NOT EXIST ON THE SERVER");
                FormDownloadBroadcastHelper.sendDownloadServiceBroadcastResult(getContext(), formId,false, "Requested form could not be found");
            }

            return Result.SUCCESS;
        } else {
            Timber.e("DOWNLOAD FORM FAILED BECAUSE BUNDLE DOES NOT CONTAIN FORM_ID");
            FormDownloadBroadcastHelper.sendDownloadServiceBroadcastResult(getContext(), formId,false, "Bundle does not contain the " + ApplicationConstants.BundleKeys.FORM_ID);
            return Result.FAILURE;
        }
    }
}
