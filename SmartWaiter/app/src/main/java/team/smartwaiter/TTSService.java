package team.smartwaiter;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

public class TTSService extends Activity implements TextToSpeech.OnInitListener {
//    private static Button button;
    private TextToSpeech tts;
    private String text1 = "First Hello World!";
    private String text2 = "Second hello world";
    private String text3 = "Why, creator, why?!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = tts.setLanguage(Locale.ENGLISH);
                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        System.out.println("This Language is not supported");
                    }
                    speak("Hello, my name is Iris. How can i be of assistance?");

                } else {
                    System.out.println("Initilization Failed!");
                }
            }

        });
//        speak();

    }

    public void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void speakit(String text){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(final int status) {
        new Thread(new Runnable() {
            public void run() {
                if(status != TextToSpeech.ERROR) // initialization me error to nae ha
                {
                    tts.setPitch(1.1f); // saw from internet
                    tts.setSpeechRate(0.4f); // f denotes float, it actually type casts 0.5 to float
                    tts.setLanguage(Locale.US);
                }
            }
        }).start();

    }

    protected void OnActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS){
            tts = new TextToSpeech(this, this);
        } else {
            Intent installIntent = new Intent();
            installIntent.setAction(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
            startActivity(installIntent);
        }
    }

    @Override
    protected void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }
}