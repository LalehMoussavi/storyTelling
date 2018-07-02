package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

public class tab3 extends Fragment implements View.OnClickListener{
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3, container, false);
        FloatingActionButton mapShow = (FloatingActionButton) rootView.findViewById(R.id.mapShow);
        FloatingActionButton startingDir = (FloatingActionButton) rootView.findViewById(R.id.startingDir);
        FloatingActionButton closestDir = (FloatingActionButton) rootView.findViewById(R.id.closestDir);
        mapShow.setOnClickListener(this);
        startingDir.setOnClickListener(this);
        closestDir.setOnClickListener(this);
        return rootView;
    }

    @Override
    public void onClick(View rootView) {

        MainActivity.firstTime = false;

        switch (rootView.getId()) {

            case R.id.mapShow:
                MainActivity.onlyShowMarkers = true;
                MainActivity.uniqueMainActivity.onStart();
                break;
            case R.id.startingDir:
                System.out.println("go to starting from tab3");
                MainActivity.goToClosetStop = false;
                MainActivity.requestedStop = null;
                MainActivity.onlyShowMarkers = false;
                MainActivity.uniqueMainActivity.onStart();
                break;
            case R.id.closestDir:
                System.out.println("go to closest side from tab3");
                MainActivity.goToClosetStop = true;
                MainActivity.onlyShowMarkers = false;
                MainActivity.uniqueMainActivity.onStart();
                break;
        }

    }
}
