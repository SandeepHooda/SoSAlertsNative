package com.sosalerts.shaurya.sosalerts.tabs;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by shaurya on 1/23/2017.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int mNumOfTabs;

    public PagerAdapter(FragmentManager fm, int NumOfTabs) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                ContactsTab contactsTab = new ContactsTab();
                return contactsTab;
            case 1:
                LocationsTab locationsTab = new LocationsTab();
                return locationsTab;
            case 2:
                SettingsTab triptsTab = new SettingsTab();
                return triptsTab;


            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
