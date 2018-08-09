package com.google.android.gms.location.sample.geofencing;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


import java.io.PrintWriter;
import java.io.StringWriter;

public class Story extends AppCompatActivity implements TTSFinshHandler{

    private FloatingActionButton mDirButton;
    public static Story uniqueStory;
    private ImageView stopsImage;
    public FloatingActionButton mPlayButton;
    public boolean enteredGeofence = false;//as opposed to clicked to get direction
    String preStory = "";//to be played before the story is started
    String postStory = "";//to be played after the story is finished

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueStory = this;
        setContentView(R.layout.story_activity);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPlayButton = (FloatingActionButton) findViewById(R.id.play);


        System.err.println("here in story: "+MainActivity.lastEnteredGeofence);
        System.err.println("button: "+ mPlayButton);
        System.err.println(getParent());

        mDirButton= findViewById(R.id.Direction);
        if (MainActivity.onlyShowMarkers){
            mDirButton.setEnabled(false);
            mDirButton.setAlpha(0.3f);
        }
        stopsImage = findViewById(R.id.stopImage);


        postStory = "";
        preStory = "";

       /*
        if just entered a geofence (and pressed the notification), or she's aleady in one stop
        and asks for story
         */
        if (MainActivity.justEnteredGeofence || (MainActivity.lastEnteredGeofence!=null && MainActivity.lastEnteredGeofence.getRequestId().equals(MainActivity.activeStoryStopName))){
            enteredGeofence = true;
            MainActivity.justEnteredGeofence = false;
            MainActivity.nextStopTovisit = MapsActivityCurrentPlace.getNextStop();

            System.err.println("playing you have entered.");
//            speakerbox.play();
            String stopName = MainActivity.lastEnteredGeofence.getRequestId();
            preStory = Constants.id2PreStory.get(stopName);
            try{
                if (Constants.id2Dir.containsKey(stopName)){
                    int nextIdx = Constants.id2Idx.get(MainActivity.nextStopTovisit);
                    int thisIdx = Constants.id2Idx.get(stopName);
                    if (nextIdx==(thisIdx+1) || (thisIdx==Constants.id2Idx.size()-1 && MainActivity.seenStops.size()>=.5*Constants.Ed_LANDMARKS.size())){
                        postStory = " "+ Constants.id2Dir.get(stopName);
                    }
                }
            }

            catch (Exception e){
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                e.printStackTrace(pw);
                String sStackTrace = sw.toString();
                if (Constants.debug){
                    Toast.makeText(getApplicationContext(), sStackTrace, Toast.LENGTH_SHORT).show();
                }

                e.printStackTrace();
            }


            Story.uniqueStory.mPlayButton.performClick();

        }

        String stopName = MainActivity.activeStoryStopName;
        String currentStory = getCurrentStory(stopName);

        //load (and set) the stop's image!
        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier(stopName.toLowerCase().replace(" ","").replace("-","").replace("'",""), "drawable",
                getPackageName());
        stopsImage.setImageResource(resourceId);

        //load (and set) the stop's story!
        TextView textView = findViewById(R.id.storyText);
        textView.setText(currentStory);

        //load (and set) the stop's name!
        TextView textView1 = findViewById(R.id.storyName);
        textView1.setText(stopName);



    }
    //gets the (full) story
    String getCurrentStory(String stopName){
        String currentStory = Constants.id2Story.get(stopName);

        if (currentStory==null){
            currentStory = "There is no story about this stop. Just enjoy!";
        }
        else{
            currentStory = preStory+currentStory+postStory;
        }
        return currentStory;
    }

    //plays the story
    public void playButtonHandler(View view){
        String stopName = MainActivity.activeStoryStopName;
        String currentStory = getCurrentStory(stopName);

        MyTTS.play(currentStory,true,this);
    }
    //stops the TTS
    public void stopButtonHandler(View view){
        MyTTS.stop();
    }


    @Override
    public void onBackPressed(){

        MyTTS.stop();

        MainActivity.shouldCoverByMap = true;
        super.onBackPressed();
    }

    public void directionHandler(View view){
        System.out.println("entered geo: "+enteredGeofence);
        if (!enteredGeofence){//This is when user asks explicitly to go to some stop.
            System.out.println("setting active story stop name");
            MainActivity.requestedStop = MainActivity.nextStopTovisit = MainActivity.activeStoryStopName;
        }

        MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.getDeviceLocation(true);
        //This is when the user has seen everything and the tour has finished!
        if (!MapsActivityCurrentPlace.shouldSuggestRoute()){
            System.out.println("setting first time to true");
            MainActivity.firstTime = true;
            finish();
            if (MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace!=null){
                MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.finish();
            }

        }
        else{//normal case: go back to map and show the direction
            onBackPressed();
        }

    }

    // closes the story page when TTS gets finished!
    @Override
    public void onTTSFinish() {
        if (!MapsActivityCurrentPlace.shouldSuggestRoute()){
            MainActivity.firstTime = true;
        }
        System.out.println("trying to finish story activity");
        finish();
    }
}
