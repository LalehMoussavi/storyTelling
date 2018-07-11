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

public class Story extends AppCompatActivity {
//    public static String lastStory = "last story";

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
        setContentView(R.layout.activity_scrolling);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPlayButton = (FloatingActionButton) findViewById(R.id.play);

//        floatingactionbutton fab = (floatingactionbutton) findviewbyid(r.id.fab) error
//        speakerbox = new Speakerbox(getApplication());

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

        mDirButton= findViewById(R.id.Direction);
        if (MainActivity.onlyShowMarkers){
            mDirButton.setEnabled(false);
            mDirButton.setAlpha(0.3f);
        }
        stopsImage = findViewById(R.id.stopImage);




//        if (MainActivity.justEnteredGeofence){
//            if (MainActivity.requestedStop!=null){
//                mDirButton.setText("RESUME THE ROUTING");
//            }
//            else{
//                mDirButton.setText("Go TO THE NEXT SIGHT");
//            }
//
//        }
//        else{
//            mDirButton.setText("GO TO THIS SIGHT");
//        }

        postStory = "";
        preStory = "";

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
                    if (nextIdx==thisIdx+1 || (nextIdx==Constants.id2Idx.size()-1 && nextIdx==thisIdx)){
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

//            try {
//                TimeUnit.SECONDS.sleep(20);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            Story.uniqueStory.mPlayButton.performClick();

        }

        String stopName = MainActivity.activeStoryStopName;
        String currentStory = getCurrentStory(stopName);

        Resources resources = getApplicationContext().getResources();
        int resourceId = resources.getIdentifier(stopName.toLowerCase().replace(" ","").replace("-","").replace("'",""), "drawable",
                getPackageName());
        stopsImage.setImageResource(resourceId);

        TextView textView = findViewById(R.id.storyText);
        textView.setText(currentStory);

        TextView textView1 = findViewById(R.id.storyName);
        textView1.setText(stopName);






    }

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


    public void playButtonHandler(View view){
        String stopName = MainActivity.activeStoryStopName;
        String currentStory = getCurrentStory(stopName);
//        String text = preStory+currentStory+postStory;
//        preStory = "";
//        postStory = "";
        MySpeakerBox.play(currentStory,true);
    }

    public void stopButtonHandler(View view){
        MySpeakerBox.stop();
    }

//    public void closeStoryButtonHandler(View view)
//    {
//        finish();
//    }

    @Override
    public void onBackPressed(){

        MySpeakerBox.stop();

        MainActivity.shouldCoverByMap = true;
        super.onBackPressed();
    }

    public void directionHandler(View view){
//        if (enteredGeofence){
//            MainActivity.nextStopTovisit = MapsActivityCurrentPlace.getNextStop();
//        }
//        else {
        if (!enteredGeofence){//This is when user asks explicitly to go to some stop.
            MainActivity.requestedStop = MainActivity.nextStopTovisit = MainActivity.activeStoryStopName;
        }

        MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.getDeviceLocation();
        onBackPressed();
    }


}
