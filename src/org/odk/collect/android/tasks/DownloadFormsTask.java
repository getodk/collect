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

package org.odk.collect.android.tasks;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.protocol.HttpContext;
import org.javarosa.xform.parse.XFormParser;
import org.kxml2.io.KXmlParser;
import org.kxml2.kdom.Document;
import org.kxml2.kdom.Element;
import org.odk.collect.android.activities.FormDownloadList;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.FormDownloaderListener;
import org.odk.collect.android.listeners.FormDownloaderListener.FormDetails;
import org.odk.collect.android.provider.FormsStorage;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.xmlpull.v1.XmlPullParser;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

/**
 * Background task for downloading forms from urls or a formlist from a url. We overload this task a
 * bit so that we don't have to keep track of two separate downloading tasks and it simplifies
 * interfaces. If LIST_URL is passed to doInBackground(), we fetch a form list. If a hashmap
 * containing form/url pairs is passed, we download those forms.
 * 
 * @author carlhartung
 */
public class DownloadFormsTask extends
        AsyncTask<HashMap<String, FormDetails>, String, HashMap<String, FormDetails>> {
	
	private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST = "http://openrosa.org/xforms/xformsManifest";

	private static final String NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST = "http://openrosa.org/xforms/xformsList";

	private static final String MD5_COLON_PREFIX = "md5:";

	private static final String t = "DownloadFormsTask";
	
    // used to store form name if one errors
    public static final String DL_FORM = "dlform";

    // used to store error message if one occurs
    public static final String DL_ERROR_MSG = "dlerrormessage";

    // used to indicate that we tried to download forms. If it's not included we tried to download a
    // form list.
    public static final String DL_FORMS = "dlforms";

    private static final int CONNECTION_TIMEOUT = 30000;
    
    private FormDownloaderListener mStateListener;
    
    private boolean isXformsListNamespacedElement(Element e) {
		return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_LIST);
    }
    
    private boolean isXformsManifestNamespacedElement(Element e) {
		return e.getNamespace().equalsIgnoreCase(NAMESPACE_OPENROSA_ORG_XFORMS_XFORMS_MANIFEST);
    }
    
    @Override
    protected HashMap<String, FormDetails> doInBackground(HashMap<String, FormDetails>... values) {
        if (values != null && values[0].containsKey(FormDownloadList.LIST_URL)) {
            // This gets a list of available forms from the specified server.
            HashMap<String, FormDetails> formList =	new HashMap<String, FormDetails>();
            
            // get shared HttpContext so that authentication and cookies are retained.
            HttpContext localContext = Collect.getInstance().getHttpContext();
            
            URI u = null;
            try {
            	FormDetails f = values[0].get(FormDownloadList.LIST_URL);
                URL url = new URL(f.stringValue);
                u = url.toURI();
            } catch (MalformedURLException e) {
                formList.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
                e.printStackTrace();
                return formList;
            } catch (URISyntaxException e) {
                formList.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
                e.printStackTrace();
                return formList;
            }
            
            HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

            // set up request...
            HttpGet req = WebUtils.createOpenRosaHttpGet(u);
        	
        	HttpResponse response = null;
            try {
            	response = httpclient.execute(req, localContext);
            	int statusCode = response.getStatusLine().getStatusCode();
            	
            	if ( statusCode != 200 ) {
                    formList.put(DL_ERROR_MSG, 
                    		new FormDetails("Fetch of forms failed (" + statusCode + ") " + response.getStatusLine().getReasonPhrase()));
                    return formList;
            	}

            	// write connection to file
            	HttpEntity entity = response.getEntity();
            	if ( entity == null ) {
                    formList.put(DL_ERROR_MSG, 
                    		new FormDetails("Fetch of forms failed -- no message body!"));
                    return formList;
            	}
            	// TODO: check that it is text/xml ?
            	Document doc = null;
                try {
                    InputStream is = null;
                    InputStreamReader isr = null;
	                try {
	                	is = entity.getContent();
	                	isr = new InputStreamReader(is, "UTF-8");
		                doc = new Document();
		                KXmlParser parser = new KXmlParser();
		                parser.setInput(isr);
		                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
		                doc.parse(parser);
	                	isr.close();
	                } finally {
	                	if ( isr != null ) {
	                		try {
	                			isr.close();
	                		} catch ( Exception e) {
	                			// no-op
	                		}
	                	}
	                	if ( is != null ) {
		                	try {
		                		is.close();
		                	} catch ( Exception e) {
		                		// no-op
		                	}
	                	}
	                }
                } catch (Exception e) {
                    formList.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
                    return formList;
                }

                Header[] fields = response.getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
                if ( fields != null && fields.length >= 1 ) {
                	boolean versionMatch = false;
                	boolean first = true;
                	StringBuilder b = new StringBuilder();
                	for ( Header h : fields ) {
                		if ( WebUtils.OPEN_ROSA_VERSION.equals(h.getValue()) ) {
                			versionMatch = true;
                			break;
                		}
                		if ( !first ) {
                			b.append("; ");
                		}
                		first = false;
                		b.append(h.getValue());
                	}
                	if ( !versionMatch ) {
                		Log.w(t, WebUtils.OPEN_ROSA_VERSION_HEADER + " unrecognized version(s): " + b.toString());
                	}
                
                	// Attempt OpenRosa 1.0 parsing
                	Element xformsElement = doc.getRootElement();
                	if ( !xformsElement.getName().equals("xforms") ) {
                		formList.put(DL_ERROR_MSG, new FormDetails("Root element is not <xforms>"));
                		return formList;
                	}
                	String namespace = xformsElement.getNamespace();
                	if ( !isXformsListNamespacedElement(xformsElement) ) {
                		formList.put(DL_ERROR_MSG, new FormDetails("Namespace is incorrect: " + namespace));
                		return formList;
                	}
                	int nElements = xformsElement.getChildCount();
                	for ( int i = 0 ; i < nElements ; ++i ) {
                		if ( xformsElement.getType(i) != Element.ELEMENT ) {
                			// e.g., whitespace (text)
                			continue;
                		}
                		Element xformElement = (Element) xformsElement.getElement(i);
                		if ( !isXformsListNamespacedElement(xformElement) ) {
            				// someone else's extension?
                			continue;
                		}
            			String name = xformElement.getName();
            			if ( !name.equalsIgnoreCase("xform")) {
            				// someone else's extension?
            				continue;
            			}

            			// this is something we know how to interpret
            			String formId = null;
        				String formName = null;
        				String majorMinorVersion = null;
        				String description = null;
        				String downloadUrl = null;
        				String manifestUrl = null;
        				// don't process descriptionUrl
        				int fieldCount = xformElement.getChildCount();
            			for ( int j = 0 ; j < fieldCount ; ++j ) {
            				if ( xformElement.getType(j) != Element.ELEMENT ) {
            					// whitespace
            					continue;
            				}
            				Element child = xformElement.getElement(j);
            				if ( !isXformsListNamespacedElement(child) ) {
            					// someone else's extension?
            					continue;
            				}
                			String tag = child.getName();
            				if ( tag.equals("formID")) {
            					formId = XFormParser.getXMLText(child, true);
            					if ( formId != null && formId.length() == 0 ) {
            						formId = null;
            					}
            				}
            				else if (tag.equals("name")) {
            					formName = XFormParser.getXMLText(child, true);
            					if ( formName != null && formName.length() == 0 ) {
            						formName = null;
            					}
            				}
            				else if (tag.equals("majorMinorVersion")) {
            					majorMinorVersion = XFormParser.getXMLText(child, true);
            					if ( majorMinorVersion != null && majorMinorVersion.length() == 0 ) {
            						majorMinorVersion = null;
            					}
            				}
            				else if (tag.equals("descriptionText")) {
            					description = XFormParser.getXMLText(child, true);
            					if ( description != null && description.length() == 0 ) {
            						description = null;
            					}
            				}
            				else if (tag.equals("downloadUrl")) {
            					downloadUrl = XFormParser.getXMLText(child, true);
            					if ( downloadUrl != null && downloadUrl.length() == 0 ) {
            						downloadUrl = null;
            					}
            				}
            				else if (tag.equals("manifestUrl")) {
            					manifestUrl = XFormParser.getXMLText(child, true);
            					if ( manifestUrl != null && manifestUrl.length() == 0 ) {
            						manifestUrl = null;
            					}
            				}
            			}
            			if ( formId == null || downloadUrl == null || formName == null ) {
            				formList.put(DL_ERROR_MSG, new FormDetails("Forms list entry " + Integer.toString(i) 
            						+ " is missing one or more tags: formId, name, or downloadUrl"));
            				continue; 
            			}
            			Integer modelVersion = null;
            			Integer uiVersion = null;
            			try {
                			if ( majorMinorVersion == null || majorMinorVersion.length() == 0 ) {
                				modelVersion = null;
                				uiVersion = null;
                			} else {
                				int idx = majorMinorVersion.indexOf(".");
                				if ( idx == -1 ) {
                					modelVersion = Integer.parseInt(majorMinorVersion);
                					uiVersion = null;
                				} else {
                					modelVersion = Integer.parseInt(majorMinorVersion.substring(0,idx));
                					uiVersion = (idx == majorMinorVersion.length()-1) ? null : 
                								Integer.parseInt(majorMinorVersion.substring(idx+1));
                				}
                			}
            			} catch ( Exception e ) {
            				e.printStackTrace();
            				formList.put(DL_ERROR_MSG, new FormDetails("Forms list entry " + Integer.toString(i) 
            						+ " has an invalid majorMinorVersion: " + majorMinorVersion));
            				continue; 
            			}
        				formList.put(formName, 
        						new FormDetails(formName, formId, modelVersion, uiVersion, description, downloadUrl, manifestUrl));
        			}                			
                } else {
                	// Aggregate 0.9.x mode...
	                // populate HashMap with form names and urls
	                Element formsElement = doc.getRootElement();
	                int formsCount = formsElement.getChildCount();
	                for ( int i = 0 ; i < formsCount ; ++i ) {
        				if ( formsElement.getType(i) != Element.ELEMENT ) {
        					// whitespace
        					continue;
        				}
        				Element child = formsElement.getElement(i);
        				String tag = child.getName();
        				if ( tag.equalsIgnoreCase("form") ) {
        					String formName = XFormParser.getXMLText(child, true);
        					if ( formName != null && formName.length() == 0 ) {
        						formName = null;
        					}
        					String downloadUrl = child.getAttributeValue(null, "url");
        					downloadUrl = downloadUrl.trim();
        					if ( downloadUrl != null && downloadUrl.length() == 0 ) {
        						downloadUrl = null;
        					}
                			if ( downloadUrl == null || formName == null ) {
                				formList.put(DL_ERROR_MSG, new FormDetails("Forms list entry " + Integer.toString(i) 
                						+ " is missing form name or url attribute"));
                				continue;
                			}
        					formList.put(formName, new FormDetails(formName, null, null, null, null, downloadUrl, null));
        				}
	                }
                }
            } catch (ClientProtocolException e) {
            	formList.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
            	e.printStackTrace();
            } catch (IOException e) {
                formList.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
                e.printStackTrace();
            }
            return formList;

        } else if (values != null) {
            // This downloads the selected forms.
            HashMap<String, FormDetails> toDownload = values[0];
            HashMap<String, FormDetails> result = new HashMap<String, FormDetails>();
            result.put(DL_FORMS, new FormDetails(DL_FORMS)); // indicate that we're trying to download forms.
            ArrayList<FormDetails> forms = new ArrayList<FormDetails>(toDownload.values());

            // boolean error = false;
            int total = forms.size();
            int count = 1;

            for (int i = 0; i < total; i++) {
                FormDetails fd = forms.get(i);
                publishProgress(fd.formName, Integer.valueOf(count).toString(), Integer.valueOf(total)
                		.toString());
                try {
                    File dl = downloadFile(fd.formName, fd.downloadUrl);

                    ContentValues v = new ContentValues();
                    v.put(FormsStorage.KEY_FORM_FILE_PATH, dl.getAbsolutePath());
                    Uri uri = Collect.getInstance().getContentResolver().insert(FormsStorage.CONTENT_URI_INFO_DATASET, v);
                    
                    String[] projection = new String[] {
                    		FormsStorage.KEY_ID,
                    		FormsStorage.KEY_FORM_ID,
                    		FormsStorage.KEY_FORM_MEDIA_PATH
                    };
                    Cursor c = Collect.getInstance().getContentResolver().query(uri, projection, null, null, null);
                    
                    if ( c != null && c.moveToNext() ) {
                    	String mediaPath = c.getString(c.getColumnIndex(FormsStorage.KEY_FORM_MEDIA_PATH));
                    	String formId = c.getString(c.getColumnIndex(FormsStorage.KEY_FORM_ID));
                    	long keyId = c.getLong(c.getColumnIndex(FormsStorage.KEY_ID));

                    	FilterUtils.FilterCriteria fFormId = 
                    		FilterUtils.buildSelectionClause(FormsStorage.KEY_FORM_ID, formId);
                    	FilterUtils.FilterCriteria fNotKeyId =
                    		FilterUtils.buildInverseSelectionClause(FormsStorage.KEY_ID, keyId);
                    	FilterUtils.FilterCriteria fc = FilterUtils.and(fFormId, fNotKeyId);
                    	
                    	int nDel = Collect.getInstance().getContentResolver().delete(FormsStorage.CONTENT_URI_INFO_DATASET,
                    									fc.selection, fc.selectionArgs);
                    	
                    	int idxDot = dl.getName().lastIndexOf(".");
                    	String dlFileNamePart = dl.getName().substring(0,idxDot);
                    	if ( nDel != 0 || !fd.formName.equalsIgnoreCase(dlFileNamePart) ) {
                    		result.put(fd.formName, new FormDetails(dl.getName()));
                    	}
                    	
                    	// TODO: pull down manifest files...
                    	if ( fd.manifestUrl != null ) {
                    		String error = downloadManifestAndMediaFiles(mediaPath, fd, count, total);
                    		if ( error != null ) {
                    			result.put(fd.formName, new FormDetails(error));
                    		}
                    	}
                    } else {
                    	Log.e(t, "Unexpected failure retrieving form info");
                    }
                } catch (SocketTimeoutException se) {
                    se.printStackTrace();
                    result.put(DL_FORM, new FormDetails(fd.formName));
                    result.put(DL_ERROR_MSG, new FormDetails("Unknown timeout exception"));
                    break;
                } catch (Exception e) {
                    e.printStackTrace();
                    result.put(DL_FORM, new FormDetails(fd.formName));
                    result.put(DL_ERROR_MSG, new FormDetails(e.getLocalizedMessage()));
                    break;
                }
                count++;
            }

            return result;
        }
        
		return null;
    }

    private static class MediaFile {
    	final String filename;
    	final String hash;
    	final String downloadUrl;
    	
    	MediaFile(String filename, String hash, String downloadUrl) {
    		this.filename = filename;
    		this.hash = hash;
    		this.downloadUrl = downloadUrl;
    	}
    }

    private String downloadManifestAndMediaFiles(String mediaPath, FormDetails fd, int count, int total) {
    	if ( fd.manifestUrl == null ) return null;

    	publishProgress(fd.formName + " (getting manifest)",
    			Integer.valueOf(count).toString(), 
    			Integer.valueOf(total).toString());
    	
    	List<MediaFile> files = new ArrayList<MediaFile>();
        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();
        
        URI u = null;
        try {
            URL url = new URL(fd.manifestUrl);
            u = url.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return e.getLocalizedMessage();
        }
        
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(u);
    	
    	HttpResponse response = null;
    	int statusCode;
        try {
        	response = httpclient.execute(req, localContext);
        	statusCode = response.getStatusLine().getStatusCode();
        	
        	if ( statusCode != 200 ) {
        		return "Fetch of manifest failed (" + statusCode + ") " + response.getStatusLine().getReasonPhrase();
        	}
        } catch ( ClientProtocolException e ) {
        	return e.getLocalizedMessage();
        } catch ( IOException e ) {
        	return e.getLocalizedMessage();
        }

        HttpEntity entity = response.getEntity();
        if ( entity == null ) {
        	return "No manifest document returned";
        }

        // TODO: check that it is text/xml ?
    	Document doc = null;
        try {
            InputStream is = null;
            InputStreamReader isr = null;
            try {
            	is = entity.getContent();
            	isr = new InputStreamReader(is, "UTF-8");
                doc = new Document();
                KXmlParser parser = new KXmlParser();
                parser.setInput(isr);
                parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, true);
                doc.parse(parser);
            	isr.close();
            } finally {
            	if ( isr != null ) {
            		try {
            			isr.close();
            		} catch ( Exception e) {
            			// no-op
            		}
            	}
            	if ( is != null ) {
                	try {
                		is.close();
                	} catch ( Exception e) {
                		// no-op
                	}
            	}
            }
        } catch (Exception e) {
            return e.getLocalizedMessage();
        }
        
        Header[] fields = response.getHeaders(WebUtils.OPEN_ROSA_VERSION_HEADER);
        if ( fields != null && fields.length >= 1 ) {
        	boolean versionMatch = false;
        	boolean first = true;
        	StringBuilder b = new StringBuilder();
        	for ( Header h : fields ) {
        		if ( WebUtils.OPEN_ROSA_VERSION.equals(h.getValue()) ) {
        			versionMatch = true;
        			break;
        		}
        		if ( !first ) {
        			b.append("; ");
        		}
        		first = false;
        		b.append(h.getValue());
        	}
        	if ( !versionMatch ) {
        		Log.w(t, WebUtils.OPEN_ROSA_VERSION_HEADER + " unrecognized version(s): " + b.toString());
        	}
        }
        
        // Attempt OpenRosa 1.0 parsing
        Element manifestElement = doc.getRootElement();
    	if ( !manifestElement.getName().equals("manifest") ) {
    		return "Root element is not <manifest>";
    	}
    	String namespace = manifestElement.getNamespace();
    	if ( !isXformsManifestNamespacedElement(manifestElement) ) {
    		return "Namespace is incorrect: " + namespace;
    	}
    	int nElements = manifestElement.getChildCount();
    	for ( int i = 0 ; i < nElements ; ++i ) {
    		if ( manifestElement.getType(i) != Element.ELEMENT ) {
    			// e.g., whitespace (text)
    			continue;
    		}
    		Element mediaFileElement = (Element) manifestElement.getElement(i);
    		if ( !isXformsManifestNamespacedElement(mediaFileElement) ) {
				// someone else's extension?
    			continue;
    		}
			String name = mediaFileElement.getName();
			if ( name.equalsIgnoreCase("mediaFile")) {
				String filename = null;
				String hash = null;
				String downloadUrl = null;
				// don't process descriptionUrl
				int childCount = mediaFileElement.getChildCount();
				for ( int j = 0 ; j < childCount ; ++j ) {
					if ( mediaFileElement.getType(j) != Element.ELEMENT ) {
		    			// e.g., whitespace (text)
		    			continue;
					}
					Element child = mediaFileElement.getElement(j);
					if ( !isXformsManifestNamespacedElement(child) ) {
						// someone else's extension?
						continue;
					}
        			String tag = child.getName();
    				if ( tag.equals("filename")) {
    					filename = XFormParser.getXMLText(child, true);
    					if ( filename != null && filename.length() == 0 ) {
    						filename = null;
    					}
    				}
    				else if (tag.equals("hash")) {
    					hash = XFormParser.getXMLText(child, true);
    					if ( hash != null && hash.length() == 0 ) {
    						hash = null;
    					}
    				}
    				else if (tag.equals("downloadUrl")) {
    					downloadUrl = XFormParser.getXMLText(child, true);
    					if ( downloadUrl != null && downloadUrl.length() == 0 ) {
    						downloadUrl = null;
    					}
    				}
    			}
    			if ( filename == null || downloadUrl == null || hash == null ) {
    				return "Manifest entry " + Integer.toString(i) 
    						+ " is missing one or more tags: filename, hash, or downloadUrl";
    			}
				files.add(new MediaFile(filename, hash, downloadUrl));
			}                			
		}
        // OK we now have the full set of files to download...
        Log.i(t, "Downloading " + files.size() + " media files.");
        int mCount = 0;
        if ( files.size() > 0 ) {
        	FileUtils.createFolder(mediaPath);
        	File mediaDir = new File(mediaPath);
	        for ( MediaFile m : files ) {
	        	++mCount;
	        	publishProgress(fd.formName + " (getting " + mCount + " of " + files.size() + " media files)",
	        			Integer.valueOf(count).toString(), 
	        			Integer.valueOf(total).toString());
	        	try {
	        		downloadMediaFileIfChanged( mediaDir, m );
	        	} catch ( Exception e ) {
	        		return e.getLocalizedMessage();
	        	}
	        }
        }
		return null;
	}
    
    private void downloadMediaFileIfChanged( File mediaDir, MediaFile m ) throws Exception {
		File mediaFile = new File(mediaDir, m.filename);

		if ( m.hash.startsWith(MD5_COLON_PREFIX) ) {
    		// see if the file exists and has the same hash
    		String hashToMatch = m.hash.substring(MD5_COLON_PREFIX.length());
    		if ( mediaFile.exists() ) {
    			String hash = FileUtils.getMd5Hash(mediaFile);
    			if ( hash.equalsIgnoreCase(hashToMatch)) return;
    			mediaFile.delete();
    		}
    	}
		
		// OK.  We need to download it because we either:
		// (1) don't have it
		// (2) don't know if it is changed because the hash is not md5
		// (3) know it is changed
        URI u = null;
        try {
            URL uurl = new URL(m.downloadUrl);
            u = uurl.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }
        
        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();
        
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(u);
    	
    	HttpResponse response = null;
        try {
        	response = httpclient.execute(req, localContext);
        	int statusCode = response.getStatusLine().getStatusCode();
        	
        	if ( statusCode != 200 ) {
        		String errMsg = "Unsuccessful download attempt: " + statusCode 
					+ " Reason: " + response.getStatusLine().getReasonPhrase();
        		Log.w(t,errMsg);
        		throw new Exception(errMsg);
        	}

            // write connection to file
            InputStream is = response.getEntity().getContent();

            OutputStream os = new FileOutputStream(mediaFile);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

	private File downloadFile(String formName, String url) throws IOException, URISyntaxException {
        // create url
        File f = null;

        // clean up friendly form name...
        String rootName = formName.replaceAll("[^\\p{L}\\p{Digit}]", " ");
        rootName = rootName.replaceAll("\\p{javaWhitespace}+", " ");
        rootName = rootName.trim();
        FileUtils.createFolder(FileUtils.FORMS_PATH);
        // proposed name of xml file...
        String path = FileUtils.FORMS_PATH + rootName + ".xml";
        String dirPath = FileUtils.FORMS_PATH + rootName + "-media";
        int i = 2;
        f = new File(path);
        File fdir = new File(dirPath);
        while (f.exists() || fdir.exists()) {
        	path = FileUtils.FORMS_PATH + rootName + "_" + i + ".xml";
        	dirPath = FileUtils.FORMS_PATH + rootName + "_" + i + "-media";
        	f = new File(path);
        	fdir = new File(dirPath);
            i++;
        }

        URI u = null;
        try {
            URL uurl = new URL(url);
            u = uurl.toURI();
        } catch (MalformedURLException e) {
            e.printStackTrace();
            throw e;
        } catch (URISyntaxException e) {
            e.printStackTrace();
            throw e;
        }
        
        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();
        
        HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

        // set up request...
        HttpGet req = WebUtils.createOpenRosaHttpGet(u);
    	
    	HttpResponse response = null;
        try {
        	response = httpclient.execute(req, localContext);
        	int statusCode = response.getStatusLine().getStatusCode();
        	
        	if ( statusCode != 200 ) {
        		Log.w(t, "Unsuccessful download attempt: " + statusCode 
        				+ " Reason: " + response.getStatusLine().getReasonPhrase() );
                return null;
        	}

            // write connection to file
            InputStream is = response.getEntity().getContent();

            OutputStream os = new FileOutputStream(f);
            byte buf[] = new byte[1024];
            int len;
            while ((len = is.read(buf)) > 0) {
                os.write(buf, 0, len);
            }
            os.flush();
            os.close();
            is.close();

        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }

        return f;
    }


    @Override
    protected void onPostExecute(HashMap<String, FormDetails> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.formDownloadingComplete(value);
            }
        }
    }


    @Override
    protected void onProgressUpdate(String... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0], new Integer(values[1]).intValue(),
                    new Integer(values[2]).intValue());
            }
        }

    }


    public void setDownloaderListener(FormDownloaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
}
