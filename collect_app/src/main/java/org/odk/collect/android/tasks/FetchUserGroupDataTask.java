package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.odk.collect.android.listeners.FetchUserGroupListener;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class FetchUserGroupDataTask extends AsyncTask<String, Void, String> {

    private FetchUserGroupListener fetchUserGroupListener;

    @Override
    protected String doInBackground(String... strings) {
        String token = strings[0];

        try {

            URL url = new URL("http://10.1.0.194:8000/accounts/api/v1/login/user-details/?token=" + token);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/json");
            try {
                InputStream in = new BufferedInputStream(urlConnection.getInputStream());
                return convertInputStreamToString(in);
            } finally {
                urlConnection.disconnect();
            }


        } catch (IOException exception) {
            Log.e("FetchUserDataTask", exception.getMessage());
            return exception.getMessage();
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        fetchUserGroupListener.fetchUserGroupDataCompleteListener(result);
        Log.i("tesn ", " " + result);
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
    }

    private static String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
        String line;
        String result = "";
        while ((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }

    public void setFetchUserGroupListener(FetchUserGroupListener listener) {
        fetchUserGroupListener = listener;
    }
}