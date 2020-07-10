package com.example.findmedevice.models;

public class DataExport {
    String androidId;
    String beaconUID;
    String longitude;
    String latitude;

    public String getAndroidId() {
        return androidId;
    }

    public void setAndroidId(String androidId) {
        this.androidId = androidId;
    }

    public String getBeaconUID() {
        return beaconUID;
    }

    public void setBeaconUID(String beaconUID) {
        this.beaconUID = beaconUID;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }
}
