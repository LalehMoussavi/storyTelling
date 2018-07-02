package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
    public void onClick(View rootView) {

        switch (rootView.getId()) {
            case R.id.play:
                System.out.println("playing from tab1");
                MySpeakerBox.play("Edinburgh is the historical, cultural and political capital of Scotland and has a reputation as one of the world's most paranormal cities in the world. There is no better introduction to the city than a walk straight down the oldest and definitely the most haunted part of Edinburgh, called the royal mile. We will start at Edinburgh's hill topping castle and humble down to the palace. Let's have a quick history in the INTRODUCTION section to give you an overview of the place.");
            case R.id.stop:
                MySpeakerBox.stop();
                break;
        }

    }

}


