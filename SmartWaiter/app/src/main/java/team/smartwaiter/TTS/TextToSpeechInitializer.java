package team.smartwaiter.TTS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.*;

public class TextToSpeechInitializer extends Service{

    private Context context;
    private static TextToSpeech talk;
    private TextToSpeechIniListener callback;
    private final Locale locale = Locale.US;
    private static boolean repr = false;
//    private Runnable r;

    public TextToSpeechInitializer(Context context , Locale locale , TextToSpeechIniListener l) {
        this.context = context;
        if(l != null) {
            callback = l;
        }
        initialize();
    }

    public TextToSpeechInitializer(){

    }


    private void initialize() {
        talk = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(final int status) {
                if (status == TextToSpeech.SUCCESS) {
                    talk.setLanguage(locale); //TODO: Check if locale is available before setting.
                    talk.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override
                        public void onStart(String s) {
//                            txtlisten.setText("Started speaking");
                            System.out.println("Caused by: " + s);
                        }

                        @Override
                        public void onDone(String s) {
                            if (repr) {
                                repr = false;
                                System.out.println("Change current REPR to " + repr);
                                callback.execReprompt();
                                System.out.println("REPROMPTING");
                            }

                            callback.onFinishedSpeaking();

                        }

                        @Override
                        public void onError(String s) {

                        }
                    });

                    callback.onSuccess(talk);
                }else{
                    callback.onFailure(talk);
                    Log.e("TTS","TextToSpeechInitializeError");
                }
            }
        });
    }

    public void speak(String text, HashMap<String, String> uttID, boolean... r){
        System.out.println("To speak: " + text);
        System.out.println("CURRENT REPR: " + repr);
        if (r.length != 0){
            System.out.println("GIVEN REPR: " + r[0]);
            repr = r[0];
            System.out.println("CHANGED CURRENT REPR TO " + repr);
//            hasHandler = true;
        }

        talk.speak(text, TextToSpeech.QUEUE_FLUSH, uttID);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}