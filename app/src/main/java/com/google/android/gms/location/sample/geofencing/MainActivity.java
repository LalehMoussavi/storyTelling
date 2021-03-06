/*
 * Copyright 2017 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.gms.location.sample.geofencing;

import android.Manifest;
import android.MySQLiteOpenHelper;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.speech.tts.TextToSpeech;


import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;

/**
 * Demonstrates how to create and remove geofences using the GeofencingApi. Uses an IntentService
 * to monitor geofence transitions and creates notifications whenever a device enters or exits
 * a geofence.
 * <p>
 * This sample requires a device's Location settings to be turned on. It also requires
 * the ACCESS_FINE_LOCATION permission, as specified in AndroidManifest.xml.
 * <p>
 */
public class MainActivity extends AppCompatActivity implements OnCompleteListener<Void> {

    private MySQLiteOpenHelper mySQLiteOpenHelper;
    public static SQLiteDatabase mdatabase;

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int REQUEST_PERMISSIONS_REQUEST_CODE = 34;

    public static MainActivity uniqueMainActivity;

    /**
     * Tracks whether the user requested to add or remove geofences, or to do neither.
     */
    private enum PendingGeofenceTask {
        ADD, REMOVE, NONE
    }

    /**
     * Provides access to the Geofencing API.
     */
    private GeofencingClient mGeofencingClient;

    /**
     * The list of geofences used in this sample.
     */
    private ArrayList<Geofence> mGeofenceList;

    /**
     * Used when requesting to add or remove geofences.
     */
    private PendingIntent mGeofencePendingIntent;

    // Buttons for kicking off the process of adding or removing geofences.
    private Button mAddGeofencesButton;
    private Button mRemoveGeofencesButton;
//    private static Button showStoriesButton;
//    private static Button ignoreStoriesButton;

    public static Geofence lastEnteredGeofence = null;
    public static Geofence lastExitedGeofence = null;
    public static String activeStoryStopName;
    public static String nextStopTovisit;
    public static String requestedStop = null;//This is what user has asked explicitly. Has priority over other stops.
    public static AssetManager am;
    public static boolean shouldCoverByMap = true;
    public static boolean goToClosetStop = false;//false: first stop, true: closest stop
    public static boolean onlyShowMarkers = false; //true: geofences & direction stops working (0n tab3)

    public static HashSet<String> seenStops = new HashSet<String>();
    public static boolean justEnteredGeofence;

    private PendingGeofenceTask mPendingGeofenceTask = PendingGeofenceTask.NONE;
    Handler handler;

    public static boolean firstTime = true;

    static void reset(){
        if (MainActivity.uniqueMainActivity!=null){
            MainActivity.uniqueMainActivity.mRemoveGeofencesButton.performClick();
        }
        lastEnteredGeofence = null;
        lastExitedGeofence = null;
        activeStoryStopName = null;
        nextStopTovisit = MapsActivityCurrentPlace.getNextStop();;
        requestedStop = null;//This is what user has asked explicitly. Has priority over other stops.
        shouldCoverByMap = true;
        goToClosetStop = false;//false: first stop, true: closest stop
        onlyShowMarkers = false; //true: geofences & direction stops working (0n tab3)

        seenStops = new HashSet<String>();
        justEnteredGeofence = false;
        firstTime = true;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (MainActivity.uniqueMainActivity!=null){
            MainActivity.uniqueMainActivity.mRemoveGeofencesButton.performClick();
        }

        if (getIntent().getBooleanExtra("EXIT", false)) {
            if (handler!=null){
                handler.removeCallbacksAndMessages(null);
            }
            finish();
            System.exit(0);
        }

        uniqueMainActivity = this;
        setContentView(R.layout.main_activity);
        am = getAssets();

        // Get the UI widgets.
        mAddGeofencesButton = (Button) findViewById(R.id.add_geofences_button);
        mRemoveGeofencesButton = (Button) findViewById(R.id.remove_geofences_button);
//        mAddGeofencesButton.setVisibility(View.GONE);
//        mRemoveGeofencesButton.setVisibility(View.GONE);//In future, just remove these two totally

//        showStoriesButton = (Button) findViewById(R.id.showStory);
//        showStoriesButton.setVisibility(View.INVISIBLE);

//        ignoreStoriesButton = (Button) findViewById(R.id.ignoreStory);
//        ignoreStoriesButton.setVisibility(View.INVISIBLE);


//        playStoryButton = (Button)findViewById((R.id.playStory));

        // Empty list for storing geofences.
        mGeofenceList = new ArrayList<>();

        // Initially set the PendingIntent used in addGeofences() and removeGeofences() to null.
        mGeofencePendingIntent = null;

        setButtonsEnabledState();


        // Get the geofences used. Geofence data is hard coded in this sample.
        populateGeofenceList();
//        addGeofences();

       mGeofencingClient = LocationServices.getGeofencingClient(this);

       //ADDED BY LALEH FOR DB

        mySQLiteOpenHelper = new MySQLiteOpenHelper(getApplicationContext());
        mdatabase = mySQLiteOpenHelper.getReadableDatabase();
        Cursor cursor =  mdatabase.rawQuery("select * from stops;", null);
        System.out.println("number of columns: "+ cursor.getColumnCount());
        //TODO: ADDED BY ME!
//        MainActivity.nextStopTovisit = "";


        // ADDED BY LALEH FOR DB
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!checkPermissions()) {
            requestPermissions();
        }
        else {
            performPendingGeofenceTask();
        }



//        tts=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                String text = "Any Text to Speak";
////                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    tts.speak(text,TextToSpeech.QUEUE_FLUSH,null);
////                } else {
////                    ts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
////                }
//
//            }
//        });
//
//        tts.speak("testing", TextToSpeech.QUEUE_FLUSH, null);

