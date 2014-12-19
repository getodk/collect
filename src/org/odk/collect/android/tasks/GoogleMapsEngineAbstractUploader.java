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

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.FormException;
import org.odk.collect.android.exception.GeoPointNotFoundException;
import org.odk.collect.android.picasa.AlbumEntry;
import org.odk.collect.android.picasa.AlbumFeed;
import org.odk.collect.android.picasa.PhotoEntry;
import org.odk.collect.android.picasa.PicasaClient;
import org.odk.collect.android.picasa.PicasaUrl;
import org.odk.collect.android.picasa.UserFeed;
import org.odk.collect.android.preferences.PreferencesActivity;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.MediaStore.Images;
import android.util.Log;
import android.util.Xml;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * 
 * @author carlhartung (chartung@nafundi.com)
 * 
 */
public abstract class GoogleMapsEngineAbstractUploader<Params, Progress, Result>
		extends GoogleMapsEngineTask<Long, Integer, HashMap<String, String>> {

	private final static String tag = "GoogleMapsEngineInstanceUploaderTask";

	protected HashMap<String, String> mResults;

	protected static final String picasa_fail = "Picasa Error: ";
	protected static final String oauth_fail = "OAUTH Error: ";
	protected static final String form_fail = "Form Error: ";

	private final static String PROJECT_ID = "projectid";

	/*
	 * By default, GME has a rate limit of 1 request/sec, so we've added GME_SLEEP_TIME
	 * to make sure we stay within that limit
	 * The production version of ODK Collect is not rate limited by GME, and is reflected
	 * in the code below.  
	 * You should change this if working on a non Google Play version of Collect.
	 */
	// dev
	//private static final int GME_SLEEP_TIME = 1100;

	// prod
	private static final int GME_SLEEP_TIME = 1;
	
	// As of August 2014 there was a known issue in GME that returns an error 
	// if a request comes in too soon after creating a table.
	// This delay prevents that error
	// see "known issues at the bottom of this page:
	// https://developers.google.com/maps-engine/documentation/table-create
	private static final int GME_CREATE_TABLE_DELAY = 4000;

	/**
	 * @param selection
	 * @param selectionArgs
	 * @param token
	 */
	protected void uploadInstances(String selection, String[] selectionArgs,
			String token) {
		SharedPreferences appSharedPrefs = PreferenceManager
				.getDefaultSharedPreferences(Collect.getInstance()
						.getApplicationContext());

		Cursor c = null;
		try {
			c = Collect
					.getInstance()
					.getContentResolver()
					.query(InstanceColumns.CONTENT_URI, null, selection,
							selectionArgs, null);

			if (c.getCount() > 0) {
				c.moveToPosition(-1);
				while (c.moveToNext()) {
					if (isCancelled()) {
						return;
					}
					String instance = c
							.getString(c
									.getColumnIndex(InstanceColumns.INSTANCE_FILE_PATH));
					String id = c.getString(c
							.getColumnIndex(InstanceColumns._ID));
					String jrformid = c.getString(c
							.getColumnIndex(InstanceColumns.JR_FORM_ID));
					Uri toUpdate = Uri.withAppendedPath(
							InstanceColumns.CONTENT_URI, id);
					ContentValues cv = new ContentValues();

					String formSelection = FormsColumns.JR_FORM_ID + "=?";
					String[] formSelectionArgs = { jrformid };
					Cursor formcursor = Collect
							.getInstance()
							.getContentResolver()
							.query(FormsColumns.CONTENT_URI, null,
									formSelection, formSelectionArgs, null);
					String md5 = null;
					String formFilePath = null;
					if (formcursor.getCount() > 0) {
						formcursor.moveToFirst();
						md5 = formcursor.getString(formcursor
								.getColumnIndex(FormsColumns.MD5_HASH));
						formFilePath = formcursor.getString(formcursor
								.getColumnIndex(FormsColumns.FORM_FILE_PATH));
					}

					if (md5 == null) {
						// fail and exit
						Log.e(tag, "no md5");
						return;
					}

					// get projectID and draftaccesslist from preferences
					// these may get overwritten per form
					// so pull the value each time
					HashMap<String, String> gmeFormValues = new HashMap<String, String>();
					String projectid = appSharedPrefs.getString(
							PreferencesActivity.KEY_GME_PROJECT_ID, null);
					gmeFormValues.put(PROJECT_ID, projectid);

					publishProgress(c.getPosition() + 1, c.getCount());
					if (!uploadOneSubmission(id, instance, jrformid, token,
							gmeFormValues, md5, formFilePath)) {
						cv.put(InstanceColumns.STATUS,
								InstanceProviderAPI.STATUS_SUBMISSION_FAILED);
						Collect.getInstance().getContentResolver()
								.update(toUpdate, cv, null, null);
						return;
					} else {
						cv.put(InstanceColumns.STATUS,
								InstanceProviderAPI.STATUS_SUBMITTED);
						Collect.getInstance().getContentResolver()
								.update(toUpdate, cv, null, null);
					}
				}
			}
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	private boolean uploadOneSubmission(String id, String instanceFilePath,
			String jrFormId, String token,
			HashMap<String, String> gmeFormValues, String md5,
			String formFilePath) {
		// if the token is null fail immediately
		if (token == null) {
			mResults.put(
					id,
					oauth_fail
							+ Collect.getInstance().getString(
									R.string.invalid_oauth));
			return false;
		}

		HashMap<String, String> answersToUpload = new HashMap<String, String>();
		HashMap<String, String> photosToUpload = new HashMap<String, String>();
		HashMap<String, PhotoEntry> uploadedPhotos = new HashMap<String, PhotoEntry>();

		HttpTransport h = AndroidHttp.newCompatibleTransport();
		GoogleCredential gc = new GoogleCredential();
		gc.setAccessToken(token);

		PicasaClient client = new PicasaClient(h.createRequestFactory(gc));
		String gmeTableId = null;

		// get instance file
		File instanceFile = new File(instanceFilePath);

		// parses the instance file and populates the answers and photos
		// hashmaps. Also extracts the projectid and draftaccess list if
		// defined
		try {
			processXMLFile(instanceFile, answersToUpload, photosToUpload,
					gmeFormValues);
		} catch (XmlPullParserException e) {
			e.printStackTrace();
			mResults.put(id, form_fail + e.getMessage());
			return false;
		} catch (FormException e) {
			mResults.put(
					id,
					form_fail
							+ Collect.getInstance().getString(
									R.string.gme_repeat_error));
			return false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			mResults.put(id, form_fail + e.getMessage());
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			mResults.put(id, form_fail + e.getMessage());
			return false;
		}

		// at this point, we should have a projectid either from
		// the settings or the form
		// if not, fail
		if (gmeFormValues.get(PROJECT_ID) == null) {
			mResults.put(
					id,
					form_fail
							+ Collect.getInstance().getString(
									R.string.gme_project_id_error));
			return false;
		}

		// check to see if a table already exists in GME that
		// matches the given md5
		try {
			gmeTableId = getGmeTableID(gmeFormValues.get(PROJECT_ID), jrFormId,
					token, md5);
		} catch (IOException e2) {
			e2.printStackTrace();
			mResults.put(id, form_fail + e2.getMessage());
			return false;
		}

		// GME limit is 1/s, so sleep for 1 second after each GME query
		try {
			Thread.sleep(GME_SLEEP_TIME);
		} catch (InterruptedException e3) {
			e3.printStackTrace();
		}

		// didn't exist, so try to create it
		boolean newTable = false;
		if (gmeTableId == null) {
			try {
				gmeTableId = createTable(jrFormId,
						gmeFormValues.get(PROJECT_ID), md5, token, formFilePath);
				newTable = true;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				mResults.put(id, form_fail + e.getMessage());
				return false;
			} catch (XmlPullParserException e) {
				e.printStackTrace();
				mResults.put(id, form_fail + e.getMessage());
				return false;
			} catch (IOException e) {
				e.printStackTrace();
				mResults.put(id, form_fail + e.getMessage());
				return false;
			} catch (FormException e) {
				e.printStackTrace();
				mResults.put(id, form_fail + e.getMessage());
				return false;
			}
		}

		// GME has 1q/s limit
		// but needs a few extra seconds after a create table
		try {
			int sleepTime = GME_SLEEP_TIME;
			if (newTable) {
				sleepTime += GME_CREATE_TABLE_DELAY;
			}
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		// at this point, we should have a valid gme table id to
		// submit this instance to
		if (gmeTableId == null) {
			mResults.put(
					id,
					form_fail
							+ Collect.getInstance().getString(
									R.string.gme_table_error));
			return false;
		}

		// if we have any photos to upload,
		// get the picasa album or create a new one
		// then upload the photos
		if (photosToUpload.size() > 0) {
			// First set up a picasa album to upload to:
			// maybe we should move this, because if we don't have any
			// photos we don't care...
			AlbumEntry albumToUse = null;
			try {
				albumToUse = getOrCreatePicasaAlbum(client, jrFormId);
			} catch (IOException e) {
				e.printStackTrace();
				GoogleAuthUtil.invalidateToken(Collect.getInstance(), token);
				mResults.put(id, picasa_fail + e.getMessage());
				return false;
			}

			try {
				uploadPhotosToPicasa(photosToUpload, uploadedPhotos, client,
						albumToUse, instanceFile);
			} catch (IOException e1) {
				e1.printStackTrace();
				mResults.put(id, picasa_fail + e1.getMessage());
				return false;
			}
		}

		// All photos have been sent to picasa (if there were any)
		// now upload data to GME

		String jsonSubmission = null;
		try {
			jsonSubmission = buildJSONSubmission(answersToUpload,
					uploadedPhotos);
		} catch (GeoPointNotFoundException e2) {
			e2.printStackTrace();
			mResults.put(id, form_fail + e2.getMessage());
			return false;
		}

		URL gmeuri = null;
		try {
			gmeuri = new URL("https://www.googleapis.com/mapsengine/v1/tables/"
					+ gmeTableId + "/features/batchInsert");
		} catch (MalformedURLException e) {
			mResults.put(id, gme_fail + e.getMessage());
			return false;
		}

		// try to upload the submission
		// if not successful, in case of error results will already be
		// populated
		return uploadSubmission(gmeuri, token, jsonSubmission, gmeTableId, id,
				newTable);
	}

	private boolean uploadSubmission(URL gmeuri, String token,
			String jsonSubmission, String gmeTableId, String id,
			boolean newTable) {
		HttpURLConnection conn = null;
		int status = -1;
		try {
			conn = (HttpURLConnection) gmeuri.openConnection();
			conn.setReadTimeout(10000 /* milliseconds */);
			conn.setConnectTimeout(15000 /* milliseconds */);
			conn.setRequestMethod("POST");
			conn.setDoInput(true);
			conn.setDoOutput(true);
			conn.setFixedLengthStreamingMode(jsonSubmission.getBytes().length);

			// make some HTTP header nicety
			conn.setRequestProperty("Content-Type",
					"application/json;charset=utf-8");
			conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
			conn.addRequestProperty("Authorization", "OAuth " + token);

			// setup send
			OutputStream os = new BufferedOutputStream(conn.getOutputStream());
			os.write(jsonSubmission.getBytes());
			// clean up
			os.flush();

			status = conn.getResponseCode();
			if (status / 100 == 2) {
				mResults.put(id,
						Collect.getInstance().getString(R.string.success));
				// GME has 1q/s limit
				try {
					Thread.sleep(GME_SLEEP_TIME);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			} else {
				String errorString = getErrorMesssage(conn.getErrorStream());
				if (status == 400) {
					// BAD REQUEST
					if (newTable) {
						mResults.put(
								id,
								gme_fail
										+ Collect.getInstance().getString(
												R.string.gme_create_400));
					} else {
						mResults.put(id, gme_fail + errorString);
					}
				} else if (status == 403 || status == 401) {
					// 403 == forbidden
					// 401 == invalid token (should work on next try)
					GoogleAuthUtil
							.invalidateToken(Collect.getInstance(), token);
					mResults.put(id, gme_fail + errorString);
				}
				return false;
			}
		} catch (Exception e) {
			e.printStackTrace();
			String errorString = getErrorMesssage(conn.getErrorStream());
			mResults.put(id, gme_fail + errorString);
			return false;
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
	}

	private String buildJSONSubmission(HashMap<String, String> answersToUpload,
			HashMap<String, PhotoEntry> uploadedPhotos)
			throws GeoPointNotFoundException {
		final GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(Feature.class, new FeatureSerializer());
		gsonBuilder.registerTypeAdapter(ArrayList.class,
				new FeatureListSerializer());
		final Gson gson = gsonBuilder.create();

		Map<String, String> properties = new HashMap<String, String>();
		properties.put("gx_id", String.valueOf(System.currentTimeMillis()));

		// there has to be a geo point, else we can't upload
		boolean foundGeo = false;
		PointGeometry pg = null;

		Iterator<String> answerIterator = answersToUpload.keySet().iterator();
		while (answerIterator.hasNext()) {
			String path = answerIterator.next();
			String answer = answersToUpload.get(path);

			// the instances don't have data types, so we try to match a
			// fairly specific pattern to determine geo coordinates, so we
			// pattern match against our answer
			// [-]#.# [-]#.# #.# #.#
			if (!foundGeo) {
				Pattern p = Pattern
						.compile("^-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s-?[0-9]+\\.[0-9]+\\s[0-9]+\\.[0-9]+$");
				Matcher m = p.matcher(answer);
				if (m.matches()) {
					foundGeo = true;
					// split on spaces, take the first two, which are lat
					// long
					String[] tokens = answer.split(" ");
					pg = new PointGeometry();
					pg.type = "Point";
					pg.setCoordinates(Double.parseDouble(tokens[1]),
							Double.parseDouble(tokens[0]));
				}
			}

			// geo or not, add to properties
			properties.put(path, answer);
		}

		if (!foundGeo) {
			throw new GeoPointNotFoundException(
					"Instance has no Coordinates! Unable to upload");
		}

		// then add the urls for photos
		Iterator<String> photoIterator = uploadedPhotos.keySet().iterator();
		while (photoIterator.hasNext()) {
			String path = photoIterator.next();
			String url = uploadedPhotos.get(path).getImageLink();
			properties.put(path, url);
		}

		Feature f = new Feature();
		f.geometry = pg;
		f.properties = properties;

		// gme expects an array of features for uploads, even though we only
		// send one
		ArrayList<Feature> features = new ArrayList<Feature>();
		features.add(f);

		return gson.toJson(features);
	}

	private void uploadPhotosToPicasa(HashMap<String, String> photos,
			HashMap<String, PhotoEntry> uploaded, PicasaClient client,
			AlbumEntry albumToUse, File instanceFile) throws IOException {
		Iterator<String> itr = photos.keySet().iterator();
		while (itr.hasNext()) {
			String key = itr.next();
			String filename = instanceFile.getParentFile() + "/"
					+ photos.get(key);
			File toUpload = new File(filename);

			// first check the local content provider
			// to see if this photo already has a picasa_id
			String selection = Images.Media.DATA + "=?";
			String[] selectionArgs = { filename };
			Cursor c = Collect
					.getInstance()
					.getContentResolver()
					.query(Images.Media.EXTERNAL_CONTENT_URI, null, selection,
							selectionArgs, null);
			if (c.getCount() != 1) {
				c.close();
				throw new FileNotFoundException(picasa_fail
						+ Collect.getInstance().getString(
								R.string.picasa_upload_error, filename));
			}

			// assume it's not already in picasa
			boolean inPicasa = false;

			// this will contain the link to the photo
			PhotoEntry picasaPhoto = null;

			c.moveToFirst();
			String picasa_id = c.getString(c
					.getColumnIndex(Images.Media.PICASA_ID));
			if (picasa_id == null || picasa_id.equalsIgnoreCase("")) {
				// not in picasa, so continue
			} else {
				// got a picasa ID, make sure it exists in this
				// particular album online
				// if it does, go on
				// if it doesn't, upload it and update the picasa_id
				if (albumToUse.numPhotos != 0) {
					PicasaUrl photosUrl = new PicasaUrl(
							albumToUse.getFeedLink());
					AlbumFeed albumFeed = client.executeGetAlbumFeed(photosUrl);

					for (PhotoEntry photo : albumFeed.photos) {
						if (picasa_id.equals(photo.id)) {
							// already in picasa, no need to upload
							inPicasa = true;
							picasaPhoto = photo;
						}
					}
				}
			}

			// wasn't already there, so upload a new copy and update the
			// content provder with its picasa_id
			if (!inPicasa) {
				String fileName = toUpload.getAbsolutePath();
				File file = new File(fileName);
				String mimetype = URLConnection.guessContentTypeFromName(file
						.getName());
				InputStreamContent content = new InputStreamContent(mimetype,
						new FileInputStream(file));

				picasaPhoto = client.executeInsertPhotoEntry(new PicasaUrl(
						albumToUse.getFeedLink()), content, toUpload.getName());

				ContentValues cv = new ContentValues();
				cv.put(Images.Media.PICASA_ID, picasaPhoto.id);

				// update the content provider picasa_id once we upload
				String where = Images.Media.DATA + "=?";
				String[] whereArgs = { toUpload.getAbsolutePath() };
				Collect.getInstance()
						.getContentResolver()
						.update(Images.Media.EXTERNAL_CONTENT_URI, cv, where,
								whereArgs);
			}

			// uploadedPhotos keeps track of the uploaded URL
			// relative to the path
			uploaded.put(key, picasaPhoto);
		}
	}

	private AlbumEntry getOrCreatePicasaAlbum(PicasaClient client,
			String jrFormId) throws IOException {
		AlbumEntry albumToUse = null;
		PicasaUrl url = PicasaUrl.relativeToRoot("feed/api/user/default");
		UserFeed feed = null;

		feed = client.executeGetUserFeed(url);

		// Find an album with a title matching the form_id
		// Technically there could be multiple albums that match
		// We just use the first one that matches
		if (feed.albums != null) {
			for (AlbumEntry album : feed.albums) {
				if (jrFormId.equals(album.title)) {
					albumToUse = album;
					break;
				}
			}
		}

		// no album exited, so create one
		if (albumToUse == null) {
			AlbumEntry newAlbum = new AlbumEntry();
			newAlbum.access = "private";
			newAlbum.title = jrFormId;
			newAlbum.summary = "Images for form: " + jrFormId;
			albumToUse = client.executeInsert(feed, newAlbum);
		}
		return albumToUse;
	}

	private String getGmeTableID(String projectid, String jrformid,
			String token, String md5) throws IOException {

		String gmetableid = null;
		// first check to see if form exists.

		// if a project ID has been defined
		String url = "https://www.googleapis.com/mapsengine/v1/tables";
		if (projectid != null) {
			url = url + "?projectId=" + projectid;
		}

		HttpURLConnection conn = null;
		TablesListResponse tables = null;
		boolean found = false;

		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		// keep fetching while nextToken exists
		// and we haven't found a matching table
		findtableloop: while (tables == null || tables.nextPageToken != null) {
			String openUrl = url + "&where=Name=" + jrformid;

			if (tables != null && tables.nextPageToken != null) {
				openUrl = url + "&pageToken=" + tables.nextPageToken;
			} else {
				openUrl = url;
			}
			Log.i(tag, "trying to open url: " + openUrl);
			URL fullUrl = new URL(openUrl);

			conn = (HttpURLConnection) fullUrl.openConnection();
			conn.setRequestMethod("GET");
			conn.addRequestProperty("Authorization", "OAuth " + token);
			conn.connect();

			if (conn.getResponseCode() != 200) {
				String errorString = getErrorMesssage(conn.getErrorStream());
				throw new IOException(errorString);
			}

			BufferedReader br = null;
			br = new BufferedReader(
					new InputStreamReader(conn.getInputStream()));

			if (tables != null) {
				tables.nextPageToken = null;
				tables = null;
			}
			tables = gson.fromJson(br, TablesListResponse.class);

			for (int i = 0; i < tables.tables.length; i++) {
				Table t = tables.tables[i];
				for (int j = 0; j < t.tags.length; j++) {
					if (md5.equalsIgnoreCase(t.tags[j])) {
						found = true;
						gmetableid = t.id;
						break findtableloop;
					}
				}
			}

			br.close();

			// GME has 1q/s limit
			try {
				Thread.sleep(GME_SLEEP_TIME);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (!found) {
			return null;
		} else {
			return gmetableid;
		}

	}

	private String createTable(String jrformid, String projectid, String md5,
			String token, String formFilePath) throws FileNotFoundException,
			XmlPullParserException, IOException, FormException {
		ArrayList<String> columnNames = new ArrayList<String>();
		getColumns(formFilePath, columnNames);

		String gmetableid = null;
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();

		Table t = new Table();
		t.name = jrformid;
		Log.i(tag, "Using GME projectid : " + projectid);
		t.projectId = projectid;

		Column first = new Column();
		first.name = "geometry";
		first.type = "points";

		Column[] columns = new Column[columnNames.size() + 1];
		columns[0] = first;

		for (int i = 0; i < columnNames.size(); i++) {
			Column c = new Column();
			c.name = columnNames.get(i);
			c.type = "string";
			columns[i + 1] = c;
		}

		Schema s = new Schema();
		s.columns = columns;
		t.schema = s;
		String[] tags = { md5 };
		t.tags = tags;
		t.description = "auto-created by ODK Collect for formid " + jrformid;

		URL createTableUrl = new URL(
				"https://www.googleapis.com/mapsengine/v1/tables");
		HttpURLConnection sendConn = null;
		int status = -1;

		final String json = gson.toJson(t);

		sendConn = (HttpURLConnection) createTableUrl.openConnection();
		sendConn.setReadTimeout(10000 /* milliseconds */);
		sendConn.setConnectTimeout(15000 /* milliseconds */);
		sendConn.setRequestMethod("POST");
		sendConn.setDoInput(true);
		sendConn.setDoOutput(true);
		sendConn.setFixedLengthStreamingMode(json.getBytes().length);

		// make some HTTP header nicety
		sendConn.setRequestProperty("Content-Type",
				"application/json;charset=utf-8");
		sendConn.setRequestProperty("X-Requested-With", "XMLHttpRequest");
		sendConn.addRequestProperty("Authorization", "OAuth " + token);

		// setup send
		OutputStream os = new BufferedOutputStream(sendConn.getOutputStream());
		os.write(json.getBytes());
		// clean up
		os.flush();

		status = sendConn.getResponseCode();
		if (status != 200) {
			String errorString = getErrorMesssage(sendConn.getErrorStream());
			throw new IOException(errorString);
		} else {
			BufferedReader br = new BufferedReader(new InputStreamReader(
					sendConn.getInputStream()));

			Table table = gson.fromJson(br, Table.class);
			Log.i(tag, "found table id :: " + table.id);
			gmetableid = table.id;
		}

		return gmetableid;
	}

	private void getColumns(String filePath, ArrayList<String> columns)
			throws FileNotFoundException, XmlPullParserException, IOException,
			FormException {
		File formFile = new File(filePath);
		FileInputStream in = null;

		in = new FileInputStream(formFile);
		XmlPullParser parser = Xml.newPullParser();

		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(in, null);
		readForm(parser, columns);
		in.close();
	}

	private void readForm(XmlPullParser parser, ArrayList<String> columns)
			throws XmlPullParserException, IOException, FormException {
		ArrayList<String> path = new ArrayList<String>();

		// we put path names in here as we go, and if we hit a duplicate we
		// blow up
		ArrayList<String> repeatCheck = new ArrayList<String>();
		boolean getPaths = false;
		int event = parser.next();
		int depth = 0;
		int lastpush = 0;
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				if (getPaths) {
					path.add(parser.getName());
					depth++;
					lastpush = depth;
					if (repeatCheck.contains(getPath(path))) {
						throw new FormException(Collect.getInstance()
								.getString(R.string.gme_repeat_error));
					} else {
						repeatCheck.add(getPath(path));
					}
				}

				if (parser.getName().equals("instance")) {
					getPaths = true;
				}
				break;
			case XmlPullParser.TEXT:
				// skip it
				break;
			case XmlPullParser.END_TAG:
				if (parser.getName().equals("instance")) {
					return;
				}
				if (getPaths) {
					if (depth == lastpush) {
						columns.add(getPath(path));
					} else {
						lastpush--;
					}
					path.remove(path.size() - 1);
					depth--;
				}

				break;
			default:
				Log.i(tag,
						"DEFAULTING: " + parser.getName() + " :: "
								+ parser.getEventType());
				break;
			}
			event = parser.next();
		}
	}

	private void processXMLFile(File instanceFile,
			HashMap<String, String> answersToUpload,
			HashMap<String, String> photosToUpload,
			HashMap<String, String> gmeFormValues)
			throws FileNotFoundException, XmlPullParserException, IOException,
			FormException {
		FileInputStream in = null;

		in = new FileInputStream(instanceFile);
		XmlPullParser parser = Xml.newPullParser();

		parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false);
		parser.setInput(in, null);
		readFeed(parser, answersToUpload, photosToUpload, gmeFormValues);
		in.close();
	}

	private void readFeed(XmlPullParser parser,
			HashMap<String, String> answersToUpload,
			HashMap<String, String> photosToUpload,
			HashMap<String, String> gmeFormValues)
			throws XmlPullParserException, IOException, FormException {
		ArrayList<String> path = new ArrayList<String>();

		// we put path names in here as we go, and if we hit a duplicate we
		// blow up
		ArrayList<String> repeatCheck = new ArrayList<String>();
		int event = parser.next();
		while (event != XmlPullParser.END_DOCUMENT) {
			switch (event) {
			case XmlPullParser.START_TAG:
				path.add(parser.getName());
				if (repeatCheck.contains(getPath(path))) {
					throw new FormException(Collect.getInstance().getString(
							R.string.gme_repeat_error));
				} else {
					repeatCheck.add(getPath(path));
				}
				// check the start tag for project ID
				for (int i = 0; i < parser.getAttributeCount(); i++) {
					String attr = parser.getAttributeName(i);
					if ("projectId".equals(attr)) {
						gmeFormValues.put("projectId",
								parser.getAttributeValue(i));
						break;
					}
				}

				break;
			case XmlPullParser.TEXT:
				String answer = parser.getText();
				if (answer.endsWith(".jpg") || answer.endsWith(".png")) {
					photosToUpload.put(getPath(path), answer);
				} else {
					answersToUpload.put(getPath(path), answer);
				}
				break;
			case XmlPullParser.END_TAG:
				path.remove(path.size() - 1);
				break;
			default:
				Log.i(tag,
						"DEFAULTING: " + parser.getName() + " :: "
								+ parser.getEventType());
				break;
			}
			event = parser.next();
		}
	}

	private String getPath(ArrayList<String> path) {
		String currentPath = "";
		for (String node : path) {
			currentPath += "/" + node;
		}
		return currentPath;
	}

    @Override
    protected void onPostExecute(HashMap<String, String> results) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.uploadingComplete(results);

                StringBuilder selection = new StringBuilder();
                Set<String> keys = results.keySet();
                Iterator<String> it = keys.iterator();

                String[] selectionArgs = new String[keys.size() + 1];
                int i = 0;
                selection.append("(");
                while (it.hasNext()) {
                    String id = it.next();
                    selection.append(InstanceColumns._ID + "=?");
                    selectionArgs[i++] = id;
                    if (i != keys.size()) {
                        selection.append(" or ");
                    }
                }
                
                selection.append(") and status=?");
                selectionArgs[i] = InstanceProviderAPI.STATUS_SUBMITTED;

                Cursor uploadResults = null;
                try {
                    uploadResults = Collect
                            .getInstance()
                            .getContentResolver()
                            .query(InstanceColumns.CONTENT_URI, null, selection.toString(),
                                    selectionArgs, null);
                    if (uploadResults.getCount() > 0) {
                        Long[] toDelete = new Long[uploadResults.getCount()];
                        uploadResults.moveToPosition(-1);

                        int cnt = 0;
                        while (uploadResults.moveToNext()) {
                            toDelete[cnt] = uploadResults.getLong(uploadResults
                                    .getColumnIndex(InstanceColumns._ID));
                            cnt++;
                        }

                        boolean deleteFlag = PreferenceManager.getDefaultSharedPreferences(
                                Collect.getInstance().getApplicationContext()).getBoolean(
                                PreferencesActivity.KEY_DELETE_AFTER_SEND, false);
                        if (deleteFlag) {
                            DeleteInstancesTask dit = new DeleteInstancesTask();
                            dit.setContentResolver(Collect.getInstance().getContentResolver());
                            dit.execute(toDelete);
                        }

                    }
                } finally {
                    if (uploadResults != null) {
                        uploadResults.close();
                    }
                }
            }
        }
    }

	@Override
	protected void onProgressUpdate(Integer... values) {
		synchronized (this) {
			if (mStateListener != null) {
				// update progress and total
				mStateListener.progressUpdate(values[0].intValue(),
						values[1].intValue());
			}
		}
	}

}
