/*
 * Copyright (C) 2011 University of Washington
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

import android.net.Uri;
import android.util.Base64;

import androidx.annotation.Nullable;

import org.apache.commons.io.IOUtils;
import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.collect.android.R;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.exception.EncryptionException;
import org.odk.collect.android.javarosawrapper.FormController.InstanceMetadata;
import org.odk.collect.android.external.FormsContract;
import org.odk.collect.android.external.InstancesContract;
import org.odk.collect.forms.Form;
import org.odk.collect.forms.instances.Instance;
import org.odk.collect.shared.strings.Md5;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import timber.log.Timber;

import static org.odk.collect.android.utilities.ApplicationConstants.Namespaces.XML_OPENROSA_NAMESPACE;

/**
 * Utility class for encrypting submissions during the SaveFormToDisk.
 *
 * @author mitchellsundt@gmail.com
 */
public class EncryptionUtils {
    public static final String RSA_ALGORITHM = "RSA";
    // the symmetric key we are encrypting with RSA is only 256 bits... use SHA-256
    public static final String ASYMMETRIC_ALGORITHM = "RSA/NONE/OAEPWithSHA256AndMGF1Padding";
    public static final String SYMMETRIC_ALGORITHM = "AES/CFB/PKCS5Padding";
    public static final String UTF_8 = "UTF-8";
    public static final int SYMMETRIC_KEY_LENGTH = 256;
    public static final int IV_BYTE_LENGTH = 16;

    // tags in the submission manifest

    private static final String XML_ENCRYPTED_TAG_NAMESPACE =
            "http://www.opendatakit.org/xforms/encrypted";
    private static final String DATA = "data";
    private static final String ID = "id";
    private static final String VERSION = "version";
    private static final String ENCRYPTED = "encrypted";
    private static final String BASE64_ENCRYPTED_KEY = "base64EncryptedKey";
    private static final String ENCRYPTED_XML_FILE = "encryptedXmlFile";
    private static final String META = "meta";
    private static final String INSTANCE_ID = "instanceID";
    private static final String MEDIA = "media";
    private static final String FILE = "file";
    private static final String BASE64_ENCRYPTED_ELEMENT_SIGNATURE =
            "base64EncryptedElementSignature";
    private static final String NEW_LINE = "\n";
    private static final String ENCRYPTION_PROVIDER = "BC";

    private EncryptionUtils() {
    }

    public static final class EncryptedFormInformation {
        public final String formId;
        public final String formVersion;
        public final InstanceMetadata instanceMetadata;
        public final PublicKey rsaPublicKey;
        public final String base64RsaEncryptedSymmetricKey;
        public final SecretKeySpec symmetricKey;
        public final byte[] ivSeedArray;
        private int ivCounter;
        public final StringBuilder elementSignatureSource = new StringBuilder();
        private boolean isNotBouncyCastle;

        EncryptedFormInformation(String formId, String formVersion,
                                 InstanceMetadata instanceMetadata, PublicKey rsaPublicKey) {
            this.formId = formId;
            this.formVersion = formVersion;
            this.instanceMetadata = instanceMetadata;
            this.rsaPublicKey = rsaPublicKey;

            // generate the symmetric key from random bits...

            SecureRandom r = new SecureRandom();
            byte[] key = new byte[SYMMETRIC_KEY_LENGTH / 8];
            r.nextBytes(key);
            symmetricKey = new SecretKeySpec(key, SYMMETRIC_ALGORITHM);

            // construct the fixed portion of the iv -- the ivSeedArray
            // this is the md5 hash of the instanceID and the symmetric key
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(instanceMetadata.instanceId.getBytes(UTF_8));
                md.update(key);
                byte[] messageDigest = md.digest();
                ivSeedArray = new byte[IV_BYTE_LENGTH];
                for (int i = 0; i < IV_BYTE_LENGTH; ++i) {
                    ivSeedArray[i] = messageDigest[i % messageDigest.length];
                }
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                Timber.e(e, "Unable to set md5 hash for instanceid and symmetric key.");
                throw new IllegalArgumentException(e.getMessage());
            }