        //Added by Laleh


//        toastText(getApplicationContext(),"Hello World");

//        if (lastEnteredGeofence!=null){
//            handleEnteringDataZone(lastEnteredGeofence);
//        }



        //Added by Laleh

        System.err.println("status: " + firstTime + " "+shouldCoverByMap+" "+lastEnteredGeofence);

        if (firstTime){
            firstTime = false;//TODO: change
            System.out.println("first time changed to: "+firstTime);

            if (LandingPage.uniqueLandingPage !=null){
                LandingPage.uniqueLandingPage.finish();
            }


            Intent intent = new Intent();
            intent.setClass(this, LandingPage.class);
            startActivity(intent);

        }

        else if (shouldCoverByMap && onlyShowMarkers){

            if (MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace!=null){
                MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.finish();
            }


            Intent intent = new Intent();
            intent.setClass(this,MapsActivityCurrentPlace.class);
            startActivity(intent);
        }

        //Now, let's start the Map

        //only call map activity if shouldCoverByMap=true


        else if (shouldCoverByMap && !onlyShowMarkers){//THIS IS THE MAP
            MainActivity.nextStopTovisit = MapsActivityCurrentPlace.getNextStop();

            if (MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace!=null){
                MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.finish();
            }

            mAddGeofencesButton.performClick();
            mAddGeofencesButton.setVisibility(View.GONE);
            mRemoveGeofencesButton.setVisibility(View.GONE);//In future, just remove these two totally
            System.err.println("starting map!+ "+shouldCoverByMap);
            Intent intent = new Intent();
            intent.setClass(this,MapsActivityCurrentPlace.class);
            startActivity(intent);
        }
        else if (lastEnteredGeofence!=null){//THIS IS THE STORY.this means we have entered in geofence
            mAddGeofencesButton.performClick();
            mAddGeofencesButton.setVisibility(View.GONE);
            mRemoveGeofencesButton.setVisibility(View.GONE);//In future, just remove these two totally
            activeStoryStopName = lastEnteredGeofence.getRequestId();
            shouldCoverByMap = true;
            if (Story.uniqueStory!=null){
                MyTTS.stop();
                Story.uniqueStory.finish();
            }
            //TODO: wait 10 seconds
            justEnteredGeofence = true;
            showStory(this);

        }
//        else{
//
//        }
//      else {
//                Intent intent = new Intent();
//                intent.setClass(this,Story.class);
//                startActivity(intent);
//                showStoriesButton.setVisibility(View.INVISIBLE);
//                ignoreStoriesButton.setVisibility(View.INVISIBLE);
//                lastEnteredGeofence = null;//alan
//        }

        if (handler!=null){
            handler.removeCallbacksAndMessages(null);
        }

//        handler = new Handler();
//
//        final int delay = 10000; //milliseconds
//
//        handler.postDelayed(new Runnable(){
//            public void run(){
//                //do something
//                handler.postDelayed(this, delay);
//                System.out.println("every" + delay +" milliseconds");
//                if (MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace!=null){
//                    try {
//                        MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.getDeviceLocation(false);
//                    }
//                    catch (Exception e){
//                        if (Constants.debug){
//                            Toast.makeText(getApplicationContext(), "exception in get recurring device location", Toast.LENGTH_SHORT).show();
//                        }
//                    }
//
//                }
//            }
//        }, delay);


    }

    static void toastText(Context context, CharSequence text){
        int duration = Toast.LENGTH_SHORT;
        if (Constants.debug){
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }

    }

    /**
     * Builds and returns a GeofencingRequest. Specifies the list of geofences to be monitored.
     * Also specifies how the geofence notifications are initially triggered.
     */
    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();

        // The INITIAL_TRIGGER_ENTER flag indicates that geofencing service should trigger a
        // GEOFENCE_TRANSITION_ENTER notification when the geofence is added and if the device
        // is already inside that geofence.
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);


        // Add the geofences to be monitored by geofencing service.
        builder.addGeofences(mGeofenceList);

        // Return a GeofencingRequest.
        return builder.build();
    }

    /**
     * Adds geofences, which sets alerts to be notified when the device enters or exits one of the
     * specified geofences. Handles the success or failure results returned by addGeofences().
     */
    public void addGeofencesButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.ADD;
            requestPermissions();
            return;
        }
        addGeofences();
    }

    /**
     * Adds geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void addGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnCompleteListener(this);
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void removeGeofencesButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        removeGeofences();
    }

    /**
     * Removes geofences, which stops further notifications when the device enters or exits
     * previously registered geofences.
     */
    public void showStoryButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        showStory(this);
