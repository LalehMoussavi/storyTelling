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

import android.os.Environment;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.File;

/**
 * Constants used in this sample.
 */

final class Constants {

    private Constants() {
    }

    private static final String PACKAGE_NAME = "com.google.android.gms.location.Geofence";

    static final String GEOFENCES_ADDED_KEY = PACKAGE_NAME + ".GEOFENCES_ADDED_KEY";

    /**
     * Used to set an expiration time for a geofence. After this amount of time Location Services
     * stops tracking the geofence.
     */
    private static final long GEOFENCE_EXPIRATION_IN_HOURS = 12;

    /**
     * For this sample, geofences expire after twelve hours.
     */
    static final long GEOFENCE_EXPIRATION_IN_MILLISECONDS =
            GEOFENCE_EXPIRATION_IN_HOURS * 60 * 60 * 1000;
//    static final float GEOFENCE_RADIUS_IN_METERS = 1609; // 1 mile, 1.6 km
    static final float GEOFENCE_RADIUS_IN_METERS = 40;//Laleh changed to 30 m

    /**
     * Map for storing information about story stops in the Edinburgh.
     */
    static final LinkedHashMap<String, LatLng> Ed_LANDMARKS = new LinkedHashMap<>();
    public static HashMap<String,String> id2Story = new HashMap<>();

    //TODO: Create a hashMap from numbers (0-26) to numbers (R.drawable.a...)
    static final HashMap<Integer, Integer> unSeenStop2Sign = new HashMap<>();
    static final HashMap<Integer, Integer> seenStop2Sign = new HashMap<>();


    static {
        // Ghost tour stops


        Ed_LANDMARKS.put("Informatics Forum",new LatLng(55.944648,-3.187364));

        Ed_LANDMARKS.put("Door Without card",new LatLng(55.944909, -3.186446));

        Ed_LANDMARKS.put("Edinburgh Castle",new LatLng(55.948583, -3.199919));

        Ed_LANDMARKS.put("School of Geosciences",new LatLng(55.948098, -3.183513));
        
        Ed_LANDMARKS.put("Castlehill",new LatLng(55.948614, -3.198350));

        Ed_LANDMARKS.put("Lawnmarket and Victoria Street",new LatLng(55.948792, -3.193799));

        Ed_LANDMARKS.put("Mary King's Close",new LatLng(55.949873, -3.190470));


        //Ed_LANDMARKS.put("Upper High Street",new LatLng(55.949802, -3.190169));

        Ed_LANDMARKS.put("Mid-High Street",new LatLng(55.950202, -3.187189));

        Ed_LANDMARKS.put("Lower High Street",new LatLng(55.950332, -3.186279));

        Ed_LANDMARKS.put("Canongate I",new LatLng(55.950702, -3.183809));

        Ed_LANDMARKS.put("Canongate II",new LatLng(55.951503, -3.179397));

        Ed_LANDMARKS.put("Holyrood",new LatLng(555.952665, -3.172624));

        //TODO: fill out this hashMap in the static block below similar to Ed_LANDMARKS

        unSeenStop2Sign.put(0,R.drawable.a);
        unSeenStop2Sign.put(1,R.drawable.b);
        unSeenStop2Sign.put(2,R.drawable.c);
        unSeenStop2Sign.put(3,R.drawable.d);
        unSeenStop2Sign.put(4,R.drawable.e);
        unSeenStop2Sign.put(5,R.drawable.f);
        unSeenStop2Sign.put(6,R.drawable.g);
        unSeenStop2Sign.put(7,R.drawable.h);
        unSeenStop2Sign.put(8,R.drawable.i);
        unSeenStop2Sign.put(9,R.drawable.j);
        unSeenStop2Sign.put(10,R.drawable.k);
        unSeenStop2Sign.put(11,R.drawable.l);
        unSeenStop2Sign.put(12,R.drawable.m);
        unSeenStop2Sign.put(13,R.drawable.n);
        unSeenStop2Sign.put(14,R.drawable.o);
        unSeenStop2Sign.put(16,R.drawable.p);
        unSeenStop2Sign.put(17,R.drawable.q);
        unSeenStop2Sign.put(18,R.drawable.r);
        unSeenStop2Sign.put(19,R.drawable.s);
        unSeenStop2Sign.put(20,R.drawable.t);
        unSeenStop2Sign.put(21,R.drawable.u);
        unSeenStop2Sign.put(22,R.drawable.v);
        unSeenStop2Sign.put(23,R.drawable.w);
        unSeenStop2Sign.put(24,R.drawable.x);
        unSeenStop2Sign.put(25,R.drawable.y);
        unSeenStop2Sign.put(26,R.drawable.z);

        seenStop2Sign.put(0,R.drawable.ag);
        seenStop2Sign.put(1,R.drawable.bg);
        seenStop2Sign.put(2,R.drawable.cg);
        seenStop2Sign.put(3,R.drawable.dg);
        seenStop2Sign.put(4,R.drawable.eg);
        seenStop2Sign.put(5,R.drawable.fg);
        seenStop2Sign.put(6,R.drawable.gg);
        seenStop2Sign.put(7,R.drawable.hg);
        seenStop2Sign.put(8,R.drawable.ig);
        seenStop2Sign.put(9,R.drawable.jg);
        seenStop2Sign.put(10,R.drawable.kg);
        seenStop2Sign.put(11,R.drawable.lg);
        seenStop2Sign.put(12,R.drawable.mg);
        seenStop2Sign.put(13,R.drawable.ng);
        seenStop2Sign.put(14,R.drawable.og);
        seenStop2Sign.put(16,R.drawable.pg);
        seenStop2Sign.put(18,R.drawable.rg);
        seenStop2Sign.put(19,R.drawable.sg);
        seenStop2Sign.put(20,R.drawable.tg);
        seenStop2Sign.put(21,R.drawable.ug);
        seenStop2Sign.put(22,R.drawable.vg);
        seenStop2Sign.put(23,R.drawable.wg);
        seenStop2Sign.put(24,R.drawable.xg);
        seenStop2Sign.put(25,R.drawable.yg);
        seenStop2Sign.put(26,R.drawable.zg);



        String story = "";

        System.out.println(Environment.getExternalStorageDirectory().toString());
        File dir = new File("Ghost_Tour/");
        System.out.println(dir);
        System.out.println(dir.listFiles());


        try {
            InputStream is = MainActivity.am.open("ids.txt");
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String id = null;
            while ((id = br.readLine())!=null){
//                 String storyFileName = "Ghost_Tour/" + id+".txt";
                String storyFileName =  id+".txt";

                InputStream is2 = MainActivity.am.open(storyFileName);
                BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));

                String line = null;
                story="";
                while ((line = br2.readLine())!=null){
                    story += line+"\n";


                }
                id2Story.put(id,story);






            }

        } catch (IOException e) {
            e.printStackTrace();
        }

//        id2Story.put("Home",story);
//        id2Story.put("Informatics Forum",story);
//        id2Story.put("Edinburgh Castle",story);
//        id2Story.put("Castlehill",story);
//        id2Story.put("Lawnmarket and Victoria Street",story);
//        id2Story.put("Mary King's Close",story);
//        id2Story.put("Upper High Street",story);
//        id2Story.put("Mid-High Street",story);
//        id2Story.put("Lower High Street",story);
//        id2Story.put("Canon gate I",story);
//        id2Story.put("Canon gate II",story);
//        id2Story.put("Holyrood",story);
    }
}
