package org.odk.collect.android.services;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.logic.FormDetails;
import org.odk.collect.android.tasks.DownloadFormsTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by sahil on 28/2/18.
 */

public class DownloadFormService extends Service implements FormDownloaderListener{

    private static final String FORM_LIST = "formList" ;
    private static final String ACTION_DOWNLOAD_FORM = "downloadForm";
    private static final String ACTION_CANCEL = "cancelFormDownload";
    private DownloadFormsTask downloadFormsTask;
    private NotificationManagerCompat notificationManagerCompat;
    private static final int DOWNLOADING_NOTIFICATION = 112121;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManagerCompat = NotificationManagerCompat.from(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        switch (intent.getAction()){
            case ACTION_DOWNLOAD_FORM:
                ArrayList<FormDetails> list = intent.getExtras().getParcelableArrayList(FORM_LIST);
                downloadFormsTask = new DownloadFormsTask();
                downloadFormsTask.setDownloaderListener(this);
                downloadFormsTask.execute(list);
                return Service.START_NOT_STICKY;
            case ACTION_CANCEL:
                formsDownloadingCancelled();
                return  Service.START_NOT_STICKY;
            default:
                return super.onStartCommand(intent, flags, startId);
        }


    }


    @Override
    public void formsDownloadingComplete(HashMap<FormDetails, String> result) {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
        }

        Set<FormDetails> keys = result.keySet();
        StringBuilder b = new StringBuilder();
        for (FormDetails k : keys) {
            b.append(k.getFormName() + " ("
                    + ((k.getFormVersion() != null)
                    ? (this.getString(R.string.version) + ": " + k.getFormVersion() + " ")
                    : "") + "ID: " + k.getFormID() + ") - " + result.get(k));
            b.append("\n\n");
        }
        notificationManagerCompat.cancel(DOWNLOADING_NOTIFICATION);
        notificationManagerCompat.notify(DOWNLOADING_NOTIFICATION, notificationBuilder(this,b.toString()).build());

    }

    @Override
    public void progressUpdate(String currentFile, int progress, int total) {
           notificationManagerCompat.notify(DOWNLOADING_NOTIFICATION,notificationBuilder(this, getString(R.string.fetching_file, currentFile, String.valueOf(progress), String.valueOf(total))).build());
    }

    @Override
    public void formsDownloadingCancelled() {
        if (downloadFormsTask != null) {
            downloadFormsTask.setDownloaderListener(null);
            downloadFormsTask = null;
        }
        notificationManagerCompat.cancel(DOWNLOADING_NOTIFICATION);
        stopSelf();

    }

    private NotificationCompat.Builder notificationBuilder(Context context, String notificationContent){
        Intent cancelIntent = new Intent(this, DownloadFormService.class);
        cancelIntent.setAction(ACTION_CANCEL);
        PendingIntent cancelPendingIntent = PendingIntent.getService(context,0,cancelIntent,0);
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.notes)
                .setContentTitle(getString(R.string.downloading_data))
                .setContentText(notificationContent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .addAction(R.drawable.notes,getString(R.string.cancel_download_form),cancelPendingIntent);
        return mBuilder;
    }
}
