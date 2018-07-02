package com.google.android.gms.location.sample.geofencing;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class Util {

    public static String readFileFromAsset(String fname){
        String ret="";
        try {
            InputStream is2 = MainActivity.am.open(fname);
            BufferedReader br2 = new BufferedReader(new InputStreamReader(is2));

            String line = null;

            while ((line = br2.readLine())!=null){
                ret += line+"\n";
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        return ret;
    }

}
