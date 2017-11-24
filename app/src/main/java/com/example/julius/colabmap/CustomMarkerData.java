package com.example.julius.colabmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by julius on 22/11/2017.
 */

public class CustomMarkerData {
    public double lat;
    public double lon;
    public String description;
    public String photoURL;
    public String id;
    CustomMarkerData(double lat, double lon, String des, String pURL, String id){
        this.lat = lat;
        this.lon = lon;
        description = des;
        photoURL = pURL;
        this.id = id;
    }
    public CustomMarkerData(){

    }
}