            // construct the base64-encoded RSA-encrypted symmetric key
            try {
                Cipher pkCipher;
                pkCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
                // write AES key
                pkCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
                byte[] pkEncryptedKey = pkCipher.doFinal(key);
                String alg = pkCipher.getAlgorithm();
                Timber.i("Algorithm Used: %s", alg);
                base64RsaEncryptedSymmetricKey = Base64.encodeToString(pkEncryptedKey, Base64.NO_WRAP);

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                Timber.e(e, "Unable to encrypt the symmetric key.");
                throw new IllegalArgumentException(e.getMessage());
            }

            // start building elementSignatureSource...
            appendElementSignatureSource(formId);
            if (formVersion != null) {
                appendElementSignatureSource(formVersion);
            }
            appendElementSignatureSource(base64RsaEncryptedSymmetricKey);

            appendElementSignatureSource(instanceMetadata.instanceId);
        }

        public void appendElementSignatureSource(String value) {
            elementSignatureSource.append(value).append('\n');
        }

        public void appendFileSignatureSource(File file) {
            String md5Hash = Md5.getMd5Hash(file);
            appendElementSignatureSource(file.getName() + "::" + md5Hash);
        }

        public String getBase64EncryptedElementSignature() {
            // Step 0: construct the text of the elements in elementSignatureSource (done)
            //     Where...
            //      * Elements are separated by newline characters.
            //      * Filename is the unencrypted filename (no .enc suffix).
            //      * Md5 hashes of the unencrypted files' contents are converted
            //        to zero-padded 32-character strings before concatenation.
            //      Assumes this is in the order:
            //          formId
            //          version   (omitted if null)
            //          base64RsaEncryptedSymmetricKey
            //          instanceId
            //          for each media file { filename "::" md5Hash }
            //          submission.xml "::" md5Hash

            // Step 1: construct the (raw) md5 hash of Step 0.
            byte[] messageDigest;
            try {
                MessageDigest md = MessageDigest.getInstance("MD5");
                md.update(elementSignatureSource.toString().getBytes(UTF_8));
                messageDigest = md.digest();
            } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
                Timber.e(e, "Exception thrown while constructing md5 hash.");
                throw new IllegalArgumentException(e.getMessage());
            }

