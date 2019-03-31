package org.odk.collect.android.ui.formdownload;

import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.DownloadFormListUtils;

import java.util.HashMap;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;

@Singleton
public class FormDownloadRepository {

    private final DownloadFormListUtils downloadFormListUtils;
    private boolean isLoading;

    @Inject
    public FormDownloadRepository() {
        downloadFormListUtils = new DownloadFormListUtils();
    }

    public Observable<HashMap<String, FormDetails>> downloadForms(String url, String username, String password) {
        return Observable.fromCallable(() -> downloadFormListUtils.downloadFormList(url, username, password, false))
                .doOnSubscribe(__ -> isLoading = true)
                .doOnNext(__ -> isLoading = false);
    }

    public boolean isLoading() {
        return isLoading;
    }
}
