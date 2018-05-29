package team.smartwaiter;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

public class TTSService extends AppCompatActivity implements TextToSpeech.OnInitListener {
    private static Button button;
    private TextToSpeech tts;
    private String text1 = "Hello World!";
    private String text2 = "I am in pain!";
    private String text3 = "Why, creator, why?!";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tts = new TextToSpeech(this, this);


    }

    public void speak(){
        tts.speak(text1, TextToSpeech.QUEUE_FLUSH, null);
        tts.speak(text2, TextToSpeech.QUEUE_ADD, null);
        tts.speak(text3, TextToSpeech.QUEUE_ADD, null);
    }

    public void speak(String text){
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
    }

    @Override
    public void onInit(int status) {

        if (status == TextToSpeech.SUCCESS) {

            int result = tts.setLanguage(Locale.UK);

            if (result == TextToSpeech.LANG_MISSING_DATA
                    || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                System.out.println("TTS This Language is not supported");
            } else {
                button.setEnabled(true);
                speak();
            }

        } else {
            System.out.println("TTS Initilization Failed!");
        }

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
        super.onDestroy();
        tts.shutdown();
    }
}