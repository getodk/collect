/*
 * Copyright (C) 2017 University of Washington
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

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;

import org.apache.commons.io.IOUtils;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import timber.log.Timber;

/**
 * Static methods used for common file operations.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class FileUtils {

    // Used to validate and display valid form names.
    public static final String VALID_FILENAME = "[ _\\-A-Za-z0-9]*.x[ht]*ml";
    public static final String FORMID = "formid";
    public static final String VERSION = "version"; // arbitrary string in OpenRosa 1.0
    public static final String TITLE = "title";
    public static final String SUBMISSIONURI = "submission";
    public static final String BASE64_RSA_PUBLIC_KEY = "base64RsaPublicKey";
    public static final String AUTO_DELETE = "autoDelete";
    public static final String AUTO_SEND = "autoSend";
    static int bufSize = 16 * 1024; // May be set by unit test

    private FileUtils() {
    }

    public static String getMimeType(String fileUrl) throws IOException {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(fileUrl);
    }

    public static boolean createFolder(String path) {
        File dir = new File(path);
        return dir.exists() || dir.mkdirs();
    }

    public static byte[] getFileAsBytes(File file) {
        try (InputStream is = new FileInputStream(file)) {

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Timber.e("File %s is too large", file.getName());
                return null;
            }

            // Create the byte array to hold the data
            byte[] bytes = new byte[(int) length];

            // Read in the bytes
            int offset = 0;
            int read = 0;
            try {
                while (offset < bytes.length && read >= 0) {
                    read = is.read(bytes, offset, bytes.length - offset);
                    offset += read;
                }
            } catch (IOException e) {
                Timber.e(e, "Cannot read file %s", file.getName());
                return null;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                try {
                    throw new IOException("Could not completely read file " + file.getName());
                } catch (IOException e) {
                    Timber.e(e);
                    return null;
                }
            }

            return bytes;
        } catch (IOException e) {
            Timber.e(e);
        }
        return new byte[0];
    }

    public static String getMd5Hash(File file) {
        final InputStream is;
        try {
            is = new FileInputStream(file);

        } catch (FileNotFoundException e) {
            Timber.d(e, "Cache file %s not found", file.getAbsolutePath());
            return null;

        }

        return getMd5Hash(is);
    }

    public static String getMd5Hash(InputStream is) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            final byte[] buffer = new byte[bufSize];

            while (true) {
                int result = is.read(buffer, 0, bufSize);
                if (result == -1) {
                    break;
                }
                md.update(buffer, 0, result);
            }

            StringBuilder md5 = new StringBuilder(new BigInteger(1, md.digest()).toString(16));
            while (md5.length() < 32) {
                md5.insert(0, "0");
            }

            is.close();
            return md5.toString();

        } catch (NoSuchAlgorithmException e) {
            Timber.e(e);
            return null;

        } catch (IOException e) {
            Timber.e(e, "Problem reading file.");
            return null;
        }
    }

    public static Bitmap getBitmapScaledToDisplay(File file, int screenHeight, int screenWidth) {
        return getBitmapScaledToDisplay(file, screenHeight, screenWidth, false);
    }

    /**
     * Scales image according to the given display
     *
     * @param file           containing the image
     * @param screenHeight   height of the display
     * @param screenWidth    width of the display
     * @param upscaleEnabled determines whether the image should be up-scaled or not
     *                       if the window size is greater than the image size
     * @return scaled bitmap
     */
    public static Bitmap getBitmapScaledToDisplay(File file, int screenHeight, int screenWidth, boolean upscaleEnabled) {
        // Determine image size of file
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        getBitmap(file.getAbsolutePath(), options);

        Bitmap bitmap;
        double scale;
        if (upscaleEnabled) {
            // Load full size bitmap image
            options = new BitmapFactory.Options();
            options.inInputShareable = true;
            options.inPurgeable = true;
            bitmap = getBitmap(file.getAbsolutePath(), options);

            double heightScale = ((double) (options.outHeight)) / screenHeight;
            double widthScale = ((double) options.outWidth) / screenWidth;
            scale = Math.max(widthScale, heightScale);

            double newHeight = Math.ceil(options.outHeight / scale);
            double newWidth = Math.ceil(options.outWidth / scale);

            bitmap = Bitmap.createScaledBitmap(bitmap, (int) newWidth, (int) newHeight, false);
        } else {
            int heightScale = options.outHeight / screenHeight;
            int widthScale = options.outWidth / screenWidth;

            // Powers of 2 work faster, sometimes, according to the doc.
            // We're just doing closest size that still fills the screen.
            scale = Math.max(widthScale, heightScale);

            // get bitmap with scale ( < 1 is the same as 1)
            options = new BitmapFactory.Options();
            options.inInputShareable = true;
            options.inPurgeable = true;
            options.inSampleSize = (int) scale;
            bitmap = getBitmap(file.getAbsolutePath(), options);
        }

        if (bitmap != null) {
            Timber.i("Screen is %dx%d.  Image has been scaled down by %f to %dx%d",
                    screenHeight, screenWidth, scale, bitmap.getHeight(), bitmap.getWidth());
        }
        return bitmap;
    }

    public static String copyFile(File sourceFile, File destFile) {
        if (sourceFile.exists()) {
            String errorMessage = actualCopy(sourceFile, destFile);
            if (errorMessage != null) {
                try {
                    Thread.sleep(500);
                    Timber.e("Retrying to copy the file after 500ms: %s",
                            sourceFile.getAbsolutePath());
                    errorMessage = actualCopy(sourceFile, destFile);
                } catch (InterruptedException e) {
                    Timber.e(e);
                }
            }
            return errorMessage;
        } else {
            String msg = "Source file does not exist: " + sourceFile.getAbsolutePath();
            Timber.e(msg);
            return msg;
        }
    }

    private static String actualCopy(File sourceFile, File destFile) {
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        FileChannel src = null;
        FileChannel dst = null;
        try {
            fileInputStream = new FileInputStream(sourceFile);
            src = fileInputStream.getChannel();
            fileOutputStream = new FileOutputStream(destFile);
            dst = fileOutputStream.getChannel();
            dst.transferFrom(src, 0, src.size());
            dst.force(true);
            return null;
        } catch (Exception e) {
            if (e instanceof FileNotFoundException) {
                Timber.e(e, "FileNotFoundException while copying file");
            } else if (e instanceof IOException) {
                Timber.e(e, "IOException while copying file");
            } else {
                Timber.e(e, "Exception while copying file");
            }
            return e.getMessage();
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(fileOutputStream);
            IOUtils.closeQuietly(src);
            IOUtils.closeQuietly(dst);
        }
    }

    public static HashMap<String, String> parseXML(File xmlFile) {
        final HashMap<String, String> fields = new HashMap<String, String>();
        final InputStream is;
        try {
            is = new FileInputStream(xmlFile);
        } catch (FileNotFoundException e1) {
            Timber.d(e1);
            throw new IllegalStateException(e1);
        }

        InputStreamReader isr;
        try {
            isr = new InputStreamReader(is, "UTF-8");
        } catch (UnsupportedEncodingException uee) {
            Timber.w(uee, "Trying default encoding as UTF 8 encoding unavailable");
            isr = new InputStreamReader(is);
        }

        final Document doc;
        try {
            doc = XFormParser.getXMLDocument(isr);
        } catch (IOException e) {
            Timber.e(e, "Unable to parse XML document %s", xmlFile.getAbsolutePath());
            throw new IllegalStateException("Unable to parse XML document", e);
        } finally {
            try {
                isr.close();
            } catch (IOException e) {
                Timber.w("%s error closing from reader", xmlFile.getAbsolutePath());
            }
        }

        final String xforms = "http://www.w3.org/2002/xforms";
        final String html = doc.getRootElement().getNamespace();

        final Element head = doc.getRootElement().getElement(html, "head");
        final Element title = head.getElement(html, "title");
        if (title != null) {
            fields.put(TITLE, XFormParser.getXMLText(title, true));
        }

        final Element model = getChildElement(head, "model");
        Element cur = getChildElement(model, "instance");

        final int idx = cur.getChildCount();
        int i;
        for (i = 0; i < idx; ++i) {
            if (cur.isText(i)) {
                continue;
            }
            if (cur.getType(i) == Node.ELEMENT) {
                break;
            }
        }

        if (i < idx) {
            cur = cur.getElement(i); // this is the first data element
            final String id = cur.getAttributeValue(null, "id");

            final String version = cur.getAttributeValue(null, "version");
            final String uiVersion = cur.getAttributeValue(null, "uiVersion");
            if (uiVersion != null) {
                // pre-OpenRosa 1.0 variant of spec
                Timber.e("Obsolete use of uiVersion -- IGNORED -- only using version: %s",
                        version);
            }

            fields.put(FORMID, (id == null) ? cur.getNamespace() : id);
            fields.put(VERSION, (version == null) ? null : version);
        } else {
            throw new IllegalStateException(xmlFile.getAbsolutePath() + " could not be parsed");
        }
        try {
            final Element submission = model.getElement(xforms, "submission");
            final String base64RsaPublicKey = submission.getAttributeValue(null, "base64RsaPublicKey");
            final String autoDelete = submission.getAttributeValue(null, "auto-delete");
            final String autoSend = submission.getAttributeValue(null, "auto-send");

            fields.put(SUBMISSIONURI, submission.getAttributeValue(null, "action"));
            fields.put(BASE64_RSA_PUBLIC_KEY,
                    (base64RsaPublicKey == null || base64RsaPublicKey.trim().length() == 0)
                            ? null : base64RsaPublicKey.trim());
            fields.put(AUTO_DELETE, autoDelete);
            fields.put(AUTO_SEND, autoSend);
        } catch (Exception e) {
            Timber.i("XML file %s does not have a submission element", xmlFile.getAbsolutePath());
            // and that's totally fine.
        }

        return fields;
    }

    // needed because element.getelement fails when there are attributes
    private static Element getChildElement(Element parent, String childName) {
        Element e = null;
        int c = parent.getChildCount();
        int i = 0;
        for (i = 0; i < c; i++) {
            if (parent.getType(i) == Node.ELEMENT) {
                if (parent.getElement(i).getName().equalsIgnoreCase(childName)) {
                    return parent.getElement(i);
                }
            }
        }
        return e;
    }

    public static void deleteAndReport(File file) {
        if (file != null && file.exists()) {
            // remove garbage
            if (!file.delete()) {
                Timber.w("%s will be deleted upon exit.", file.getAbsolutePath());
                file.deleteOnExit();
            } else {
                Timber.w("%s has been deleted.", file.getAbsolutePath());
            }
        }
    }

    public static String constructMediaPath(String formFilePath) {
        String pathNoExtension = formFilePath.substring(0, formFilePath.lastIndexOf('.'));
        return pathNoExtension + "-media";
    }

    /**
     * @param mediaDir the media folder
     */
    public static void checkMediaPath(File mediaDir) {
        if (mediaDir.exists() && mediaDir.isFile()) {
            Timber.e("The media folder is already there and it is a FILE!! We will need to delete "
                    + "it and create a folder instead");
            boolean deleted = mediaDir.delete();
            if (!deleted) {
                throw new RuntimeException(
                        Collect.getInstance().getString(R.string.fs_delete_media_path_if_file_error,
                                mediaDir.getAbsolutePath()));
            }
        }

        // the directory case
        boolean createdOrExisted = createFolder(mediaDir.getAbsolutePath());
        if (!createdOrExisted) {
            throw new RuntimeException(
                    Collect.getInstance().getString(R.string.fs_create_media_folder_error,
                            mediaDir.getAbsolutePath()));
        }
    }

    public static void purgeMediaPath(String mediaPath) {
        File tempMediaFolder = new File(mediaPath);
        File[] tempMediaFiles = tempMediaFolder.listFiles();
        if (tempMediaFiles == null || tempMediaFiles.length == 0) {
            deleteAndReport(tempMediaFolder);
        } else {
            for (File tempMediaFile : tempMediaFiles) {
                deleteAndReport(tempMediaFile);
            }
        }
    }

    public static void moveMediaFiles(String tempMediaPath, File formMediaPath) throws IOException {
        File tempMediaFolder = new File(tempMediaPath);
        File[] mediaFiles = tempMediaFolder.listFiles();
        if (mediaFiles == null || mediaFiles.length == 0) {
            deleteAndReport(tempMediaFolder);
        } else {
            for (File mediaFile : mediaFiles) {
                org.apache.commons.io.FileUtils.moveFileToDirectory(mediaFile, formMediaPath, true);
            }
            deleteAndReport(tempMediaFolder);
        }
    }

    public static void saveBitmapToFile(Bitmap bitmap, String path) {
        final Bitmap.CompressFormat compressFormat = path.toLowerCase(Locale.getDefault()).endsWith(".png") ?
                Bitmap.CompressFormat.PNG : Bitmap.CompressFormat.JPEG;

        try (FileOutputStream out = new FileOutputStream(path)) {
            bitmap.compress(compressFormat, 100, out);
        } catch (Exception e) {
            Timber.e(e);
        }
    }

    /*
    This method is used to avoid OutOfMemoryError exception during loading an image.
    If the exception occurs we catch it and try to load a smaller image.
     */
    public static Bitmap getBitmap(String path, BitmapFactory.Options originalOptions) {
        BitmapFactory.Options newOptions = new BitmapFactory.Options();
        newOptions.inSampleSize = originalOptions.inSampleSize;
        if (newOptions.inSampleSize <= 0) {
            newOptions.inSampleSize = 1;
        }
        Bitmap bitmap;
        try {
            bitmap = BitmapFactory.decodeFile(path, originalOptions);
        } catch (OutOfMemoryError e) {
            Timber.i(e);
            newOptions.inSampleSize++;
            return getBitmap(path, newOptions);
        }

        return bitmap;
    }

    public static byte[] read(File file) {
        byte[] bytes = {};
        try {
            bytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(bytes);
            is.close();
        } catch (IOException e) {
            Timber.e(e);
        }
        return bytes;
    }

    public static void write(File file, byte[] data) {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Timber.e(e);
        }
    }

    /**
     * With the FileProvider you have to manually grant and revoke read/write permissions to files you
     * are sharing. With this approach the access only lasts as long as the target activity on Api versions
     * above Kit Kat. Once you are below that you have to manually revoke the permissions.
     *
     * @param intent that needs to have the permission flags
     * @param uri    that the permissions are being applied to
     * @return intent that has read and write permissions
     */
    public static void grantFilePermissions(Intent intent, Uri uri, Context context) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        /*
         Workaround for Android bug.
         grantUriPermission also needed for KITKAT,
         see https://code.google.com/p/android/issues/detail?id=76683
         */
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
        }
    }


    /**
     * With the FileProvider you have to manually grant and revoke read/write permissions to files you
     * are sharing. With this approach the access only lasts as long as the target activity on Api versions
     * above Kit Kat. Once you are below that you have to manually revoke the permissions.
     *
     * @param intent that needs to have the permission flags
     * @param uri    that the permissions are being applied to
     * @return intent that has read and write permissions
     */
    public static void grantFileReadPermissions(Intent intent, Uri uri, Context context) {
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        /*
         Workaround for Android bug.
         grantUriPermission also needed for KITKAT,
         see https://code.google.com/p/android/issues/detail?id=76683
         */
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            List<ResolveInfo> resInfoList = context.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
            for (ResolveInfo resolveInfo : resInfoList) {
                String packageName = resolveInfo.activityInfo.packageName;
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
        }
    }

    public static void revokeFileReadWritePermission(Context context, Uri uri) {
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.KITKAT) {
            context.revokeUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
        }
    }
}
