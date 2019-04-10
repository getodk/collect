/*
 * Copyright 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.odk.collect.android.utilities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Build;
import androidx.core.app.NotificationCompat;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

public class NotificationUtils {

    public static final String CHANNEL_ID = "collect_notification_channel";
    public static final int FORM_UPDATE_NOTIFICATION_ID = 0;

    private NotificationUtils() {
    }

    public static void createNotificationChannel(Collect collect) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = collect.getSystemService(NotificationManager.class);

            if (notificationManager != null) {
                notificationManager.createNotificationChannel(new NotificationChannel(
                                        CHANNEL_ID,
                                        collect.getString(R.string.notification_channel_name),
                                        NotificationManager.IMPORTANCE_DEFAULT)
                );
            }
        }
    }

    public static void showNotification(PendingIntent contentIntent,
                                        int notificationId,
                                        int title,
                                        String contentText) {
        Context context = Collect.getInstance();

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID).setContentIntent(contentIntent);

        builder
                .setContentTitle(context.getString(title))
                .setContentText(contentText)
                .setSmallIcon(IconUtils.getNotificationAppIcon())
                .setAutoCancel(true);

        NotificationManager manager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(notificationId, builder.build());
        }
    }
}
