package com.sosalerts.shaurya.sosalerts.tabs;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sosalerts.shaurya.sosalerts.R;

/**
 * Created by shaurya on 1/23/2017.
 */

public class TripsTab extends Fragment {
    private final String fileName = this.getClass().getName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.e(fileName, " now inside package " );
        return inflater.inflate(R.layout.trips_tab, container, false);
    }
}
