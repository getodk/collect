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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLException;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.protocol.HttpContext;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.listeners.InstanceUploaderListener;
import org.odk.collect.android.provider.SubmissionsStorage;
import org.odk.collect.android.utilities.FileUtils;
import org.odk.collect.android.utilities.FilterUtils;
import org.odk.collect.android.utilities.WebUtils;
import org.odk.collect.android.utilities.FilterUtils.FilterCriteria;

import android.database.Cursor;
import android.os.AsyncTask;
import android.util.Log;
import android.webkit.MimeTypeMap;

/**
 * Background task for uploading completed forms.
 * 
 * @author Carl Hartung (carlhartung@gmail.com)
 */
public class InstanceUploaderTask extends AsyncTask<String, Integer, ArrayList<String>> {

    private static String t = "InstanceUploaderTask";
    //private static long MAX_BYTES = 1048576 - 1024; // 1MB less 1KB overhead
    InstanceUploaderListener mStateListener;
    private static final int CONNECTION_TIMEOUT = 30000;
    
    /**
     * The values are the names of the instances to upload -- i.e., the directory names.
     * 
     */
    @Override
    protected ArrayList<String> doInBackground(String... values) {
        ArrayList<String> uploadedInstances = new ArrayList<String>();
        int instanceCount = values.length;
        Set<String> instanceDirs = new HashSet<String>();
        instanceDirs.addAll(Arrays.asList(values));

        // get shared HttpContext so that authentication and cookies are retained.
        HttpContext localContext = Collect.getInstance().getHttpContext();
    	HttpClient httpclient = WebUtils.createHttpClient(CONNECTION_TIMEOUT);

    	
    	FilterCriteria fc = FilterUtils.buildSelectionClause(SubmissionsStorage.KEY_STATUS, 
    														 SubmissionsStorage.STATUS_COMPLETE);
    	
    	Cursor c = null;
    	try {
	    	c = Collect.getInstance().getContentResolver().query(SubmissionsStorage.CONTENT_URI_INFO_DATASET,
	    				new String[] { SubmissionsStorage.KEY_ID,
	    								SubmissionsStorage.KEY_SUBMISSION_URI,
	    								SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH },
	    				fc.selection, fc.selectionArgs, null);
	    	
	    	int idxInstanceDir = c.getColumnIndex(SubmissionsStorage.KEY_INSTANCE_DIRECTORY_PATH);
	    	int idxUri = c.getColumnIndex(SubmissionsStorage.KEY_SUBMISSION_URI);
	    	int i = 0;
	    	while ( c.moveToNext() ) {
	    		String instanceDir = c.getString(idxInstanceDir);
	    		if ( !instanceDirs.contains(instanceDir) ) continue;
	    		++i;
	    		String urlString = c.getString(idxUri);
	    		// it is one of the submissions we want to do...
	    		
	            URI u = null;
	    		try {
	    			URL url = new URL(urlString);
	    			u = url.toURI();
	    		} catch ( MalformedURLException e ) {
	    			e.printStackTrace();
	    			Log.e(t, "Invalid url: " + urlString + " for submission " + instanceDir);
	    		} catch (URISyntaxException e ) {
	    			e.printStackTrace();
	    			Log.e(t, "Invalid uri: " + urlString + " for submission " + instanceDir);
	    		}
	
		        boolean openRosaServer = false;
		        HttpHead httpHead = WebUtils.createOpenRosaHttpHead(u);
		
		        // prepare response
		        HttpResponse response = null;
		        try {
		            response = httpclient.execute(httpHead,localContext);
		            int statusCode = response.getStatusLine().getStatusCode();
		            if ( statusCode == 204 ) {
		            	Header[] locations = response.getHeaders("Location");
		            	if ( locations != null && locations.length == 1 ) {
		            		try {
		            			URL url = new URL (locations[0].getValue());
		            			URI uNew = url.toURI();
		            			if ( u.getHost().equalsIgnoreCase(uNew.getHost()) ) {
		            				openRosaServer = true;
		            				// trust the server to tell us a new location
		            				// ... and possibly to use https instead.
		            				u = uNew;
		            			} else {
		            				// Don't follow a redirection attempt to a different host.
		            				// We can't tell if this is a spoof or not.
		            				Log.e(t, "Unexpected redirection attempt to a different host: " + uNew.toString());
		            	            return uploadedInstances;
		            			}
		            		} catch ( MalformedURLException e ) {
		            			e.printStackTrace();
		        	            return uploadedInstances;
		            		} catch (URISyntaxException e) {
								e.printStackTrace();
		        	            return uploadedInstances;
							}
		            	}
		            } else {
		            	try {
		            		// don't really care about the stream...
		            		InputStream is = response.getEntity().getContent();
		            		// read to end of stream...
		            		final long count = 1024L;
		            		while ( is.skip(count) == count);
		            		is.close();
		            	} catch ( IOException e ) {
		            		e.printStackTrace();
		            		return uploadedInstances;
		            	} catch ( IllegalStateException e ) {
		            		e.printStackTrace();
		            		return uploadedInstances;
		            	}
		            	Log.w(t, "Status code on Head request: " + statusCode );
		            }
			    } catch (SSLException e) {
			        e.printStackTrace();
		            return uploadedInstances;
			    } catch (ClientProtocolException e) {
			        e.printStackTrace();
		            return uploadedInstances;
			    } catch (IOException e) {
			        e.printStackTrace();
		            return uploadedInstances;
			    } catch (IllegalStateException e) {
			        e.printStackTrace();
		            return uploadedInstances;
			    } catch (Exception e) {
			        e.printStackTrace();
		            return uploadedInstances;
			    }
			    // At this point, we may have updated the uri to use https.
			    // This occurs only if the Location header keeps the host name
			    // the same.  If it specifies a different host name, we error
			    // out.
			    // 
			    // And we may have set authentication cookies in our 
			    // cookiestore (referenced by localContext) that will enable
			    // authenticated publication to the server.
			    // 
	            publishProgress(i + 1, instanceCount);
	
	            HttpPost httppost = WebUtils.createOpenRosaHttpPost(u);
	
	            // get instance file
	            File instanceFile = new File( FileUtils.getInstanceFilePath(instanceDir));
	            File file = new File( FileUtils.getSubmissionBlobPath(instanceDir));
	            
	            String submissionFile = file.getName();
	            String xmlInstanceFile = instanceFile.getName();
	
	            // find all files in parent directory
	            File[] files = file.getParentFile().listFiles();
	            if (files == null) {
	                Log.e(t, "no files to upload");
	                return uploadedInstances;
	            }
	
	        	MimeTypeMap m = MimeTypeMap.getSingleton();
	
	        	// TODO: limit to < 10Mb
	        	
	        	// mime post
	            MultipartEntity entity = new MultipartEntity();
	            for (int j = 0; j < files.length; j++) {
	                File f = files[j];
	                FileBody fb;
	                String fileName = f.getName();
	                int idx = fileName.lastIndexOf(".");
	                String extension = "";
	                if ( idx != -1 ) {
	                	extension = fileName.substring(idx+1);
	                }
	            	String contentType = m.getMimeTypeFromExtension(extension);
	
	            	if ( fileName.equals(submissionFile) ) {
	            	    fb = new FileBody(f, "text/xml");
	                    entity.addPart("xml_submission_file", fb);
	                    Log.i(t, "added xml_submission_file: " + f.getName());
	            	} else if ( fileName.equals(xmlInstanceFile) ) {
	            		// skip it...we are sending submission file (above)
	            	} else if (extension.equals("xml")) {
	                	if ( openRosaServer ) {
		                    fb = new FileBody(f, "text/xml");
		                    entity.addPart(f.getName(), fb);
		                    Log.i(t, "added xml file " + f.getName());
	                	}
	                } else if (extension.equals("jpg")) {
	                    fb = new FileBody(f, "image/jpeg");
	                    entity.addPart(f.getName(), fb);
	                    Log.i(t, "added image file " + f.getName());
	                } else if (extension.equals("3gpp")) {
	                    fb = new FileBody(f, "audio/3gpp");
	                    entity.addPart(f.getName(), fb);
	                    Log.i(t, "added audio file " + f.getName());
	                } else if (extension.equals("3gp")) {
	                    fb = new FileBody(f, "video/3gpp");
	                    entity.addPart(f.getName(), fb);
	                    Log.i(t, "added video file " + f.getName());
	                } else if (extension.equals("mp4")) {
	                    fb = new FileBody(f, "video/mp4");
	                    entity.addPart(f.getName(), fb);
	                    Log.i(t, "added video file " + f.getName());
	                } else if (openRosaServer) {
	                	if (extension.equals("csv")) {
		                	fb = new FileBody(f, "text/csv");
		                	entity.addPart(f.getName(), fb);
		                	Log.i(t, "added csv file " + f.getName());
		                } else if (extension.equals("xls")) {
		                	fb = new FileBody(f, "application/vnd.ms-excel");
		                	entity.addPart(f.getName(), fb);
		                	Log.i(t, "added xls file " + f.getName());
		                } else if ( contentType != null ) {
		                	fb = new FileBody(f, contentType );
		                	entity.addPart(f.getName(), fb);
		                	Log.i(t, "added recognized filetype (" + contentType + ") " + f.getName());
		                } else {
		                	contentType = "application/octet-stream";
		                	fb = new FileBody(f, contentType);
		                	entity.addPart(f.getName(), fb);
		                    Log.w(t, "added unrecognized file (" + contentType + ") " + f.getName());
		                }
	                } else {
	                	Log.w(t, "unrecognized file type " + f.getName());
	                }
	            }
	            httppost.setEntity(entity);
	
	            // prepare response and return uploaded
	            response = null;
	            try {
	                response = httpclient.execute(httppost,localContext);
	            } catch (ClientProtocolException e) {
	                e.printStackTrace();
	                return uploadedInstances;
	            } catch (IOException e) {
	                e.printStackTrace();
	                return uploadedInstances;
	            } catch (IllegalStateException e) {
	                e.printStackTrace();
	                return uploadedInstances;
	            }
	
	            int responseCode = response.getStatusLine().getStatusCode();
	            Log.i(t, "Response code:" + responseCode);
	            // check response.
	            InputStream is = null;
				BufferedReader r = null;
				try {
					is = response.getEntity().getContent();
					r = new BufferedReader(new InputStreamReader(is));
					String line;
					while ( (line = r.readLine()) != null ) {
						if ( responseCode == 201 || responseCode == 202) {
							Log.i(t, line);
						} else {
							Log.e(t, line);
						}
					}
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				} finally {
					if ( r != null ) {
						try {
							r.close();
						} catch ( Exception e ) {
						} finally {
							r = null;
						}
					}
					if ( is != null ) {
						try {
							is.close();
						} catch ( Exception e ) {
						} finally {
							is = null;
						}
					}
				}
	
	            // verify that the response was a 201 or 202.  
				// If it wasn't, the submission has failed.
	            if (responseCode == 201 || responseCode == 202) {
	                uploadedInstances.add(instanceDir);
	            }
	
	        }
    	} finally {
    		if ( c != null ) {
    			c.close();
    		}
    	}

        return uploadedInstances;
    }


    @Override
    protected void onPostExecute(ArrayList<String> value) {
        synchronized (this) {
            if (mStateListener != null) {
                mStateListener.uploadingComplete(value);
            }
        }
    }


    @Override
    protected void onProgressUpdate(Integer... values) {
        synchronized (this) {
            if (mStateListener != null) {
                // update progress and total
                mStateListener.progressUpdate(values[0].intValue(), values[1].intValue());
            }
        }
    }


    public void setUploaderListener(InstanceUploaderListener sl) {
        synchronized (this) {
            mStateListener = sl;
        }
    }
}
