/*
 * Copyright (C) 2009 University of Washington
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

package org.odk.collect.android.utilities;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.odk.collect.android.application.Collect;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Video;
import android.util.Log;

/**
 * Consolidate all interactions with media providers here.
 *
 * The functionality of getPath() was provided by paulburke as described here:
 * See
 * http://stackoverflow.com/questions/20067508/get-real-path-from-uri-android
 * -kitkat-new-storage-access-framework for details
 *
 * @author mitchellsundt@gmail.com
 * @author paulburke
 *
 *
 */
public class MediaUtils {
	private static final String t = "MediaUtils";

	private MediaUtils() {
		// static methods only
	}

	private static String escapePath(String path) {
		String ep = path;
		ep = ep.replaceAll("\\!", "!!");
		ep = ep.replaceAll("_", "!_");
		ep = ep.replaceAll("%", "!%");
		return ep;
	}

	public static final Uri getImageUriFromMediaProvider(String imageFile) {
		String selection = Images.ImageColumns.DATA + "=?";
		String[] selectArgs = { imageFile };
		String[] projection = { Images.ImageColumns._ID };
		Cursor c = null;
		try {
			c = Collect
					.getInstance()
					.getContentResolver()
					.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection, selection, selectArgs, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				String id = c.getString(c
						.getColumnIndex(Images.ImageColumns._ID));

				return Uri
						.withAppendedPath(
								android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								id);
			}
			return null;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static final int deleteImageFileFromMediaProvider(String imageFile) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// images
		int count = 0;
		Cursor imageCursor = null;
		try {
			String select = Images.Media.DATA + "=?";
			String[] selectArgs = { imageFile };

			String[] projection = { Images.ImageColumns._ID };
			imageCursor = cr
					.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (imageCursor.getCount() > 0) {
				imageCursor.moveToFirst();
				List<Uri> imagesToDelete = new ArrayList<Uri>();
				do {
					String id = imageCursor.getString(imageCursor
							.getColumnIndex(Images.ImageColumns._ID));

					imagesToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (imageCursor.moveToNext());

				for (Uri uri : imagesToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (imageCursor != null) {
				imageCursor.close();
			}
		}
		File f = new File(imageFile);
		if (f.exists()) {
			f.delete();
		}
		return count;
	}

	public static final int deleteImagesInFolderFromMediaProvider(File folder) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// images
		int count = 0;
		Cursor imageCursor = null;
		try {
			String select = Images.Media.DATA + " like ? escape '!'";
			String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

			String[] projection = { Images.ImageColumns._ID };
			imageCursor = cr
					.query(android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (imageCursor.getCount() > 0) {
				imageCursor.moveToFirst();
				List<Uri> imagesToDelete = new ArrayList<Uri>();
				do {
					String id = imageCursor.getString(imageCursor
							.getColumnIndex(Images.ImageColumns._ID));

					imagesToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (imageCursor.moveToNext());

				for (Uri uri : imagesToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (imageCursor != null) {
				imageCursor.close();
			}
		}
		return count;
	}

	public static final Uri getAudioUriFromMediaProvider(String audioFile) {
		String selection = Audio.AudioColumns.DATA + "=?";
		String[] selectArgs = { audioFile };
		String[] projection = { Audio.AudioColumns._ID };
		Cursor c = null;
		try {
			c = Collect
					.getInstance()
					.getContentResolver()
					.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							projection, selection, selectArgs, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				String id = c.getString(c
						.getColumnIndex(Audio.AudioColumns._ID));

				return Uri
						.withAppendedPath(
								android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
								id);
			}
			return null;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static final int deleteAudioFileFromMediaProvider(String audioFile) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// audio
		int count = 0;
		Cursor audioCursor = null;
		try {
			String select = Audio.Media.DATA + "=?";
			String[] selectArgs = { audioFile };

			String[] projection = { Audio.AudioColumns._ID };
			audioCursor = cr
					.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (audioCursor.getCount() > 0) {
				audioCursor.moveToFirst();
				List<Uri> audioToDelete = new ArrayList<Uri>();
				do {
					String id = audioCursor.getString(audioCursor
							.getColumnIndex(Audio.AudioColumns._ID));

					audioToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (audioCursor.moveToNext());

				for (Uri uri : audioToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (audioCursor != null) {
				audioCursor.close();
			}
		}
		File f = new File(audioFile);
		if (f.exists()) {
			f.delete();
		}
		return count;
	}

	public static final int deleteAudioInFolderFromMediaProvider(File folder) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// audio
		int count = 0;
		Cursor audioCursor = null;
		try {
			String select = Audio.Media.DATA + " like ? escape '!'";
			String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

			String[] projection = { Audio.AudioColumns._ID };
			audioCursor = cr
					.query(android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (audioCursor.getCount() > 0) {
				audioCursor.moveToFirst();
				List<Uri> audioToDelete = new ArrayList<Uri>();
				do {
					String id = audioCursor.getString(audioCursor
							.getColumnIndex(Audio.AudioColumns._ID));

					audioToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (audioCursor.moveToNext());

				for (Uri uri : audioToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (audioCursor != null) {
				audioCursor.close();
			}
		}
		return count;
	}

	public static final Uri getVideoUriFromMediaProvider(String videoFile) {
		String selection = Video.VideoColumns.DATA + "=?";
		String[] selectArgs = { videoFile };
		String[] projection = { Video.VideoColumns._ID };
		Cursor c = null;
		try {
			c = Collect
					.getInstance()
					.getContentResolver()
					.query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection, selection, selectArgs, null);
			if (c.getCount() > 0) {
				c.moveToFirst();
				String id = c.getString(c
						.getColumnIndex(Video.VideoColumns._ID));

				return Uri
						.withAppendedPath(
								android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
								id);
			}
			return null;
		} finally {
			if (c != null) {
				c.close();
			}
		}
	}

	public static final int deleteVideoFileFromMediaProvider(String videoFile) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// video
		int count = 0;
		Cursor videoCursor = null;
		try {
			String select = Video.Media.DATA + "=?";
			String[] selectArgs = { videoFile };

			String[] projection = { Video.VideoColumns._ID };
			videoCursor = cr
					.query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (videoCursor.getCount() > 0) {
				videoCursor.moveToFirst();
				List<Uri> videoToDelete = new ArrayList<Uri>();
				do {
					String id = videoCursor.getString(videoCursor
							.getColumnIndex(Video.VideoColumns._ID));

					videoToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (videoCursor.moveToNext());

				for (Uri uri : videoToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (videoCursor != null) {
				videoCursor.close();
			}
		}
		File f = new File(videoFile);
		if (f.exists()) {
			f.delete();
		}
		return count;
	}

	public static final int deleteVideoInFolderFromMediaProvider(File folder) {
		ContentResolver cr = Collect.getInstance().getContentResolver();
		// video
		int count = 0;
		Cursor videoCursor = null;
		try {
			String select = Video.Media.DATA + " like ? escape '!'";
			String[] selectArgs = { escapePath(folder.getAbsolutePath()) };

			String[] projection = { Video.VideoColumns._ID };
			videoCursor = cr
					.query(android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
							projection, select, selectArgs, null);
			if (videoCursor.getCount() > 0) {
				videoCursor.moveToFirst();
				List<Uri> videoToDelete = new ArrayList<Uri>();
				do {
					String id = videoCursor.getString(videoCursor
							.getColumnIndex(Video.VideoColumns._ID));

					videoToDelete
							.add(Uri.withAppendedPath(
									android.provider.MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
									id));
				} while (videoCursor.moveToNext());

				for (Uri uri : videoToDelete) {
					Log.i(t, "attempting to delete: " + uri);
					count += cr.delete(uri, null, null);
				}
			}
		} catch (Exception e) {
			Log.e(t, e.toString());
		} finally {
			if (videoCursor != null) {
				videoCursor.close();
			}
		}
		return count;
	}

	/**
	 * Consolidates the file path determination functionality of the various
	 * media prompts. Beginning with KitKat, the responses use a different
	 * mechanism and needs a lot of special handling.
	 *
	 * @param ctxt
	 * @param uri
	 * @param pathKey
	 * @return
	 */
	@SuppressLint("NewApi")
	public static String getPathFromUri(Context ctxt, Uri uri, String pathKey) {

		if (Build.VERSION.SDK_INT >= 19) {
			return getPath(ctxt, uri);
		} else {
			if (uri.toString().startsWith("file")) {
				return uri.toString().substring(7);
			} else {
				String[] projection = { pathKey };
				Cursor c = null;
				try {
					c = ctxt.getContentResolver().query(uri, projection, null,
							null, null);
					int column_index = c.getColumnIndexOrThrow(pathKey);
					String path = null;
					if (c.getCount() > 0) {
						c.moveToFirst();
						path = c.getString(column_index);
					}
					return path;
				} finally {
					if (c != null) {
						c.close();
					}
				}
			}
		}
	}

	@SuppressLint("NewApi")
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.<br>
	 * <br>
	 * Callers should check whether the path is local before assuming it
	 * represents a local file.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @see #isLocal(String)
	 * @see #getFile(Context, Uri)
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

		final boolean isKitKat = Build.VERSION.SDK_INT >= 19;

		// DocumentProvider
		if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {

			// ExternalStorageProvider
			if (isExternalStorageDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				if ("primary".equalsIgnoreCase(type)) {
					return Environment.getExternalStorageDirectory() + "/"
							+ split[1];
				}

				// TODO handle non-primary volumes
			}
			// DownloadsProvider
			else if (isDownloadsDocument(uri)) {

				final String id = DocumentsContract.getDocumentId(uri);
				final Uri contentUri = ContentUris.withAppendedId(
						Uri.parse("content://downloads/public_downloads"),
						Long.valueOf(id));

				return getDataColumn(context, contentUri, null, null);
			}
			// MediaProvider
			else if (isMediaDocument(uri)) {
				final String docId = DocumentsContract.getDocumentId(uri);
				final String[] split = docId.split(":");
				final String type = split[0];

				Uri contentUri = null;
				if ("image".equals(type)) {
					contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
				} else if ("video".equals(type)) {
					contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
				} else if ("audio".equals(type)) {
					contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
				}

				final String selection = "_id=?";
				final String[] selectionArgs = new String[] { split[1] };

				return getDataColumn(context, contentUri, selection,
						selectionArgs);
			}
		}
		// MediaStore (and general)
		else if ("content".equalsIgnoreCase(uri.getScheme())) {

			// Return the remote address
			if (isGooglePhotosUri(uri))
				return uri.getLastPathSegment();

			return getDataColumn(context, uri, null, null);
		}
		// File
		else if ("file".equalsIgnoreCase(uri.getScheme())) {
			return uri.getPath();
		}

		return null;
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 * @author paulburke
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
		return "com.android.externalstorage.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 * @author paulburke
	 */
	public static boolean isDownloadsDocument(Uri uri) {
		return "com.android.providers.downloads.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 * @author paulburke
	 */
	public static boolean isMediaDocument(Uri uri) {
		return "com.android.providers.media.documents".equals(uri
				.getAuthority());
	}

	/**
	 * @param uri
	 *            The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
		return "com.google.android.apps.photos.content".equals(uri
				.getAuthority());
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context
	 *            The context.
	 * @param uri
	 *            The Uri to query.
	 * @param selection
	 *            (Optional) Filter used in the query.
	 * @param selectionArgs
	 *            (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 * @author paulburke
	 */
	public static String getDataColumn(Context context, Uri uri,
			String selection, String[] selectionArgs) {

		Cursor cursor = null;
		final String column = "_data";
		final String[] projection = { column };

		try {
			cursor = context.getContentResolver().query(uri, projection,
					selection, selectionArgs, null);
			if (cursor != null && cursor.moveToFirst()) {

				final int column_index = cursor.getColumnIndexOrThrow(column);
				return cursor.getString(column_index);
			}
		} finally {
			if (cursor != null)
				cursor.close();
		}
		return null;
	}
}
