package org.odk.collect.android.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONException;
import org.json.JSONObject;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.LoginCompleteListener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginTask extends AsyncTask<String, Void, String> {

    private LoginCompleteListener loginCompleteListener;
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";

    @Override
    protected String doInBackground(String... strings) {
        try {
            /*String username = strings[0];
            String password = strings[1];*/
            String url = Collect.getInstance().getResources().getString(R.string.local_login_url);
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(url);
            String json;
            JSONObject jsonObject = new JSONObject();
            /*jsonObject.accumulate("username", username);
            jsonObject.accumulate("password", password);*/
            jsonObject.accumulate(KEY_USERNAME, "sampathuser");
            jsonObject.accumulate(KEY_PASSWORD, "12345");

            json = jsonObject.toString();

            StringEntity se = new StringEntity(json);
            httpPost.setEntity(se);

            httpPost.setHeader("Accept", "application/json");
            httpPost.setHeader("Content-type", "application/json");

            HttpResponse httpResponse = httpclient.execute(httpPost);

            InputStream inputStream = httpResponse.getEntity().getContent();
            return convertInputStreamToString(inputStream);

        } catch (IOException | JSONException exception) {
            Log.e("LoginTask", exception.getMessage());
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
        loginCompleteListener.loginCompleteListener(result);
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

    public void setLoginCompleteListener(LoginCompleteListener listener) {
        loginCompleteListener = listener;
    }
}
