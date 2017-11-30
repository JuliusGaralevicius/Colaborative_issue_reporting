package com.example.julius.colabmap;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by julius on 22/11/2017.
 */

public class CustomIssueMarkerData {
    public double lat;
    public double lon;
    public String description;
    public String photoName;
    public String id;
    CustomIssueMarkerData(double lat, double lon, String des, String photoName, String id){
        this.lat = lat;
        this.lon = lon;
        description = des;
        this.photoName = photoName;
        this.id = id;
    }
    public CustomIssueMarkerData(){

    }
}
