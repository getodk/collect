package org.odk.collect.android.ui.formdownload;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.utilities.DownloadFormListUtils;
import org.odk.collect.android.utilities.FormDownloader;
import org.odk.collect.android.utilities.providers.BaseResourceProvider;

import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import io.reactivex.Observable;
import io.reactivex.subjects.BehaviorSubject;
import timber.log.Timber;

@Singleton
public class FormDownloadRepository {

    private final DownloadFormListUtils downloadFormListUtils;
    private final FormDownloader formDownloader;
    private final BehaviorSubject<String> formDownloadProgressSubject;

    @Inject
    BaseResourceProvider resourceProvider;

    private boolean isLoading;

    @Inject
    FormDownloadRepository() {
        downloadFormListUtils = new DownloadFormListUtils();
        formDownloader = new FormDownloader();
        formDownloadProgressSubject = BehaviorSubject.create();
    }

    public boolean isLoading() {
        return isLoading;
    }

    public Observable<HashMap<String, FormDetails>> downloadFormList(String url, String username, String password) {
        return Observable.fromCallable(() -> downloadFormListUtils.downloadFormList(url, username, password, false))
                .doOnSubscribe(__ -> isLoading = true)
                .doOnNext(__ -> isLoading = false)
                .doOnTerminate(() -> isLoading = false)
                .doOnDispose(() -> isLoading = false)
                .doOnError(throwable -> {
                    isLoading = false;
                    Timber.e(throwable);
                });
    }

    public Observable<HashMap<FormDetails, String>> downloadForms(List<FormDetails> filesToDownload) {
        return Observable.fromCallable(() -> formDownloader.downloadForms(filesToDownload))
                .doOnSubscribe(disposable -> {
                    isLoading = true;

                    formDownloader.setDownloaderListener(new FormDownloaderListener() {
                        @Override
                        public void progressUpdate(String currentFile, String progress, String total) {
                            String message = resourceProvider.getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total));
                            formDownloadProgressSubject.onNext(message);
                        }

                        @Override
                        public boolean isTaskCanceled() {
                            return disposable.isDisposed();
                        }
                    });
                })
                .doOnNext(__ -> isLoading = false)
                .doOnTerminate(() -> isLoading = false)
                .doOnDispose(() -> isLoading = false)
                .doOnError(throwable -> {
                    isLoading = false;
                    Timber.e(throwable);
                });
    }

    public Observable<String> getFormDownloadProgress() {
        return formDownloadProgressSubject;
    }
}
