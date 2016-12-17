/*
 * Copyright (C) 2014 GeoODK
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

/**
 *
 * @author Jon Nordling (jonnordling@gmail.com)
 */
package org.odk.collect.android.spatial;

import android.net.Uri;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class CustomMarkerHelper extends Marker{

	Uri marker_uri;
	String marker_name;
	String marker_id;
	String marker_url;
	String marker_status;
	String marker_geoField;
	
	
	public String getMarker_geoField() {
		return marker_geoField;
	}

	public void setMarker_geoField(String marker_geoField) {
		this.marker_geoField = marker_geoField;
	}

	public CustomMarkerHelper(MapView mapView) {
		super(mapView);
		
		// TODO Auto-generated constructor stub
	}

	public Uri getMarker_uri() {
		return marker_uri;
	}

	public void setMarker_uri(Uri marker_uri) {
		this.marker_uri = marker_uri;
	}

	public String getMarker_name() {
		return marker_name;
	}

	public void setMarker_name(String marker_name) {
		this.marker_name = marker_name;
	}

	public String getMarker_id() {
		return marker_id;
	}

	public void setMarker_id(String marker_id) {
		this.marker_id = marker_id;
	}

	public String getMarker_url() {
		return marker_url;
	}

	public void setMarker_url(String marker_url) {
		this.marker_url = marker_url;
	}

	public String getMarker_status() {
		return marker_status;
	}

	public void setMarker_status(String marker_status) {
		this.marker_status = marker_status;
	}

}
