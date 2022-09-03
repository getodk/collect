
package org.odk.collect.android.tasks;

import android.os.AsyncTask;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;

import org.odk.collect.android.amazonaws.mobile.AWSMobileClient;
import org.odk.collect.android.amazonaws.models.nosql.DevicesDO;
import org.odk.collect.android.application.Collect;

import timber.log.Timber;

import static java.lang.Thread.sleep;

/**
 * Background task for getting values from the server
 */
public class SmapRegisterForMessagingTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground(String... params) {

        String token = params[0];
        String server = params[1];
        String username = params[2];

        int timeout = 0;

        try {

            Timber.i("================================================== Notifying server of messaging update");
            Timber.i("    token: %s", token);
            Timber.i("    server: %s", server);
            Timber.i("    user: %s", username);
            AWSMobileClient.initializeMobileClientIfNecessary(Collect.getInstance());
            final DynamoDBMapper mapper = AWSMobileClient.defaultMobileClient().getDynamoDBMapper();
            DevicesDO devices = new DevicesDO();
            devices.setRegistrationId(token);
            devices.setSmapServer(server);
            devices.setUserIdent(username);
            mapper.save(devices);
            Timber.i("================================================== Notifying server of messaging update done");

        } catch (Exception e) {
            Timber.e(e);

        } finally {


        }
        return "done";
    }

}