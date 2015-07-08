/*
 * Copyright (C) 2014 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.tasks;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

import org.odk.collect.android.listeners.InstanceUploaderListener;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.GooglePlayServicesAvailabilityException;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
 * @author carlhartung (chartung@nafundi.com)
 * 
 */
public abstract class GoogleMapsEngineTask<Params, Progress, Result> extends
		AsyncTask<Params, Progress, Result> {

	private static String tag = "GoogleMapsEngineTask";

	public final static int PLAYSTORE_REQUEST_CODE = 55551;
	public final static int USER_RECOVERABLE_REQUEST_CODE = 55552;

	protected static final String gme_fail = "GME Error: ";

	protected String mGoogleUserName = null;
	protected InstanceUploaderListener mStateListener;

	public void setUserName(String username) {
		mGoogleUserName = username;
	}

	public void setUploaderListener(InstanceUploaderListener sl) {
		synchronized (this) {
			mStateListener = sl;
		}
	}

	protected String authenticate(Context context, String mGoogleUserName)
			throws IOException, GoogleAuthException,
			GooglePlayServicesAvailabilityException,
			UserRecoverableAuthException {
		// use google auth utils to get oauth2 token
		String scope = "oauth2:https://www.googleapis.com/auth/mapsengine https://picasaweb.google.com/data/";
		String token = null;

		if (mGoogleUserName == null) {
			Log.e(tag, "Google user not set");
			return null;
		}

		token = GoogleAuthUtil.getToken(context, mGoogleUserName, scope);
		return token;
	}

	protected static class Backoff {

		private static final long INITIAL_WAIT = 1000 + new Random()
				.nextInt(1000);
		private static final long MAX_BACKOFF = 1800 * 1000;

		private long mWaitInterval = INITIAL_WAIT;
		private boolean mBackingOff = true;

		public boolean shouldRetry() {
			return mBackingOff;
		}

		private void noRetry() {
			mBackingOff = false;
		}

		public void backoff() {
			if (mWaitInterval > MAX_BACKOFF) {
				noRetry();
			} else if (mWaitInterval > 0) {
				try {
					Thread.sleep(mWaitInterval);
				} catch (InterruptedException e) {

				}
			}

			mWaitInterval = (mWaitInterval == 0) ? INITIAL_WAIT
					: mWaitInterval * 2;
		}
	}

	protected static byte[] readStream(InputStream in) throws IOException {
		final byte[] buf = new byte[1024];
		int count = 0;
		ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
		while ((count = in.read(buf)) != -1) {
			out.write(buf, 0, count);
		}
		in.close();
		return out.toByteArray();
	}

	protected String getErrorMesssage(InputStream is) {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder jsonResponseBuilder = new StringBuilder();

	    String line = null;
	    try {
	      while ((line = br.readLine()) != null) {
	          jsonResponseBuilder.append(line + "\n");
	      }
	    } catch (IOException e) {
	      e.printStackTrace();
	    } finally {
	      try {
	        is.close();
	      } catch (IOException e) {
	        e.printStackTrace();
	      }
	    }
	    String jsonResponse = jsonResponseBuilder.toString();
	    Log.i(tag, "GME json response : " + jsonResponse);

		GMEErrorResponse errResp = gson.fromJson(jsonResponse, GMEErrorResponse.class);
		StringBuilder sb = new StringBuilder();
		sb.append(gme_fail + "\n");
		
		if (errResp.error.errors != null) {
    		for (int i = 0; i < errResp.error.errors.length; i++) {
    			sb.append(errResp.error.errors[i].message + "\n");
    		}
		} else {
		    sb.append(errResp.error.message + "\n");
		}
		return sb.toString();
	}

	// FOR JSON
	public static class FeaturesListResponse {
		public Feature[] features;

	}

	public static class Feature {
		public Geometry geometry;
		public Map<String, String> properties;
	}

	public static class Geometry {
		public String type;

	}

	public static class PointGeometry extends Geometry {
		public double[] coordinates;

		public void setCoordinates(double lat, double lon) {
			coordinates = new double[2];
			coordinates[0] = lat;
			coordinates[1] = lon;
		}
	}

	
	
	public class GMEErrorResponse {
	    public GMEError error;
	}
	
	public class GMEError {
	    public GMEInnerError[] errors;
        public String code;
        public String message;
        public String extendedHelp;
	}

	public class GMEInnerError {
		public String domain;
		public String reason;
		public String message;
		public String locationType;
		public String location;
	}

	public static class Table {
		public String id;
		public String name;
		public String description;
		public String projectId;
		public String tags[];
		public String sourceEncoding;
		public String draftAccessList;
		public String publishedAccessList;
		public String processingStatus;
		public Schema schema;
	}

	public static class Schema {
		public Column[] columns;
		public String primaryGeometry;
		public String primaryKey;
	}

	public static class Column {
		public String name;
		public String type;
	}

	public static class TablesListResponse {
		public Table[] tables;
		public String nextPageToken;
	}

	public static class FeatureSerializer implements JsonSerializer<Feature> {
		@Override
		public JsonElement serialize(final Feature feature,
				final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject json = new JsonObject();
			json.addProperty("type", "Feature");
			JsonElement geoElement = context.serialize(feature.geometry);
			json.add("geometry", geoElement);
			JsonElement propElement = context.serialize(feature.properties);
			json.add("properties", propElement);
			return json;
		}
	}

	public static class FeatureListSerializer implements
			JsonSerializer<ArrayList<Feature>> {
		@Override
		public JsonElement serialize(final ArrayList<Feature> features,
				final Type typeOfSrc, final JsonSerializationContext context) {
			final JsonObject json = new JsonObject();
			final JsonArray jsonArray = new JsonArray();

			for (final Feature feature : features) {
				final JsonElement element = context.serialize(feature);
				jsonArray.add(element);
			}
			json.add("features", jsonArray);
			return json;
		}
	}

}
