package com.sosalerts.shaurya.sosalerts.services.address;

import java.util.Date;

/**
 * Created by shaurya on 1/25/2017.
 */

public class SavedLocations {
    private String locationName;
    private String link;
    private Date longPressTime;
    public Date getLongPressTime() {
        return longPressTime;
    }

    public void setLongPressTime(Date longPressTime) {
        this.longPressTime = longPressTime;
    }



    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }



    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }
    @Override
    public String toString(){
        return (locationName != null ? locationName : "");
    }
}
