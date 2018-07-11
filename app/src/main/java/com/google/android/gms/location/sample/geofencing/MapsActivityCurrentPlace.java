package com.google.android.gms.location.sample.geofencing;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;


import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import com.google.maps.model.DirectionsResult;
//import com.mapzen.speakerbox.Speakerbox;

/**
 * An activity that displays a map showing the place at the device's current location.
 */
public class MapsActivityCurrentPlace extends AppCompatActivity
        implements OnMapReadyCallback ,RoutingListener, GoogleMap.OnMarkerClickListener{

    private static final String TAG = MapsActivityCurrentPlace.class.getSimpleName();
    private GoogleMap mMap;
    private CameraPosition mCameraPosition;

    // The entry points to the Places API.
    private GeoDataClient mGeoDataClient;
    private PlaceDetectionClient mPlaceDetectionClient;

    // The entry point to the Fused Location Provider.
    private FusedLocationProviderClient mFusedLocationProviderClient;

    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng mDefaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean mLocationPermissionGranted;

    // The geographical location where the device is currently located. That is, the last-known
    // location retrieved by the Fused Location Provider.
    private Location mLastKnownLocation;

    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";

    // Used for selecting the current place.
    private static final int M_MAX_ENTRIES = 5;
    private String[] mLikelyPlaceNames;
    private String[] mLikelyPlaceAddresses;
    private String[] mLikelyPlaceAttributions;
    private LatLng[] mLikelyPlaceLatLngs;
    private List<Polyline> polylines = new ArrayList<>();
    public static MapsActivityCurrentPlace uniqueMapsActivityCurrentPlace;
    public static String lastReroutedStop = "";

    List<PolylineOptions> allPolyOptions = new ArrayList<>();
    List<Double> distancesToRoute = new ArrayList<>();

    private static final int[] COLORS = new int[]{R.color.primary_dark,R.color.primary,R.color.primary_light,R.color.accent,R.color.primary_dark_material_light};
//    static boolean firstRoute = true;

    static void reset(){
        lastReroutedStop = "";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace = this;
        polylines = new ArrayList<>();
        if (Constants.debug){
            MainActivity.toastText(getApplicationContext(),"Hello from MapsActi");
        }


//         Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            mLastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            mCameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Retrieve the content view that renders the map.
        setContentView(R.layout.activity_maps);

        // Construct a GeoDataClient.
        mGeoDataClient = Places.getGeoDataClient(this, null);

        // Construct a PlaceDetectionClient.
        mPlaceDetectionClient = Places.getPlaceDetectionClient(this, null);

        // Construct a FusedLocationProviderClient.
        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        // Build the map.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        setLocationManager();

    }

    void setLocationManager(){

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        try {
//            Toast.makeText(getApplicationContext(), "set loc manager",  Toast.LENGTH_SHORT).show();

            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location){
                    try{
                        //                    Toast.makeText(getApplicationContext(), "in get location changed",  Toast.LENGTH_SHORT).show();
                        if (polylines.size()>0) {
                            List<LatLng> latLongs = getLatLongsFromPolyLine(polylines.get(0));
                            double distanceFromPolyLine = bdccGeoDistanceAlgorithm.my_bdccGeoDistanceFromPolyLine(latLongs, new LatLng(location.getLatitude(), location.getLongitude()));
                            if (Constants.debug){
                                Toast.makeText(getApplicationContext(), "this distance"+" "+distanceFromPolyLine +" meters",  Toast.LENGTH_SHORT).show();
                            }


                            distancesToRoute.add(distanceFromPolyLine);

                            if (distancesToRoute.size() < Constants.NUMDIST){
                                return;
                            }

//                            double averageDistanceToRoute;
                            double minDistance = 100000 ;

                            //now it's time to get the average
                            for (double distance: distancesToRoute) {
                                if (distance<minDistance){
                                    minDistance = distance;
                                }
//                                sumOfDistanceToRoute = (sumOfDistanceToRoute + distance);
                            }

//                            averageDistanceToRoute = sumOfDistanceToRoute / Constants.NUMDIST;

                            distancesToRoute = new ArrayList<>();

                            int mm = (int) minDistance;
                            if (Constants.debug){
                                Toast.makeText(getApplicationContext(), "Min distance"+" "+mm +" meters",  Toast.LENGTH_SHORT).show();
                            }


                            if (minDistance > 120){

                                if (!lastReroutedStop.equals(MainActivity.nextStopTovisit)){

                                    lastReroutedStop = MainActivity.nextStopTovisit;
                                    if (Constants.debug){
                                        Toast.makeText(getApplicationContext(), "new route"+" "+mm +" meters" +" num points: "+ latLongs.size(),  Toast.LENGTH_SHORT).show();
                                    }

//                                    getRouteToMarker( new LatLng(location.getLatitude(), location.getLongitude()),Constants.Ed_LANDMARKS.get(MainActivity.nextStopTovisit));
                                    getDeviceLocation();
//                                    speakerbox = new Speakerbox(getApplication());

                                    MySpeakerBox.play("Wrong direction. Please check the map again.",false);
                                }
                                else{
                                    if (Constants.debug){
                                        Toast.makeText(getApplicationContext(), "already rerouted",  Toast.LENGTH_SHORT).show();
                                    }

                                }
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
                }
                @Override
                public void onProviderDisabled(String provider) {
//                    Toast.makeText(getApplicationContext(), "disabled",  Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated method stub
                }
                @Override
                public void onProviderEnabled(String provider) {
//                    Toast.makeText(getApplicationContext(), "enabled",  Toast.LENGTH_SHORT).show();
                    // TODO Auto-generated method stub
                }
                @Override
                public void onStatusChanged(String provider, int status,
                                            Bundle extras) {
//                    Toast.makeText(getApplicationContext(), "status changed",  Toast.LENGTH_SHORT).show();
                }
            };

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener);

            //TODO: maybe change it to smaller value
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 7000, 10, locationListener);

//            Toast.makeText(getApplicationContext(), "loc manager successfully set",  Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            if (Constants.debug){
                Toast.makeText(getApplicationContext(), "exception",  Toast.LENGTH_SHORT).show();
            }

        }

    }



    @Override
    public void onStart() {
        super.onStart();
    }

    //TODO: add int id as the third argument

    public void pinAllMarkers () {
        int i = 0;
        for (String placeName: Constants.Ed_LANDMARKS.keySet()){
            System.err.println("pinng: " + placeName );
            pinMarker(Constants.Ed_LANDMARKS.get(placeName), placeName, i);
            i++;
        }

    }



    public void pinMarker (LatLng latlong, String placeName, int i) {

        Marker marker = mMap.addMarker(new MarkerOptions()
                        .title(placeName)
                        .position(latlong));

        if (MainActivity.seenStops.contains(placeName) && ! MainActivity.onlyShowMarkers) {

            marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.seenStop2Sign.get(i)));
        } else {

            marker.setIcon(BitmapDescriptorFactory.fromResource(Constants.unSeenStop2Sign.get(i)));
        }
