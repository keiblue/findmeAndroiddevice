package com.example.findmedevice;

public class DataExport {
    long id;
    String beaconUID;
    String longitude;
    String latitude;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
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