//        MyTTS.play(this);
    }

    public void ignoreStoryButtonHandler(View view) {
        if (!checkPermissions()) {
            mPendingGeofenceTask = PendingGeofenceTask.REMOVE;
            requestPermissions();
            return;
        }
        ignoreStory();
    }

//    public static void handleEnteringDataZone(Geofence geofence){
//        String story = Constants.id2Story.get(geofence.getRequestId());
//        Story.lastStory = story;
////        showStoriesButton.setVisibility(View.VISIBLE);
////        ignoreStoriesButton.setVisibility(View.VISIBLE);
//    }

    public static void showStory(Context parentActivity){
        System.err.println("inside show stories");
        Intent intent = new Intent();
        intent.setClass(parentActivity,Story.class);
        parentActivity.startActivity(intent);

//        Speakerbox speakerbox = new Speakerbox(MainActivity.this);
//        speakerbox.play("Hi");

//        showStoriesButton.setVisibility(View.INVISIBLE);
//        ignoreStoriesButton.setVisibility(View.INVISIBLE);
//        lastEnteredGeofence = null;
//        shouldCoverByMap = true;   //alan

    }

    private void ignoreStory(){
//        showStoriesButton.setVisibility(View.INVISIBLE);
//        ignoreStoriesButton.setVisibility(View.INVISIBLE);
//        lastEnteredGeofence = null;
        shouldCoverByMap = true;
        onStart();
    }

    /**
     * Removes geofences. This method should be called after the user has granted the location
     * permission.
     */
    @SuppressWarnings("MissingPermission")
    private void removeGeofences() {
        if (!checkPermissions()) {
            showSnackbar(getString(R.string.insufficient_permissions));
            return;
        }

        mGeofencingClient.removeGeofences(getGeofencePendingIntent()).addOnCompleteListener(this);
    }

    /**
     * Runs when the result of calling {@link #addGeofences()} and/or {@link #removeGeofences()}
     * is available.
     * @param task the resulting Task, containing either a result or error.
     */
    @Override
    public void onComplete(@NonNull Task<Void> task) {
        mPendingGeofenceTask = PendingGeofenceTask.NONE;
        if (task.isSuccessful()) {
            updateGeofencesAdded(!getGeofencesAdded());
            setButtonsEnabledState();

            int messageId = getGeofencesAdded() ? R.string.geofences_added :
                    R.string.geofences_removed;
            if (Constants.debug){
                Toast.makeText(this, getString(messageId), Toast.LENGTH_SHORT).show();
            }

        } else {
            // Get the status code for the error and log it using a user-friendly message.
            String errorMessage = GeofenceErrorMessages.getErrorString(this, task.getException());
            Log.w(TAG, errorMessage);
        }
    }

    /**
     * Gets a PendingIntent to send with the request to add or remove Geofences. Location Services
     * issues the Intent inside this PendingIntent whenever a geofence transition occurs for the
     * current list of geofences.
     *
     * @return A PendingIntent for the IntentService that handles geofence transitions.
     */
    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (mGeofencePendingIntent != null) {
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(this, GeofenceBroadcastReceiver.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when calling
        // addGeofences() and removeGeofences().
        mGeofencePendingIntent = PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mGeofencePendingIntent;
    }

    /**
     * This sample hard codes geofence data. A real app might dynamically create geofences based on
     * the user's location.
     */
    private void populateGeofenceList() {
        for (Map.Entry<String, LatLng> entry : Constants.Ed_LANDMARKS.entrySet()) {

            mGeofenceList.add(new Geofence.Builder()
                    // Set the request ID of the geofence. This is a string to identify this
                    // geofence.
                    .setRequestId(entry.getKey())

                    // Set the circular region of this geofence.
                    .setCircularRegion(
                            entry.getValue().latitude,
                            entry.getValue().longitude,
                            Constants.GEOFENCE_RADIUS_IN_METERS
                    )

                    // Set the expiration duration of the geofence. This geofence gets automatically
                    // removed after this period of time.
                    .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)

                    // Set the transition types of interest. Alerts are only generated for these
                    // transition. We track entry and exit transitions in this sample.
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                            Geofence.GEOFENCE_TRANSITION_EXIT)

                    // Create the geofence.
                    .build());
        }
    }

    /**
     * Ensures that only one button is enabled at any time. The Add Geofences button is enabled
     * if the user hasn't yet added geofences. The Remove Geofences button is enabled if the
     * user has added geofences.
     */
    private void setButtonsEnabledState() {
        if (getGeofencesAdded()) {
            mAddGeofencesButton.setEnabled(false);
            mRemoveGeofencesButton.setEnabled(true);
        } else {
            mAddGeofencesButton.setEnabled(true);
            mRemoveGeofencesButton.setEnabled(false);
        }
    }

    /**
     * Shows a {@link Snackbar} using {@code text}.
     *
     * @param text The Snackbar text.
     */
    private void showSnackbar(final String text) {
        View container = findViewById(android.R.id.content);
        if (container != null) {
            Snackbar.make(container, text, Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Shows a {@link Snackbar}.
     *
     * @param mainTextStringId The id for the string resource for the Snackbar text.
     * @param actionStringId   The text of the action item.
     * @param listener         The listener associated with the Snackbar action.
     */
    private void showSnackbar(final int mainTextStringId, final int actionStringId,
                              View.OnClickListener listener) {
        Snackbar.make(
                findViewById(android.R.id.content),
                getString(mainTextStringId),
                Snackbar.LENGTH_INDEFINITE)
                .setAction(getString(actionStringId), listener).show();
    }

    /**
     * Returns true if geofences were added, otherwise false.
     */
    private boolean getGeofencesAdded() {
        return PreferenceManager.getDefaultSharedPreferences(this).getBoolean(
                Constants.GEOFENCES_ADDED_KEY, false);
    }

    /**
     * Stores whether geofences were added ore removed in {@link SharedPreferences};
     *
     * @param added Whether geofences were added or removed.
     */
    private void updateGeofencesAdded(boolean added) {
        PreferenceManager.getDefaultSharedPreferences(this)
                .edit()
                .putBoolean(Constants.GEOFENCES_ADDED_KEY, added)
                .apply();
    }

    /**
     * Performs the geofencing task that was pending until location permission was granted.
     */
    private void performPendingGeofenceTask() {
        if (mPendingGeofenceTask == PendingGeofenceTask.ADD) {
            addGeofences();
        } else if (mPendingGeofenceTask == PendingGeofenceTask.REMOVE) {
            removeGeofences();
        }
    }

    /**
     * Return the current state of the permissions needed.
     */
    private boolean checkPermissions() {
        int permissionState = ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        return permissionState == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        boolean shouldProvideRationale =
                ActivityCompat.shouldShowRequestPermissionRationale(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);

        // Provide an additional rationale to the user. This would happen if the user denied the
        // request previously, but didn't check the "Don't ask again" checkbox.
        if (shouldProvideRationale) {
            Log.i(TAG, "Displaying permission rationale to provide additional context.");
            showSnackbar(R.string.permission_rationale, android.R.string.ok,
                    new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            // Request permission
                            ActivityCompat.requestPermissions(MainActivity.this,
                                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_PERMISSIONS_REQUEST_CODE);
                        }
                    });
        } else {
            Log.i(TAG, "Requesting permission");
            // Request permission. It's possible this can be auto answered if device policy
            // sets the permission in a given state or the user denied the permission
            // previously and checked "Never ask again".
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_PERMISSIONS_REQUEST_CODE);
        }
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        Log.i(TAG, "onRequestPermissionResult");
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            if (grantResults.length <= 0) {
                // If user interaction was interrupted, the permission request is cancelled and you
                // receive empty arrays.
                Log.i(TAG, "User interaction was cancelled.");
            } else if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "Permission granted.");
                performPendingGeofenceTask();
            } else {
                // Permission denied.

                // Notify the user via a SnackBar that they have rejected a core permission for the
                // app, which makes the Activity useless. In a real app, core permissions would
                // typically be best requested during a welcome-screen flow.

                // Additionally, it is important to remember that a permission might have been
                // rejected without asking the user for permission (device policy or "Never ask
                // again" prompts). Therefore, a user interface affordance is typically implemented
                // when permissions are denied. Otherwise, your app could appear unresponsive to
                // touches or interactions which have required permissions.
                showSnackbar(R.string.permission_denied_explanation, R.string.settings,
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                // Build intent that displays the App settings screen.
                                Intent intent = new Intent();
                                intent.setAction(
                                        Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                Uri uri = Uri.fromParts("package",
                                        BuildConfig.APPLICATION_ID, null);
                                intent.setData(uri);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        });
                mPendingGeofenceTask = PendingGeofenceTask.NONE;
            }
        }
    }


}