            // Step 2: construct the base64-encoded RSA-encrypted md5
            try {
                Cipher pkCipher;
                pkCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
                // write AES key
                pkCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
                byte[] pkEncryptedKey = pkCipher.doFinal(messageDigest);
                return Base64.encodeToString(pkEncryptedKey, Base64.NO_WRAP);

            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
                Timber.e(e, "Unable to encrypt the symmetric key.");
                throw new IllegalArgumentException(e.getMessage());
            }
        }

        public Cipher getCipher() throws InvalidKeyException,
                InvalidAlgorithmParameterException, NoSuchAlgorithmException,
                NoSuchPaddingException {
            ++ivSeedArray[ivCounter % ivSeedArray.length];
            ++ivCounter;
            IvParameterSpec baseIv = new IvParameterSpec(ivSeedArray);
            Cipher c = null;
            try {
                c = Cipher.getInstance(EncryptionUtils.SYMMETRIC_ALGORITHM, "BC");
                isNotBouncyCastle = false;
            } catch (NoSuchProviderException e) {
                Timber.w(e, "Unable to obtain BouncyCastle provider! Decryption may fail.");
                isNotBouncyCastle = true;
                c = Cipher.getInstance(EncryptionUtils.SYMMETRIC_ALGORITHM);
            }
            c.init(Cipher.ENCRYPT_MODE, symmetricKey, baseIv);
            return c;
        }

        public boolean isNotBouncyCastle() {
            return isNotBouncyCastle;
        }
    }

    /**
     * Retrieve the encryption information for this uri.
     *
     * @param uri              an Instance uri
     * @param instanceMetadata the metadata for this instance used to check if the form definition
     *                         defines an instanceID
     * @return an {@link EncryptedFormInformation} object if the form definition requests encryption
     * and the record can be encrypted. {@code null} if the form definition does not request
     * encryption or if the BouncyCastle implementation is not present.
     * @throws EncryptionException if the form definition requests encryption but the record can't
     *                             be encrypted
     */
    public static EncryptedFormInformation getEncryptedFormInformation(Uri uri, InstanceMetadata instanceMetadata) throws EncryptionException {
        // fetch the form information
        String formId;
        String formVersion;
        PublicKey pk;

        Form form = null;

        if (InstancesContract.CONTENT_ITEM_TYPE.equals(Collect.getInstance().getContentResolver().getType(uri))) {
            Instance instance = new InstancesRepositoryProvider(Collect.getInstance()).get().get(ContentUriHelper.getIdFromUri(uri));
            if (instance == null) {
                String msg = TranslationHandler.getString(Collect.getInstance(), R.string.not_exactly_one_record_for_this_instance);
                Timber.e(msg);
                throw new EncryptionException(msg, null);
            }

            formId = instance.getFormId();
            formVersion = instance.getFormVersion();

            List<Form> forms = new FormsRepositoryProvider(Collect.getInstance()).get().getAllByFormIdAndVersion(formId, formVersion);

            // OK to finalize with form definition that was soft-deleted. OK if there are multiple
            // forms with the same formid/version as long as only one is active (not deleted).
            if (forms.isEmpty() || new FormsRepositoryProvider(Collect.getInstance()).get().getAllNotDeletedByFormIdAndVersion(formId, formVersion).size() > 1) {
                String msg = TranslationHandler.getString(Collect.getInstance(), R.string.not_exactly_one_blank_form_for_this_form_id);
                Timber.d(msg);
                throw new EncryptionException(msg, null);
            }

            form = forms.get(0);
        } else if (FormsContract.CONTENT_ITEM_TYPE.equals(Collect.getInstance().getContentResolver().getType(uri))) {
            throw new IllegalArgumentException("Can't get encryption info for Form URI!");
        }

        formId = form.getFormId();
        if (formId == null || formId.length() == 0) {
            String msg = TranslationHandler.getString(Collect.getInstance(), R.string.no_form_id_specified);
            Timber.d(msg);
            throw new EncryptionException(msg, null);
        }
        formVersion = form.getVersion();
        String base64RsaPublicKey = form.getBASE64RSAPublicKey();

        if (base64RsaPublicKey == null) {
            return null; // this is legitimately not an encrypted form
        }

        byte[] publicKey = Base64.decode(base64RsaPublicKey, Base64.NO_WRAP);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance(RSA_ALGORITHM);
        } catch (NoSuchAlgorithmException e) {
            String msg = TranslationHandler.getString(Collect.getInstance(), R.string.phone_does_not_support_rsa);
            Timber.d(e, "%s due to %s ", msg, e.getMessage());
            throw new EncryptionException(msg, e);
        }
        try {
            pk = kf.generatePublic(publicKeySpec);
        } catch (InvalidKeySpecException e) {
            String msg = TranslationHandler.getString(Collect.getInstance(), R.string.invalid_rsa_public_key);
            Timber.d(e, "%s due to %s ", msg, e.getMessage());
            throw new EncryptionException(msg, e);
        }

        // submission must have an OpenRosa metadata block with a non-null instanceID
        if (instanceMetadata.instanceId == null) {
            throw new EncryptionException("This form does not specify an instanceID. You must specify one to enable encryption.", null);
        }

        // For now, prevent encryption if the BouncyCastle implementation is not present.
        // https://code.google.com/p/opendatakit/issues/detail?id=918
        try {
            Cipher.getInstance(EncryptionUtils.SYMMETRIC_ALGORITHM, ENCRYPTION_PROVIDER);
        } catch (NoSuchAlgorithmException | NoSuchProviderException | NoSuchPaddingException e) {
            String msg;
            if (e instanceof NoSuchAlgorithmException) {
                msg = "No BouncyCastle implementation of symmetric algorithm!";
            } else if (e instanceof NoSuchProviderException) {
                msg = "No BouncyCastle provider implementation of symmetric algorithm!";
            } else {
                msg = "No BouncyCastle provider for padding implementation of symmetric algorithm!";
            }
            Timber.d(msg);
            return null;
        }

        return new EncryptedFormInformation(formId, formVersion, instanceMetadata, pk);
    }

    private static void encryptFile(File file, EncryptedFormInformation formInfo)
            throws IOException, EncryptionException {
        File encryptedFile = new File(file.getParentFile(), file.getName()
                + ".enc");

        if (encryptedFile.exists() && !encryptedFile.delete()) {
            throw new IOException("Cannot overwrite " + encryptedFile.getAbsolutePath()
                    + ". Perhaps the file is locked?");
        }

        // add elementSignatureSource for this file...
        formInfo.appendFileSignatureSource(file);

        RandomAccessFile randomAccessFile = null;
        CipherOutputStream cipherOutputStream = null;
        try {
            Cipher c = formInfo.getCipher();

            randomAccessFile = new RandomAccessFile(encryptedFile, "rws");
            ByteArrayOutputStream encryptedData = new ByteArrayOutputStream();
            cipherOutputStream = new CipherOutputStream(encryptedData, c);
            InputStream fin = new FileInputStream(file);
            byte[] buffer = new byte[2048];
            int len = fin.read(buffer);
            while (len != -1) {
                cipherOutputStream.write(buffer, 0, len);
                len = fin.read(buffer);
            }
            fin.close();
            cipherOutputStream.flush();
            cipherOutputStream.close();

            randomAccessFile.write(encryptedData.toByteArray());

            Timber.i("Encrpyted:%s -> %s", file.getName(), encryptedFile.getName());
        } catch (Exception e) {
            String msg = "Error encrypting: " + file.getName() + " -> "
                    + encryptedFile.getName();
            Timber.e(e, "%s due to %s ", msg, e.getMessage());
            throw new EncryptionException(msg, e);
        } finally {
            IOUtils.closeQuietly(cipherOutputStream);

            if (randomAccessFile != null) {
                randomAccessFile.close();
            }
        }
    }

    public static boolean deletePlaintextFiles(File instanceXml, @Nullable File lastSaved) {
        // NOTE: assume the directory containing the instanceXml contains ONLY
        // files related to this one instance.
        File instanceDir = instanceXml.getParentFile();

        boolean allSuccessful = true;

        // Delete files that do not end with ".enc", and do not start with ".";
        // ignore directories
        File[] instanceFiles = instanceDir.listFiles();
        for (File f : instanceFiles) {
            if (f.equals(instanceXml)) {
                continue; // don't touch instance file
            }
            if (f.isDirectory()) {
                continue; // don't handle directories
            }
            if (!f.getName().endsWith(".enc")) {
                // not an encrypted file -- delete it!
                allSuccessful = allSuccessful & f.delete(); // DO NOT
                // short-circuit
            }
        }

        // Delete the last-saved instance, if one exists.
        if (lastSaved != null && lastSaved.exists()) {
            allSuccessful &= lastSaved.delete();
        }

        return allSuccessful;
    }

    private static List<File> encryptSubmissionFiles(File instanceXml,
                                                     File submissionXml, EncryptedFormInformation formInfo)
            throws IOException, EncryptionException {
        // NOTE: assume the directory containing the instanceXml contains ONLY
        // files related to this one instance.
        File instanceDir = instanceXml.getParentFile();

        // encrypt files that do not end with ".enc", and do not start with ".";
        // ignore directories
        File[] allFiles = instanceDir.listFiles();
        List<File> filesToProcess = new ArrayList<>();
        for (File f : allFiles) {
            if (f.equals(instanceXml)) {
                continue; // don't touch restore file
            }
            if (f.equals(submissionXml)) {
                continue; // handled last
            }
            if (f.isDirectory()) {
                continue; // don't handle directories
            }
            if (f.getName().startsWith(".")) {
                continue; // MacOSX garbage
            }
            if (f.getName().endsWith(".enc")) {
                f.delete(); // try to delete this (leftover junk)
            } else {
                filesToProcess.add(f);
            }
        }
        // encrypt here...
        for (File f : filesToProcess) {
            encryptFile(f, formInfo);
        }

        // encrypt the submission.xml as the last file...
        encryptFile(submissionXml, formInfo);

        return filesToProcess;
    }

    /**
     * Constructs the encrypted attachments, encrypted form xml, and the
     * plaintext submission manifest (with signature) for the form submission.
     * <p>
     * Does not delete any of the original files.
     */
    public static void generateEncryptedSubmission(File instanceXml,
                                                   File submissionXml, EncryptedFormInformation formInfo)
            throws IOException, EncryptionException {
        // submissionXml is the submission data to be published to Aggregate
        if (!submissionXml.exists() || !submissionXml.isFile()) {
            throw new IOException("No submission.xml found");
        }

        // TODO: confirm that this xml is not already encrypted...

        // Step 1: encrypt the submission and all the media files...
        List<File> mediaFiles = encryptSubmissionFiles(instanceXml,
                submissionXml, formInfo);

        // Step 2: build the encrypted-submission manifest (overwrites
        // submission.xml)...
        writeSubmissionManifest(formInfo, submissionXml, mediaFiles);
    }

    private static void writeSubmissionManifest(
            EncryptedFormInformation formInfo,
            File submissionXml, List<File> mediaFiles) throws EncryptionException {

        Document d = new Document();
        d.setStandalone(true);
        d.setEncoding(UTF_8);
        Element e = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, DATA);
        e.setPrefix(null, XML_ENCRYPTED_TAG_NAMESPACE);
        e.setAttribute(null, ID, formInfo.formId);
        if (formInfo.formVersion != null) {
            e.setAttribute(null, VERSION, formInfo.formVersion);
        }
        e.setAttribute(null, ENCRYPTED, "yes");
        d.addChild(0, Node.ELEMENT, e);

        int idx = 0;
        Element c;
        c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, BASE64_ENCRYPTED_KEY);
        c.addChild(0, Node.TEXT, formInfo.base64RsaEncryptedSymmetricKey);
        e.addChild(idx++, Node.ELEMENT, c);

        c = d.createElement(XML_OPENROSA_NAMESPACE, META);
        c.setPrefix("orx", XML_OPENROSA_NAMESPACE);
        {
            Element instanceTag = d.createElement(XML_OPENROSA_NAMESPACE, INSTANCE_ID);
            instanceTag.addChild(0, Node.TEXT, formInfo.instanceMetadata.instanceId);
            c.addChild(0, Node.ELEMENT, instanceTag);
        }
        e.addChild(idx++, Node.ELEMENT, c);
        e.addChild(idx++, Node.IGNORABLE_WHITESPACE, NEW_LINE);

        if (mediaFiles != null) {
            for (File file : mediaFiles) {
                c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, MEDIA);
                Element fileTag = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, FILE);
                fileTag.addChild(0, Node.TEXT, file.getName() + ".enc");
                c.addChild(0, Node.ELEMENT, fileTag);
                e.addChild(idx++, Node.ELEMENT, c);
                e.addChild(idx++, Node.IGNORABLE_WHITESPACE, NEW_LINE);
            }
        }

        c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, ENCRYPTED_XML_FILE);
        c.addChild(0, Node.TEXT, submissionXml.getName() + ".enc");
        e.addChild(idx++, Node.ELEMENT, c);

        c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, BASE64_ENCRYPTED_ELEMENT_SIGNATURE);
        c.addChild(0, Node.TEXT, formInfo.getBase64EncryptedElementSignature());
        e.addChild(idx++, Node.ELEMENT, c);

        FileOutputStream fout = null;
        OutputStreamWriter writer = null;
        try {
            fout = new FileOutputStream(submissionXml);
            writer = new OutputStreamWriter(fout, UTF_8);

            KXmlSerializer serializer = new KXmlSerializer();
            serializer.setOutput(writer);
            // setting the response content type emits the xml header.
            // just write the body here...
            d.writeChildren(serializer);
            serializer.flush();
            writer.flush();
            fout.getChannel().force(true);
            writer.close();
        } catch (Exception ex) {
            String msg = "Error writing submission.xml for encrypted submission: "
                    + submissionXml.getParentFile().getName();
            Timber.e(ex, "%s due to : %s ", msg, ex.getMessage());
            throw new EncryptionException(msg, ex);
        } finally {
            IOUtils.closeQuietly(writer);
            IOUtils.closeQuietly(fout);
        }
    }
}
