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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

import org.kxml2.io.KXmlSerializer;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.kxml2.kdom.Node;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.logic.FormController.InstanceMetadata;
import org.odk.collect.android.provider.FormsProviderAPI.FormsColumns;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

/**
 * Utility class for encrypting submissions during the SaveToDiskTask.
 * 
 * @author mitchellsundt@gmail.com
 * 
 */
public class EncryptionUtils {
	private static final String t = "EncryptionUtils";
	public static final String RSA_ALGORITHM = "RSA";
	// the symmetric key we are encrypting with RSA is only 256 bits... use SHA-256
	public static final String ASYMMETRIC_ALGORITHM = "RSA/NONE/OAEPWithSHA256AndMGF1Padding"; 
	public static final String SYMMETRIC_ALGORITHM = "AES/CFB/PKCS5Padding";
	public static final String UTF_8 = "UTF-8";
	public static final int SYMMETRIC_KEY_LENGTH = 256;
	public static final int IV_BYTE_LENGTH = 16;

	// tags in the submission manifest

	private static final String XML_ENCRYPTED_TAG_NAMESPACE = "http://www.opendatakit.org/xforms/encrypted";
	private static final String XML_OPENROSA_NAMESPACE = "http://openrosa.org/xforms";
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
	private static final String BASE64_ENCRYPTED_ELEMENT_SIGNATURE = "base64EncryptedElementSignature";
	private static final String NEW_LINE = "\n";

	private EncryptionUtils() {
	};

	public static final class EncryptedFormInformation {
		public final String formId;
		public final String formVersion;
		public final InstanceMetadata instanceMetadata;
		public final PublicKey rsaPublicKey;
		public final String base64RsaEncryptedSymmetricKey;
		public final SecretKeySpec symmetricKey;
		public final byte[] ivSeedArray;
		private int ivCounter = 0;
		public final StringBuilder elementSignatureSource = new StringBuilder();
		public final Base64Wrapper wrapper;

