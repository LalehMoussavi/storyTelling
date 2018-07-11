package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

public class tab3 extends Fragment implements View.OnClickListener{

    View rootView;
    static tab3 uniqueTab3;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueTab3 = this;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab3, container, false);
        FloatingActionButton mapShow = (FloatingActionButton) rootView.findViewById(R.id.mapShow);
        FloatingActionButton startingDir = (FloatingActionButton) rootView.findViewById(R.id.startingDir);
        FloatingActionButton closestDir = (FloatingActionButton) rootView.findViewById(R.id.closestDir);
        FloatingActionButton resetTour = (FloatingActionButton) rootView.findViewById(R.id.resetTour);
        mapShow.setOnClickListener(this);
        startingDir.setOnClickListener(this);
        closestDir.setOnClickListener(this);
        resetTour.setOnClickListener(this);
        this.rootView = rootView;
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("switching tabs");
        MySpeakerBox.stop();

        if (MainActivity.seenStops.size()>0){
            //We're in the middle of a tour
            TextView textView  = rootView.findViewById(R.id.textView3);
            textView.setText("RESUME THE TOUR");
        }
        else{
            TextView textView  = rootView.findViewById(R.id.textView3);
            textView.setText("TAKE ME TO THE FIRST SITE");
        }

    }

    @Override
    public void onClick(View rootView) {

        MainActivity.firstTime = false;

        switch (rootView.getId()) {

            case R.id.mapShow:
                MainActivity.shouldCoverByMap = true;
                MainActivity.onlyShowMarkers = true;
                MainActivity.uniqueMainActivity.onStart();
                break;
            case R.id.startingDir:
                System.out.println("go to starting from tab3");
                MainActivity.shouldCoverByMap = true;
                MainActivity.goToClosetStop = false;
                MainActivity.requestedStop = null;
                MainActivity.onlyShowMarkers = false;
                MainActivity.uniqueMainActivity.onStart();
                break;
            case R.id.closestDir:
                System.out.println("go to closest side from tab3");
                MainActivity.shouldCoverByMap = true;
                MainActivity.goToClosetStop = true;
                MainActivity.onlyShowMarkers = false;
                MainActivity.uniqueMainActivity.onStart();
                break;
            case R.id.resetTour:

                System.out.println("reseting");
                MainActivity.reset();
                MapsActivityCurrentPlace.reset();
                this.onStart();
                break;
        }

    }

}
