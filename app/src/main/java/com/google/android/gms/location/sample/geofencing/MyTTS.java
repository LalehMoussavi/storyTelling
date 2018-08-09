package com.google.android.gms.location.sample.geofencing;

import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.View;

import com.mapzen.speakerbox.Speakerbox;

import java.util.HashMap;
import java.util.Locale;

public class MyTTS {
//    static Speakerbox speakerbox;
    static long lastStoryStarted = -1;
    static TextToSpeech tts;
    static int uniqueID = 0;//to be incremented for each time we call play
    static String lastText = "";

    public static void play (String text, boolean isStory){
        play(text,isStory,null);
    }

   public static void play (String text, boolean isStory, TTSFinshHandler caller){
       text = text.replace ("Cannongate I", "Cannongate one");
       text = text.replace("Canongate II", "Canongate two");
       text = text.replace("James IV", "James fourth");
       if (tts!=null && lastText.startsWith("You have arrived at") && tts.isSpeaking()){
           tts.stop();
       }
       else if (tts!=null && tts.isSpeaking()){
           System.out.println("not accepting new text, it's already playing something");
           return;
       }
       else{
           System.out.println("Sure we accept a new text");
       }

       if (isStory){
           lastStoryStarted = System.currentTimeMillis();
       }

       lastText = text;

        System.out.println("speaking from MyspeakerBox");

       final String myText = text;
       final TTSFinshHandler myCaller = caller;

       final HashMap<String, String> map = new HashMap<>();
       map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, (uniqueID)+"");
       uniqueID++;

       tts = new TextToSpeech(MainActivity.uniqueMainActivity.getApplicationContext(), new TextToSpeech.OnInitListener() {
           @Override
           public void onInit(int status) {
               System.out.println("status: "+ status);
               if (status==TextToSpeech.SUCCESS){
                   tts.setLanguage(Locale.UK);
                   tts.speak(myText,TextToSpeech.QUEUE_FLUSH, map);

                   tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                       @Override
                       public void onDone(String utteranceId) {

                           if (myCaller!=null){
                               myCaller.onTTSFinish();
                           }

                       }

                       @Override
                       public void onError(String utteranceId) {
                       }

                       @Override
                       public void onStart(String utteranceId) {
                       }
                   });

               }


           }
       });

    }

   public static void stop (){

       System.out.println("stop requested");
//       try{
//           throw new RuntimeException("just checking");
//       }
//       catch (RuntimeException e){
//           e.printStackTrace();
//       }

//        if (speakerbox != null) {
//          speakerbox.stop();
//        }
       if (tts!=null && tts.isSpeaking()){
           tts.stop();
       }
    }

}
