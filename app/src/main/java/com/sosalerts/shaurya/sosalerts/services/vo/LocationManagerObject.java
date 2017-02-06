package com.sosalerts.shaurya.sosalerts.services.vo;

import android.location.LocationListener;
import android.location.LocationManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by shaurya on 2/6/2017.
 */

public class LocationManagerObject {
    private LocationManager locManager;
    private List<LocationListener> listners = new ArrayList<LocationListener>();

    public List<LocationListener> getListners() {
        return listners;
    }

    public void setListners(List<LocationListener> listners) {
        this.listners = listners;
    }

    public LocationManager getLocManager() {
        return locManager;
    }

    public void setLocManager(LocationManager locManager) {
        this.locManager = locManager;
    }


}