//                        .alpha(0.2f)
//        MarkerOptions options = new MarkerOptions().position(latlong).title(placeName);
//        mMap.addMarker(options);
    }


    private void getRouteToMarker(LatLng latLng1, LatLng latLng2) {
        if (MainActivity.onlyShowMarkers){
            return;
        }
        else if (mLastKnownLocation!=null){
            distancesToRoute = new ArrayList<>();
            Routing routing = new Routing.Builder()
                    .travelMode(AbstractRouting.TravelMode.WALKING)
                    .withListener(this)
                    .alternativeRoutes(false)
                    .waypoints( latLng1, latLng2)
                    .key("AIzaSyDSruS20vubRU2r4hW0IGeWZ7zYO-8YEMU")
                    .build();

            routing.execute();
        }

    }

    boolean shouldStoreRoutes = false;

    private void drawTourroute() {
        System.err.println("Inside draw tour route");

//        File f = new File("/data/user/0/com.google.android.gms.location.sample.geofencing/files/Ghost_Tour.poly");//TODO: fix this for multi-routes
        if (true){

            System.err.println("Tour Routing file does not exist");

            ArrayList<LatLng> latLngs = new ArrayList<>();
            for (LatLng latLng : Constants.Ed_LANDMARKS.values()){
                latLngs.add(latLng);
            }

            for (int i =0; i<latLngs.size()-1; i++){
                shouldStoreRoutes = i==latLngs.size()-2;
                getRouteToMarker(latLngs.get(i),latLngs.get(i+1));
            }


        }
        else {
            try {

                System.err.println("Loading Tour Routing File");

                allPolyOptions = serializeDataIn();
                System.err.println("num routes: "+allPolyOptions.size());
                for (PolylineOptions polyOptions:allPolyOptions){
                    Polyline polyline = mMap.addPolyline(polyOptions);
                    polylines.add(polyline);
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }


        }



       //Now allPolyOptions is ready




    }





    void findPath(){


        LatLng start = new LatLng(55.949873, -3.190470);
        LatLng waypoint= new LatLng(55.950202, -3.187189);
        LatLng end = new LatLng(55.951551, -3.179339);

        Routing routing = new Routing.Builder()
                .travelMode(Routing.TravelMode.WALKING)
                .withListener(this)
                .waypoints(start, waypoint, end)
                .build();
        routing.execute();

    }




    //same as above
    private String getEndLocationTitle(DirectionsResult results){
        return  "Time :"+ results.routes[0].legs[0].duration.humanReadable +
                " Distance :" + results.routes[0].legs[0].distance.humanReadable;
    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mMap != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, mMap.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, mLastKnownLocation);
            super.onSaveInstanceState(outState);
        }
    }

    /**
     * Sets up the options menu.
     * @param menu The options menu.
     * @return Boolean.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.current_place_menu, menu);
        return true;
    }


//    /**
//     * Handles a click on the menu option to get a place.
//     * @param item The menu item to handle.
//     * @return Boolean.
//     */
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (item.getItemId() == R.id.option_get_place) {
//            showCurrentPlace();
//        }
//        return true;
//    }

    /**
     * Manipulates the map when it's available.
     * This callback is triggered when the map is ready to be used.
     */
    @Override
    public void onMapReady(GoogleMap map) {
        mMap = map;
        mMap.setOnMarkerClickListener(this);
        if (Constants.debug){
            Toast.makeText(getApplicationContext(), "Inside onMapReady",  Toast.LENGTH_SHORT).show();
        }


        final Context thisMapActivity = this;

        // Use a custom info window adapter to handle multiple lines of text in the
        // info window contents.
        mMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            // Return null here, so that getInfoContents() is called next.
            public View getInfoWindow(Marker arg0) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                // Inflate the layouts for the info window, title and snippet.
                View infoWindow = getLayoutInflater().inflate(R.layout.custom_info_contents,
                        (FrameLayout) findViewById(R.id.map), false);

                TextView title = ((TextView) infoWindow.findViewById(R.id.title));
                title.setText(marker.getTitle());

                TextView snippet = ((TextView) infoWindow.findViewById(R.id.snippet));
                snippet.setText(marker.getSnippet());



                mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                    public void onInfoWindowClick(Marker marker)
                    {
                        MainActivity.activeStoryStopName = marker.getTitle();
                        MainActivity.showStory(thisMapActivity);
                    }
                });


                return infoWindow;
            }

        });

        // Prompt the user for permission.
        getLocationPermission();

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();

        // Get the current location of the device and set the position of the map.
        erasePolylines ();
