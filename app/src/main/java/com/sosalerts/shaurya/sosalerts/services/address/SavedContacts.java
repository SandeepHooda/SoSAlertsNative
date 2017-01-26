package com.sosalerts.shaurya.sosalerts.services.address;

import java.util.Date;

/**
 * Created by shaurya on 1/26/2017.
 */

public class SavedContacts {
    private String contactNo ="";
    private String contactName = "";
    private Date longPressTime;

    public Date getLongPressTime() {
        return longPressTime;
    }

    public void setLongPressTime(Date longPressTime) {
        this.longPressTime = longPressTime;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        if(null != contactName){
            this.contactName = contactName;
        }

    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        if (null != contactNo){
            this.contactNo = contactNo;
        }

    }

    @Override
    public String toString(){
        if (contactName.length() > 15){
            contactName = contactName.substring(0,15);
            contactName = contactName.substring(0,contactName.lastIndexOf(" "));

        }
        return contactName +" "+ contactNo;
    }
}
