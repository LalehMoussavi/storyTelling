package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


public class tab1 extends Fragment implements View.OnClickListener {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1, container, false);
        FloatingActionButton play = (FloatingActionButton) rootView.findViewById(R.id.play);
        FloatingActionButton stop = (FloatingActionButton) rootView.findViewById(R.id.stop);
        play.setOnClickListener(this);
        stop.setOnClickListener(this);
        return rootView;
        }

    @Override
    public void onStart() {
        super.onStart();
        System.out.println("switching tabs");
        MySpeakerBox.stop();
    }

    @Override
    public void onClick(View rootView) {

        switch (rootView.getId()) {
            case R.id.play:
                System.out.println("playing from tab1");
                MySpeakerBox.play("welcome to the haunted Edinburgh walk, a guide to the dark history and supernatural locations of the capital. The app is your tour guide, showing you eight sites in total. It works best if you follow in sequence, but it is entirely up to you. When you got close to each of these stops, the related story starts playing and as you leave you will get some guidance on how to find the next site. To have a more detailed information about the tour scroll down. To start the tour go to the TOUR tab and if you are wondering why there are so many ghost around, listen to the INTRODUCTION tab.",false);
            case R.id.stop:
                MySpeakerBox.stop();
                break;
        }

    }

}


