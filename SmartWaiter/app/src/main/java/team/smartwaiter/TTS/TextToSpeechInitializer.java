package team.smartwaiter.TTS;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.Nullable;
import android.util.Log;

import java.sql.SQLOutput;
import java.util.*;

import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;

public class TextToSpeechInitializer extends Service{

    private Context context;
    private static TextToSpeech talk;
    private TextToSpeechIniListener callback;
    private final Locale locale = Locale.US;

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
                    callback.onSucces(talk);
                }else{
                    callback.onFailure(talk);
                    Log.e("TTS","TextToSpeechInitializeError");
                }
            }
        });
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}