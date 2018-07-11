package com.google.android.gms.location.sample.geofencing;

import android.view.View;

import com.mapzen.speakerbox.Speakerbox;

public class MySpeakerBox {
    static Speakerbox speakerbox;
    static long lastStoryStarted = -1;


   static{
       speakerbox = new Speakerbox(MainActivity.uniqueMainActivity.getApplication());
   }

   public static void play (String text, boolean isStory){
       if (isStory){
           lastStoryStarted = System.currentTimeMillis();
       }
//       else if (lastStoryStarted!=-1 && (System.currentTimeMillis()-lastStoryStarted)<120000){
//           return;
//       }

        if (speakerbox != null) {
            speakerbox.stop();
            speakerbox = new Speakerbox(MainActivity.uniqueMainActivity.getApplication());
        }

        System.out.println("speaking from MyspeakerBox");
        speakerbox.play(text);

    }

   public static void stop (){
        if (speakerbox != null) {
          speakerbox.stop();
        }
    }

}
