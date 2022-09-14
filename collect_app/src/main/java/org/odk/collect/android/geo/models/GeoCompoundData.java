package org.odk.collect.android.geo.models;

import org.odk.collect.android.R;
import org.odk.collect.android.geo.MapPoint;

import java.util.ArrayList;
import java.util.HashMap;

import timber.log.Timber;

public class GeoCompoundData {

    public ArrayList<MapPoint> points = new ArrayList<>();
    public HashMap<Integer, CompoundMarker> markers = new HashMap<>();

    public GeoCompoundData(String value) {
        if(value != null) {
            String components[] = value.split("#");
            for(int i = 0; i < components.length; i++) {
                if(components[i].startsWith("line:")) {
                    String lineComponents [] = components[i].split(":");
                    if(lineComponents.length > 1) {

                        String coords = lineComponents[1];
                        for (String vertex : (coords == null ? "" : coords).split(";")) {
                            String[] words = vertex.trim().split(" ");
                            if (words.length >= 2) {
                                double lat;
                                double lon;
                                double alt;
                                double sd;
                                try {
                                    lat = Double.parseDouble(words[0]);
                                    lon = Double.parseDouble(words[1]);
                                    alt = words.length > 2 ? Double.parseDouble(words[2]) : 0;
                                    sd = words.length > 3 ? Double.parseDouble(words[3]) : 0;
                                } catch (NumberFormatException e) {
                                    continue;
                                }
                                points.add(new MapPoint(lat, lon, alt, sd));
                            }
                        }
                    }

                } else if(components[i].startsWith("marker:")) {

                    CompoundMarker cm = new CompoundMarker();

                    String pointComponents [] = components[i].split(":");
                    String props;
                    if(pointComponents.length > 2) {
                        props = pointComponents[2];
                    } else {
                        props = pointComponents[1];		// old versions of webforms could miss out on the point coords
                    }
                    String pComp [] = props.split(";");
                    for(int k = 0; k < pComp.length; k++) {
                        String pe [] = pComp[k].split("=");
                        if(pe.length > 1) {
                            if(pe[0].trim().equals("index")) {
                                try {
                                    cm.index = Integer.valueOf(pe[1].trim());
                                } catch (Exception e) {
                                    Timber.e(e);
                                }
                            } else if(pe[0].trim().equals("type")) {
                                cm.type = pe[1].trim();
                            }
                        }
                    }
                    markers.put(cm.index, cm);
                }
            }
        }
    }

}
