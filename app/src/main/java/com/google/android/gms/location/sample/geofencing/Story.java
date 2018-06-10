package com.google.android.gms.location.sample.geofencing;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.mapzen.speakerbox.Speakerbox;

import java.util.concurrent.TimeUnit;

public class Story extends AppCompatActivity {
//    public static String lastStory = "last story";
    private Button mCloseButton;
    public static Speakerbox speakerbox;

    public static Story uniqueStory;
    public Button mPlayButton;
    public boolean enteredGeofence = false;//as opposed to clicked to get direction
    String preStory = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        uniqueStory = this;
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPlayButton = (Button) findViewById(R.id.play);
        speakerbox = new Speakerbox(getApplication());

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        System.err.println("here in story: "+MainActivity.lastEnteredGeofence);
        System.err.println("button: "+ mPlayButton);
        System.err.println(getParent());

        if (MainActivity.justEnteredGeofence){
            enteredGeofence = true;
            MainActivity.justEnteredGeofence = false;

            System.err.println("playing you have entered.");
//            speakerbox.play();
            preStory = "You have entered "+ MainActivity.lastEnteredGeofence.getRequestId()+". Please listen to the story.";


//            try {
//                TimeUnit.SECONDS.sleep(20);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Story.uniqueStory.mPlayButton.performClick();
        }

        String placeName = MainActivity.activeStoryStopName;
        String lastStory = Constants.id2Story.get(placeName);

        TextView textView = findViewById(R.id.storyText);
        textView.setText(lastStory);

        TextView textView1 = findViewById(R.id.storyName);
        textView1.setText(placeName);



//        mCloseButton = (Button) findViewById(R.id.closeStory);

    }



    public void playButtonHandler(View view){
        String placeName = MainActivity.activeStoryStopName;
        String lastStory = Constants.id2Story.get(placeName);
        System.err.println("Playing The actual story");

        if (speakerbox != null) {
            speakerbox.stop();
        }

        speakerbox.play(preStory+lastStory);
        preStory = "";

    }

    public void stopButtonHandler(View view){
        if (speakerbox != null) {
            speakerbox.stop();
        }
    }

    public void closeStoryButtonHandler(View view)
    {
        finish();
    }

    @Override
    public void onBackPressed(){
        if (speakerbox != null) {
            speakerbox.stop();
        }
        super.onBackPressed();
    }

    public void directionHandler(View view){
        if (enteredGeofence){
            MainActivity.nextStopTovisit = MapsActivityCurrentPlace.getNextStop();
        }
        else {
            MainActivity.nextStopTovisit = MainActivity.activeStoryStopName;
        }

        MainActivity.shouldCoverByMap = true;
        MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.getDeviceLocation();
        onBackPressed();
    }
}