		EncryptedFormInformation(String formId, String formVersion,
				InstanceMetadata instanceMetadata, PublicKey rsaPublicKey, Base64Wrapper wrapper) {
			this.formId = formId;
			this.formVersion = formVersion;
			this.instanceMetadata = instanceMetadata;
			this.rsaPublicKey = rsaPublicKey;
			this.wrapper = wrapper;

			// generate the symmetric key from random bits...
				
			SecureRandom r = new SecureRandom();
			byte[] key = new byte[SYMMETRIC_KEY_LENGTH/8];
			r.nextBytes(key);
			SecretKeySpec sk = new SecretKeySpec(key, SYMMETRIC_ALGORITHM);
			symmetricKey = sk;
		
			// construct the fixed portion of the iv -- the ivSeedArray
			// this is the md5 hash of the instanceID and the symmetric key
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            md.update(instanceMetadata.instanceId.getBytes(UTF_8));
	            md.update(key);
	            byte[] messageDigest = md.digest();
	            ivSeedArray = new byte[IV_BYTE_LENGTH];
	            for ( int i = 0 ; i < IV_BYTE_LENGTH ; ++i ) {
	            	ivSeedArray[i] = messageDigest[(i % messageDigest.length)];
	            }
	        } catch (NoSuchAlgorithmException e) {
	            Log.e(t, e.toString());
	            e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
	        } catch (UnsupportedEncodingException e) {
	            Log.e(t, e.toString());
	            e.printStackTrace();
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
				Log.i(t, "AlgorithmUsed: " + alg);
				base64RsaEncryptedSymmetricKey = wrapper
						.encodeToString(pkEncryptedKey);

			} catch (NoSuchAlgorithmException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (NoSuchPaddingException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (InvalidKeyException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (IllegalBlockSizeException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (BadPaddingException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			}
			
			// start building elementSignatureSource...
			appendElementSignatureSource(formId);
			if ( formVersion != null ) {
				appendElementSignatureSource(formVersion.toString());
			}
			appendElementSignatureSource(base64RsaEncryptedSymmetricKey);

			appendElementSignatureSource( instanceMetadata.instanceId );
		}

		public void appendElementSignatureSource(String value) {
			elementSignatureSource.append(value).append("\n");
		}

		public void appendFileSignatureSource(File file) {
			String md5Hash = FileUtils.getMd5Hash(file);
			appendElementSignatureSource(file.getName()+"::"+md5Hash);
		}
		
		public String getBase64EncryptedElementSignature() {
			// Step 0: construct the text of the elements in elementSignatureSource (done)
			// 		Where...
			//      * Elements are separated by newline characters.
			//      * Filename is the unencrypted filename (no .enc suffix).
			//      * Md5 hashes of the unencrypted files' contents are converted 
			//        to zero-padded 32-character strings before concatenation.
			//      Assumes this is in the order:
			//			formId
			//			version   (omitted if null)
			//			base64RsaEncryptedSymmetricKey
			//			instanceId
			//          for each media file { filename "::" md5Hash }
			//          submission.xml "::" md5Hash

			// Step 1: construct the (raw) md5 hash of Step 0.
			byte[] messageDigest;
	        try {
	            MessageDigest md = MessageDigest.getInstance("MD5");
	            md.update(elementSignatureSource.toString().getBytes(UTF_8));
	            messageDigest = md.digest();
	        } catch (NoSuchAlgorithmException e) {
	            Log.e(t, e.toString());
	            e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
	        } catch (UnsupportedEncodingException e) {
	            Log.e(t, e.toString());
	            e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			}

			// Step 2: construct the base64-encoded RSA-encrypted md5
	        try {				
				Cipher pkCipher;
				pkCipher = Cipher.getInstance(ASYMMETRIC_ALGORITHM);
				// write AES key
				pkCipher.init(Cipher.ENCRYPT_MODE, rsaPublicKey);
				byte[] pkEncryptedKey = pkCipher.doFinal(messageDigest);
				return wrapper.encodeToString(pkEncryptedKey);

			} catch (NoSuchAlgorithmException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (NoSuchPaddingException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (InvalidKeyException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (IllegalBlockSizeException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			} catch (BadPaddingException e) {
				Log.e(t, "Unable to encrypt the symmetric key");
				e.printStackTrace();
				throw new IllegalArgumentException(e.getMessage());
			}
		}
		
		public Cipher getCipher() throws InvalidKeyException,
				InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchPaddingException {
			++ivSeedArray[ivCounter % ivSeedArray.length];
			++ivCounter;
			IvParameterSpec baseIv = new IvParameterSpec(ivSeedArray);
			Cipher c = Cipher.getInstance(EncryptionUtils.SYMMETRIC_ALGORITHM);
			c.init(Cipher.ENCRYPT_MODE, symmetricKey, baseIv);
			return c;
		}
	}

	/**
	 * Retrieve the encryption information for this uri.
	 * 
	 * @param mUri either an instance URI (if previously saved) or a form URI
	 * @param instanceMetadata 
	 * @return
	 */
	public static EncryptedFormInformation getEncryptedFormInformation(Uri mUri, InstanceMetadata instanceMetadata) {
		
		ContentResolver cr = Collect.getInstance().getContentResolver();
		
		// fetch the form information
		String formId;
		String formVersion;
		PublicKey pk;
		Base64Wrapper wrapper;

		Cursor formCursor = null;
		try {
			if (cr.getType(mUri) == InstanceColumns.CONTENT_ITEM_TYPE) {
				// chain back to the Form record...
				String[] selectionArgs = null;
				String selection = null;
				Cursor instanceCursor = null;
				try {
					instanceCursor = cr.query(mUri, null, null, null, null);
					if ( instanceCursor.getCount() != 1 ) {
						Log.e(t, "Not exactly one record for this instance!");
						return null; // save unencrypted.
					}
					instanceCursor.moveToFirst();
					String jrFormId = instanceCursor.getString(instanceCursor.getColumnIndex(InstanceColumns.JR_FORM_ID));
					int idxJrVersion = instanceCursor.getColumnIndex(InstanceColumns.JR_VERSION);
					if ( !instanceCursor.isNull(idxJrVersion) ) {
						selectionArgs = new String[] {jrFormId, instanceCursor.getString(idxJrVersion)};
						selection = FormsColumns.JR_FORM_ID + " =? AND " + FormsColumns.JR_VERSION + "=?";
					} else {
						selectionArgs = new String[] {jrFormId};
						selection = FormsColumns.JR_FORM_ID + " =? AND " + FormsColumns.JR_VERSION + " IS NULL";
					}
				} finally {
					if ( instanceCursor != null ) {
						instanceCursor.close();
					}
				}
	
		        formCursor = cr.query(FormsColumns.CONTENT_URI, null, selection, selectionArgs,
		                null);
		        
		        if (formCursor.getCount() != 1) {
		        	Log.e(t, "Not exactly one blank form matches this jr_form_id");
		        	return null; // save unencrypted
		        }
	            formCursor.moveToFirst();
			} else if (cr.getType(mUri) == FormsColumns.CONTENT_ITEM_TYPE) {
				formCursor = cr.query(mUri, null, null, null, null);
				if ( formCursor.getCount() != 1 ) {
					Log.e(t, "Not exactly one blank form!");
					return null; // save unencrypted.
				}
				formCursor.moveToFirst();
			}
			
			formId = formCursor.getString(formCursor.getColumnIndex(FormsColumns.JR_FORM_ID));
			if (formId == null || formId.length() == 0) {
				Log.e(t, "No FormId specified???");
				return null;
			}
			int idxVersion = formCursor.getColumnIndex(FormsColumns.JR_VERSION);
			int idxBase64RsaPublicKey = formCursor.getColumnIndex(FormsColumns.BASE64_RSA_PUBLIC_KEY);
			formVersion = formCursor.isNull(idxVersion) ? null : formCursor.getString(idxVersion);
			String base64RsaPublicKey = formCursor.isNull(idxBase64RsaPublicKey) 
					? null : formCursor.getString(idxBase64RsaPublicKey);

			if (base64RsaPublicKey == null || base64RsaPublicKey.length() == 0) {
				return null; // this is legitimately not an encrypted form
			}

			int version = android.os.Build.VERSION.SDK_INT;
			if (version < 8) {
				Log.e(t, "Phone does not support encryption.");
				return null; // save unencrypted
			}
			
			// this constructor will throw an exception if we are not
			// running on version 8 or above (if Base64 is not found).
			try {
				wrapper = new Base64Wrapper();
			} catch (ClassNotFoundException e) {
				Log.e(t, "Phone does not have Base64 class but API level is "
						+ version);
				e.printStackTrace();
				return null; // save unencrypted
			}

			// OK -- Base64 decode (requires API Version 8 or higher)
			byte[] publicKey = wrapper.decode(base64RsaPublicKey);
			X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKey);
			KeyFactory kf;
			try {
				kf = KeyFactory.getInstance(RSA_ALGORITHM);
			} catch (NoSuchAlgorithmException e) {
				Log.e(t, "Phone does not support RSA encryption.");
				e.printStackTrace();
				return null;
			}
			try {
				pk = kf.generatePublic(publicKeySpec);
			} catch (InvalidKeySpecException e) {
				e.printStackTrace();
				Log.e(t, "Invalid RSA public key.");
				return null;
			}
		} finally {
			if (formCursor != null) {
				formCursor.close();
			}
		}
		
		// submission must have an OpenRosa metadata block with a non-null
		// instanceID value.
		if (instanceMetadata.instanceId == null) {
			Log.e(t, "No OpenRosa metadata block or no instanceId defined in that block");
			return null;
		}

		return new EncryptedFormInformation(formId, formVersion, instanceMetadata,
				pk, wrapper);
	}

	private static void encryptFile(File file, EncryptedFormInformation formInfo)
			throws IOException, NoSuchAlgorithmException,
			NoSuchPaddingException, InvalidKeyException,
			InvalidAlgorithmParameterException {
		File encryptedFile = new File(file.getParentFile(), file.getName()
				+ ".enc");
		
		// add elementSignatureSource for this file...
		formInfo.appendFileSignatureSource(file);
		
		try {
			Cipher c = formInfo.getCipher();

			OutputStream fout;
			fout = new FileOutputStream(encryptedFile);
			fout = new CipherOutputStream(fout, c);
			InputStream fin;
			fin = new FileInputStream(file);
			byte[] buffer = new byte[2048];
			int len = fin.read(buffer);
			while (len != -1) {
				fout.write(buffer, 0, len);
				len = fin.read(buffer);
			}
			fin.close();
			fout.flush();
			fout.close();
			Log.i(t,
					"Encrpyted:" + file.getName() + " -> "
							+ encryptedFile.getName());
		} catch (IOException e) {
			Log.e(t, "Error encrypting: " + file.getName() + " -> "
					+ encryptedFile.getName());
			e.printStackTrace();
			throw e;
		} catch (NoSuchAlgorithmException e) {
			Log.e(t, "Error encrypting: " + file.getName() + " -> "
					+ encryptedFile.getName());
			e.printStackTrace();
			throw e;
		} catch (NoSuchPaddingException e) {
			Log.e(t, "Error encrypting: " + file.getName() + " -> "
					+ encryptedFile.getName());
			e.printStackTrace();
			throw e;
		} catch (InvalidKeyException e) {
			Log.e(t, "Error encrypting: " + file.getName() + " -> "
					+ encryptedFile.getName());
			e.printStackTrace();
			throw e;
		} catch (InvalidAlgorithmParameterException e) {
			Log.e(t, "Error encrypting: " + file.getName() + " -> "
					+ encryptedFile.getName());
			e.printStackTrace();
			throw e;
		}
	}

	public static boolean deletePlaintextFiles(File instanceXml) {
		// NOTE: assume the directory containing the instanceXml contains ONLY
		// files related to this one instance.
		File instanceDir = instanceXml.getParentFile();

		boolean allSuccessful = true;
		// encrypt files that do not end with ".enc", and do not start with ".";
		// ignore directories
		File[] allFiles = instanceDir.listFiles();
		for (File f : allFiles) {
			if (f.equals(instanceXml))
				continue; // don't touch instance file
			if (f.isDirectory())
				continue; // don't handle directories
			if (!f.getName().endsWith(".enc")) {
				// not an encrypted file -- delete it!
				allSuccessful = allSuccessful & f.delete(); // DO NOT
															// short-circuit
			}
		}
		return allSuccessful;
	}

	private static List<File> encryptSubmissionFiles(File instanceXml,
			File submissionXml, EncryptedFormInformation formInfo) {
		// NOTE: assume the directory containing the instanceXml contains ONLY
		// files related to this one instance.
		File instanceDir = instanceXml.getParentFile();

		// encrypt files that do not end with ".enc", and do not start with ".";
		// ignore directories
		File[] allFiles = instanceDir.listFiles();
		List<File> filesToProcess = new ArrayList<File>();
		for (File f : allFiles) {
			if (f.equals(instanceXml))
				continue; // don't touch restore file
			if (f.equals(submissionXml))
				continue; // handled last
			if (f.isDirectory())
				continue; // don't handle directories
			if (f.getName().startsWith("."))
				continue; // MacOSX garbage
			if (f.getName().endsWith(".enc")) {
				f.delete(); // try to delete this (leftover junk)
			} else {
				filesToProcess.add(f);
			}
		}
		// encrypt here...
		for (File f : filesToProcess) {
			try {
				encryptFile(f, formInfo);
			} catch (IOException e) {
				return null;
			} catch (InvalidKeyException e) {
				return null;
			} catch (NoSuchAlgorithmException e) {
				return null;
			} catch (NoSuchPaddingException e) {
				return null;
			} catch (InvalidAlgorithmParameterException e) {
				return null;
			}
		}

		// encrypt the submission.xml as the last file...
		try {
			encryptFile(submissionXml, formInfo);
		} catch (IOException e) {
			return null;
		} catch (InvalidKeyException e) {
			return null;
		} catch (NoSuchAlgorithmException e) {
			return null;
		} catch (NoSuchPaddingException e) {
			return null;
		} catch (InvalidAlgorithmParameterException e) {
			return null;
		}

		return filesToProcess;
	}

	/**
	 * Constructs the encrypted attachments, encrypted form xml, and the 
	 * plaintext submission manifest (with signature) for the form submission.
	 * 
	 * Does not delete any of the original files.
	 * 
	 * @param instanceXml
	 * @param submissionXml
	 * @param metadata
	 * @param formInfo
	 * @return
	 */
	public static boolean generateEncryptedSubmission(File instanceXml,
			File submissionXml, EncryptedFormInformation formInfo) {
		// submissionXml is the submission data to be published to Aggregate
		if (!submissionXml.exists() || !submissionXml.isFile()) {
			Log.e(t, "No submission.xml found");
			return false;
		}
		
		// TODO: confirm that this xml is not already encrypted...

		// Step 1: encrypt the submission and all the media files...
		List<File> mediaFiles = encryptSubmissionFiles(instanceXml,
				submissionXml, formInfo);
		if (mediaFiles == null) {
			return false; // something failed...
		}

		// Step 2: build the encrypted-submission manifest (overwrites
		// submission.xml)...
		if (!writeSubmissionManifest(formInfo, submissionXml, mediaFiles)) {
			return false;
		}
		return true;
	}
	
	private static boolean writeSubmissionManifest(
			EncryptedFormInformation formInfo,
			File submissionXml, List<File> mediaFiles) {

		Document d = new Document();
		d.setStandalone(true);
		d.setEncoding(UTF_8);
		Element e = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, DATA);
		e.setPrefix(null, XML_ENCRYPTED_TAG_NAMESPACE);
		e.setAttribute(null, ID, formInfo.formId);
		if ( formInfo.formVersion != null ) {
			e.setAttribute(null, VERSION, formInfo.formVersion);
		}
		e.setAttribute(null,  ENCRYPTED, "yes");
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
		  
		for (File file : mediaFiles) {
			c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE,  MEDIA);
			Element fileTag = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE,  FILE);
			fileTag.addChild(0,  Node.TEXT, file.getName() + ".enc");
			c.addChild(0, Node.ELEMENT, fileTag);
			e.addChild(idx++, Node.ELEMENT, c);
			e.addChild(idx++, Node.IGNORABLE_WHITESPACE, NEW_LINE);
		}
		
		c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, ENCRYPTED_XML_FILE);
		c.addChild(0, Node.TEXT, submissionXml.getName() + ".enc");
		e.addChild(idx++, Node.ELEMENT, c);

		c = d.createElement(XML_ENCRYPTED_TAG_NAMESPACE, BASE64_ENCRYPTED_ELEMENT_SIGNATURE);
		c.addChild(0, Node.TEXT, formInfo.getBase64EncryptedElementSignature());
		e.addChild(idx++, Node.ELEMENT, c);

		FileOutputStream out;
		try {
			out = new FileOutputStream(submissionXml);
			OutputStreamWriter writer = new OutputStreamWriter(out, UTF_8);
			
			KXmlSerializer serializer = new KXmlSerializer();
			serializer.setOutput(writer);
			// setting the response content type emits the xml header.
			// just write the body here...
			d.writeChildren(serializer);
			serializer.flush();
			writer.flush();
			writer.close();
		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
			Log.e(t, "Error writing submission.xml for encrypted submission: "
					+ submissionXml.getParentFile().getName());
			return false;
		} catch (UnsupportedEncodingException ex) {
			ex.printStackTrace();
			Log.e(t, "Error writing submission.xml for encrypted submission: "
					+ submissionXml.getParentFile().getName());
			return false;
		} catch (IOException ex) {
			ex.printStackTrace();
			Log.e(t, "Error writing submission.xml for encrypted submission: "
					+ submissionXml.getParentFile().getName());
			return false;
		}

		return true;
	}
}