//        if (System.currentTimeMillis()-lastUpdatedTime>2000){
        getDeviceLocation();
//            lastUpdatedTime = System.currentTimeMillis();
//        }
//        else{
//            Toast.makeText(getApplicationContext(), "not enough time between re-rerouting!" , Toast.LENGTH_SHORT).show();
//        }

        pinAllMarkers();

        System.err.println("last loc: "+mLastKnownLocation);

//      findPath();

        //
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
    public void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (mLocationPermissionGranted) {
                Task<Location> locationResult = mFusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {

                        if (task.isSuccessful()) {
                            if (Constants.debug){
                                Toast.makeText(getApplicationContext(), "in getDeviceLocation re-reoute" , Toast.LENGTH_SHORT).show();
                            }

                            // Set the map's camera position to the current location of the device.
                            mLastKnownLocation = task.getResult();
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                    new LatLng(mLastKnownLocation.getLatitude(),
                                            mLastKnownLocation.getLongitude()), DEFAULT_ZOOM));
//                            getRouteToMarker(new LatLng(55.948583, -3.199919));

                            if (MainActivity.goToClosetStop){
                                System.out.println("go to closest side");
                                MainActivity.requestedStop =  MapsActivityCurrentPlace.uniqueMapsActivityCurrentPlace.findClosestStop();
                                MainActivity.goToClosetStop = false;
                                MainActivity.nextStopTovisit = MapsActivityCurrentPlace.getNextStop();
                            }

                            String placeName =  MainActivity.nextStopTovisit;
                            LatLng firstToVisit =  Constants.Ed_LANDMARKS.get(placeName);

                            System.err.println("get Device Location!");
//                            firstRoute = true;

                            getRouteToMarker(new LatLng(mLastKnownLocation.getLatitude(), mLastKnownLocation.getLongitude()),firstToVisit);



//                            try {
//                                TimeUnit.SECONDS.sleep(1);
//                            } catch (InterruptedException e) {
//                                e.printStackTrace();
//                            }

                            //drawTourroute();



                        } else {

                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            mMap.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(mDefaultLocation, DEFAULT_ZOOM));
                            mMap.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }


    // added by Laleh
    public static String getNextStop(){

        if (MainActivity.requestedStop!=null){
            return MainActivity.requestedStop;
        }

        String nextStopToVisit = "";
        int idx = -1;
        if (MainActivity.lastEnteredGeofence != null){
            String lastVisitedStop = MainActivity.lastEnteredGeofence.getRequestId();
            idx = Constants.id2Idx.get(lastVisitedStop);
        }

        while ((idx + 1)<Constants.id2Idx.size()){
            if (!MainActivity.seenStops.contains(Constants.ids.get(idx+1))){
//                nextStopToVisit = Constants.ids.get(idx+1);
                break;
            }
            else{
                idx++;
            }
        }

        idx++;

        if (idx==Constants.id2Idx.size()){
            idx --;
        }

        nextStopToVisit = Constants.ids.get(idx);//The last stop

        return nextStopToVisit;

//        boolean seenLastVisited = false;
//        for (String stopName: Constants.Ed_LANDMARKS.keySet()){
//            nextStopToVisit = stopName;
//
//            if (MainActivity.lastEnteredGeofence == null){
//                break;
//            }
//
//            if (seenLastVisited){
//                break;
//            }
//            //TODO: Added by me
//            if ( stopName.equals(MainActivity.lastEnteredGeofence.getRequestId())) {
//                seenLastVisited = true;
//            }
//        }


//        return nextStopToVisit;
    }
    /**
     * Prompts the user for permission to use the device location.
     */
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    /**
     * Handles the result of the request for location permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        mLocationPermissionGranted = false;
        switch (requestCode) {
            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    mLocationPermissionGranted = true;
                }
            }
        }
        updateLocationUI();
    }

    /**
     * Prompts the user to select the current place from a list of likely places, and shows the
     * current place on the map - provided the user has granted location permission.
     */
    private void showCurrentPlace() {
        if (mMap == null) {
            return;
        }

        if (mLocationPermissionGranted) {
            // Get the likely places - that is, the businesses and other points of interest that
            // are the best match for the device's current location.
            @SuppressWarnings("MissingPermission") final Task<PlaceLikelihoodBufferResponse> placeResult =
                    mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnCompleteListener
                    (new OnCompleteListener<PlaceLikelihoodBufferResponse>() {
                        @Override
                        public void onComplete(@NonNull Task<PlaceLikelihoodBufferResponse> task) {
                            if (task.isSuccessful() && task.getResult() != null) {
                                PlaceLikelihoodBufferResponse likelyPlaces = task.getResult();

                                // Set the count, handling cases where less than 5 entries are returned.
                                int count;
                                if (likelyPlaces.getCount() < M_MAX_ENTRIES) {
                                    count = likelyPlaces.getCount();
                                } else {
                                    count = M_MAX_ENTRIES;
                                }

                                int i = 0;
                                mLikelyPlaceNames = new String[count];
                                mLikelyPlaceAddresses = new String[count];
                                mLikelyPlaceAttributions = new String[count];
                                mLikelyPlaceLatLngs = new LatLng[count];

                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Build a list of likely places to show the user.
                                    mLikelyPlaceNames[i] = (String) placeLikelihood.getPlace().getName();
                                    mLikelyPlaceAddresses[i] = (String) placeLikelihood.getPlace()
                                            .getAddress();
                                    mLikelyPlaceAttributions[i] = (String) placeLikelihood.getPlace()
                                            .getAttributions();
                                    mLikelyPlaceLatLngs[i] = placeLikelihood.getPlace().getLatLng();

                                    i++;
                                    if (i > (count - 1)) {
                                        break;
                                    }
                                }

                                // Release the place likelihood buffer, to avoid memory leaks.
                                likelyPlaces.release();

                                // Show a dialog offering the user the list of likely places, and add a
                                // marker at the selected place.
                                openPlacesDialog();

                            } else {
                                Log.e(TAG, "Exception: %s", task.getException());
                            }
                        }
                    });
        } else {
            // The user has not granted permission.
            Log.i(TAG, "The user did not grant location permission.");



//            // Add a default marker, because the user hasn't selected a place.
//            Bitmap marker = createBitmapFromLayoutWithText(getApplicationContext(), "salam") ;
//            mMap.addMarker(new MarkerOptions()
//                    .title(getString(R.string.default_info_title))
//                    .position(mDefaultLocation)
//                    .snippet(getString(R.string.default_info_snippet))
////                    .icon(BitmapDescriptorFactory.fromBitmap(marker))
//                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.a))
//                    );

            // Prompt the user for permission.
            getLocationPermission();
        }
    }



    private static Bitmap createBitmapFromLayoutWithText(Context context, String text) {
        LayoutInflater mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        //Inflate the layout into a view and configure it the way you like
        RelativeLayout view = new RelativeLayout(context);
        mInflater.inflate(R.layout.map_nts_label, view, true);
        TextView tv = (TextView)view.findViewById(R.id.map_ntslabel_text);
        tv.setText(text);

        //Provide it with a layout params. It should necessarily be wrapping the
        //content as we not really going to have a parent for it.
        view.setLayoutParams(new ViewGroup.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT));

        //Pre-measure the view so that height and width don't remain null.
        view.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

        //Assign a size and position to the view and all of its descendants
        view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());

        //Create the bitmap
        Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(),
                view.getMeasuredHeight(),
                Bitmap.Config.ARGB_8888);
        //Create a canvas with the specified bitmap to draw into
        Canvas c = new Canvas(bitmap);

        //Render this view (and all of its children) to the given Canvas
        view.draw(c);
        return bitmap;
    }

    /**
     * Displays a form allowing the user to select a place from a list of likely places.
     */
    private void openPlacesDialog() {
        // Ask the user to choose the place where they are now.
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // The "which" argument contains the position of the selected item.
                LatLng markerLatLng = mLikelyPlaceLatLngs[which];
                String markerSnippet = mLikelyPlaceAddresses[which];
                if (mLikelyPlaceAttributions[which] != null) {
                    markerSnippet = markerSnippet + "\n" + mLikelyPlaceAttributions[which];
                }

                // Add a marker for the selected place, with an info window
                // showing information about that place.

                mMap.addMarker(new MarkerOptions()
                        .title(mLikelyPlaceNames[which])
                        .position(markerLatLng)
                        .snippet(markerSnippet)
                        .alpha(0.2f));

                // Position the map's camera at the location of the marker.
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(markerLatLng,
                        DEFAULT_ZOOM));
            }
        };

        // Display the dialog.
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle(R.string.pick_place)
                .setItems(mLikelyPlaceNames, listener)
                .show();
    }

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (mMap == null) {
            return;
        }
        try {
            if (mLocationPermissionGranted) {
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                mMap.setMyLocationEnabled(false);
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                mLastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    @Override
    public void onRoutingFailure(RouteException e) {
        if (Constants.debug){
            if(e != null) {
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }else {
                Toast.makeText(this, "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
            }
        }


    }

    @Override
    public void onRoutingStart() {

    }

    //function to save List<PolylineOptions>
    public void serializeDataOut(List<PolylineOptions> allPolyOptions)throws IOException{
        String fileName= "Ghost_Tour.poly";

//        OutputStreamWriter osw = new OutputStreamWriter(getApplicationContext().openFileOutput(fileName, Context.MODE_PRIVATE));

        FileOutputStream fos = getApplicationContext().openFileOutput(fileName,Context.MODE_PRIVATE);

        System.out.println("save path: "+getApplicationContext().getFilesDir().getAbsolutePath());
//        OutputStream os = new FileOutputStream();

//        FileOutputStream fos = new FileOutputStream(fileName);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(allPolyOptions);
        oos.close();
    }

    //function to load List<PolylineOptions>
    public List<PolylineOptions> serializeDataIn() throws IOException, ClassNotFoundException {
        String fileName= "Ghost_Tour.poly";



        FileInputStream fis = getApplicationContext().openFileInput(fileName);

//        FileInputStream fin = new FileInputStream(fileName);
        ObjectInputStream ois = new ObjectInputStream(fis);
        List<PolylineOptions> allPolyOptions= (List<PolylineOptions>) ois.readObject();
        ois.close();
        return allPolyOptions;
    }

    List<LatLng> getLatLongsFromPolyLine(Polyline polyline){
        return polyline.getPoints();
    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRoutIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

//        System.err.println("first route: "+ firstRoute);

        polylines = new ArrayList<>();
        //TODO: uncomment this
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            //In case of more than 5 alternative route
            int colorIndex;
            int width;
//            if (firstRoute){
//                colorIndex = (i) % COLORS.length;
//                width  = 20 + i * 3;
//            }
//            else {
            colorIndex = (i + 5) % COLORS.length;
            width = 10 + i * 3;
//            }

//            firstRoute = false;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(width);
            polyOptions.addAll(route.get(i).getPoints());
            allPolyOptions.add(polyOptions);
            Polyline polyline = mMap.addPolyline(polyOptions);

            polylines.add(polyline);
            if (Constants.debug){
                Toast.makeText(getApplicationContext(),"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
            }

        }



//        if (shouldStoreRoutes){
//            try {
//                serializeDataOut(allPolyOptions);
//            } catch (IOException e) {
//                System.err.println("error in storing tour route polylines");
//                e.printStackTrace();
//            }
//        }


    }

    @Override
    public void onRoutingCancelled() {

    }

    public void erasePolylines (){
        for (Polyline line : polylines) {
            line.remove();
        }
        polylines. clear();
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }


    @Override
    public void onBackPressed(){
        MySpeakerBox.stop();
        MainActivity.firstTime = true;
        super.onBackPressed();
    }

    public String findClosestStop() {
        String closestStop = "";
        double mindistance = Double.POSITIVE_INFINITY;
        Location l1 = new Location("");


        for  (String stopName : Constants.Ed_LANDMARKS.keySet()){

            LatLng point = Constants.Ed_LANDMARKS.get(stopName);
            l1.setLatitude(point.latitude);
            l1.setLongitude(point.longitude);

            double distancetoStops = mLastKnownLocation.distanceTo(l1);

            if (distancetoStops < mindistance){
                mindistance = distancetoStops;
                closestStop = stopName;
            }

        }
        return closestStop;
    }





}
