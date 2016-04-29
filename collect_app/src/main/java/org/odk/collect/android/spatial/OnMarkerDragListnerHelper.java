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
 * @author Jon Nordling (jonnordling@gmail.com)
 */
package org.odk.collect.android.spatial;

import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.views.MapView;

public class OnMarkerDragListnerHelper extends Marker{

	public OnMarkerDragListnerHelper(MapView mapView) {
		super(mapView);
		// TODO Auto-generated constructor stub
	}

	public interface OnMarkerDragListenerHelper{
		abstract void onMarkerDrag(Marker marker);
		abstract void onMarkerDragEnd(Marker marker);
		abstract void onMarkerDragStart(Marker marker);
	}

}
