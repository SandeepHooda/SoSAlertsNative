package com.sosalerts.shaurya.sosalerts.services.locationTracker;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;

/**
 * Created by shaurya on 2/11/2017.
 */

public class LocationTrackerVO {
    private String locationName;
    private long entryTime;
    private long averageStayInMin;
    private int noOfSamples;
    public String toString(){
        return locationName+"_"+ entryTime +"_"+averageStayInMin+"_"+ noOfSamples;
    }
    public void fillInDetailsFromLocationStr(String location, LocationTrackerVO tracker){
        if(null == location) {
            location = "";
        }
        StringTokenizer tokenizer = new StringTokenizer(location, "_") ;
        List<String> locationTrackerElements = new ArrayList<String>();
        while(tokenizer.hasMoreTokens()){
            String element = tokenizer.nextToken();
            locationTrackerElements.add(element);
        }
        if(locationTrackerElements.size() > 0){
            tracker.setLocationName(locationTrackerElements.get(0));
        }
        if(locationTrackerElements.size() > 1){
            tracker.setEntryTime(Long.parseLong(locationTrackerElements.get(1)));
        }
        if(locationTrackerElements.size() > 2){
            tracker.setAverageStayInMin(Integer.parseInt(locationTrackerElements.get(2)));
        }
        if(locationTrackerElements.size() > 3){
            tracker.setNoOfSamples(Integer.parseInt(locationTrackerElements.get(3)));
        }

    }

    public void calculateAndSetAverageMinutesAtLocation(LocationTrackerVO location){
        long entryTime = location.getEntryTime();
        long currentStayInMinutes = ((new Date().getTime() - entryTime)/(1000*60));
        if(entryTime ==0){
           // return -1; //Invalid because no entry time
        }else if (currentStayInMinutes > 20*60) {
            //return -2; //Invalid because difference is more that 20 hours

        }else {
            //return stayInMinutes;
            long averageStayInMin = location.getAverageStayInMin();
            int noOfSamples = location.getNoOfSamples();
            location.setAverageStayInMin( ( (averageStayInMin*noOfSamples)+currentStayInMinutes) /(noOfSamples+1));
        }

    }
    public int getNoOfSamples() {
        return noOfSamples;
    }

    public void setNoOfSamples(int noOfSamples) {
        this.noOfSamples = noOfSamples;
    }

    public long getAverageStayInMin() {
        return averageStayInMin;
    }

    public void setAverageStayInMin(long averageStayInMin) {
        this.averageStayInMin = averageStayInMin;
    }

    public long getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(long entryTime) {
        this.entryTime = entryTime;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }


}
