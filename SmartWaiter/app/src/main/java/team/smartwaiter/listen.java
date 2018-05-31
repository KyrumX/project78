package team.smartwaiter;

import android.app.Activity;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.List;

import android.content.ActivityNotFoundException;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import android.widget.TextView;
import android.speech.RecognizerIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Locale;

public class listen extends Activity implements RecognitionListener, TextToSpeech.OnInitListener{
    private static TextView txtlisten;
    private TextToSpeech tts;
    final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private final int REQ_CODE_SPEECH_INPUT = 100;

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_listen);

        tts = new TextToSpeech(this, this);

        speech.setRecognitionListener(this);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);

//        Voor Nederlands, gebruik onderstaande code. Apparaat moet wel ingesteld zijn op Nederlands.
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        txtlisten = (TextView) findViewById(R.id.txtlisten);

        try {
            speech.startListening(intent);
        } catch (ActivityNotFoundException a) {

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    txtlisten.setText(result.get(0));
                }
                break;
            }

        }
    }

    @Override
    public void onReadyForSpeech(Bundle bundle) {
//        txtlisten.setText("Ready for input");
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
        txtlisten.setText("Processing..");
    }

    @Override
    public void onError(int i) {
        txtlisten.setText("I didn't quite catch that..");
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        final String output = matches.get(0);
        for (String x : matches) {
            System.out.println(x);
        }
        System.out.println(output);
//        speech.destroy();
//        speech.cancel();
        txtlisten.setText(output);

        List<String> food = Arrays.asList("burger", "rice", "spaghetti", "mixed grill", "soup", "steak", "salad", "macaroni");
        List<String> drinks = Arrays.asList("cola", "ice tea", "Fanta", "lemonade", "chocolate milk");
//        List<String> amount = Arrays.asList("one", "two", "three", "four", "five");
//        List<String> amountnum = Arrays.asList("1", "2", "3", "4", "5");

        if (!processMeal(food, output)) {
            processMeal(drinks, output);
        }

    }


    public boolean processMeal(List<String> typelist,  String output){

        List<String> amount = Arrays.asList("one", "two", "three", "four", "five");
        List<String> amountnum = Arrays.asList("1", "2", "3", "4", "5");

        boolean productfound = false;
        for (String x : typelist) {
            if (output.contains(x)) {
                productfound = true;
                boolean foundamount = false;
                for (String a : amount) {
                    if (output.contains(a)) {
                        String order = "Order: " + x + " | amount: " + a;
                        txtlisten.setText(order);
                        speak(a + " " + x + ". Confirm by saying yes.");
                        foundamount = true;
                        return true;
                    }
                }

                if (!foundamount) {
                    for (String z : amountnum){
                        if (output.contains(z)){
                            String order = "Order: " + x + " | amount: " + z;
                            txtlisten.setText(order);
                            speak(z + " " + x + ". Confirm by saying yes.");
                            foundamount = true;
                            return true;
                        }
                    }
                }

                if (!foundamount) {
                    String order = "Order: " + x + " | amount: one";
                    txtlisten.setText(order);
                    speak("One " + x + ". Confirm by saying yes.");
                    return true;
                }



            }
        }
        if (!productfound){
            txtlisten.setText("I didn't quite catch that");
            speak("I can't seem to figure out what you said, please try again.");
        }

        return false;
    }


    @Override
    public void onPartialResults(Bundle bundle) {

    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    @Override
    public void onInit(final int status) {
        new Thread(new Runnable() {
            public void run() {
                if(status != TextToSpeech.ERROR) // initialization me error to nae ha
                {
                    tts.setLanguage(Locale.US);
                }
            }
        }).start();
    }

    public void speak(String text){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
        }else{
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

}
