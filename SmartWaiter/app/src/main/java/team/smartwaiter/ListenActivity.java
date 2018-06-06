package team.smartwaiter;

import android.app.Activity;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;

import java.sql.SQLOutput;
import java.util.concurrent.TimeUnit;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Hashtable;
import java.util.HashMap;
import java.util.*;

import android.content.ActivityNotFoundException;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.speech.RecognizerIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Locale;

import team.smartwaiter.api.ApiController;
import team.smartwaiter.api.Serializer;

public class ListenActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener{
    private static TextView txtlisten;
    private static boolean hasOrdered = false;
    private static boolean hasConfirmed = false;
    private static Button reorder;
    private static Hashtable<String, Integer> orders = new Hashtable<String, Integer>();
    private TextToSpeech tts;
    final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //VARS for processing orders
    Hashtable orderpairs = new Hashtable<>();;
    ApiController controller = new ApiController();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_listen);
        reorder = (Button) findViewById(R.id.button2);
        reorder.setVisibility(View.GONE);

        reorder.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                hasOrdered = false;
                Intent intent = getIntent();
                finish();
                startActivity(intent);
            }
        });

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
        reorder.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        final String output = matches.get(0).toLowerCase();
        System.out.println("Output: " + output);

        for (String x : matches) {
            System.out.println(x);
        }

        System.out.println(output);

        //List<String> food = Arrays.asList("burger", "rice", "spaghetti", "mixed grill", "soup", "steak", "salad", "macaroni");
        List<String> menu = null;
        menu = Serializer.ConvertMenu(controller.getMenu(), "name");

        if (!hasOrdered) {
            if (!processMeal(menu, matches)) {
                // If order not complete, trying again here
                System.out.println("Couldn't find item");
                reprompt(7);
            } else {
                // If the order complete, asking for confirmation here
                reprompt(5);
            }

        } else {
            System.out.println("is true");
            for (String d : matches) {
                if (d.toLowerCase().contains("yes") | d.toLowerCase().contains("yeah") | d.toLowerCase().contains("okay")) {
                    reorder.setVisibility(View.VISIBLE);
                    speak("Order confirmed.");

                    //Now that one orderline has been confirmed (e.g. 2 cola's) we need to push it to the db

                    for (Object key : orderpairs.keySet()) {
                        System.out.println("KEY: " + key);
//                        controller.postOrderLine();
//                        speakorder += orderpairs.get(key) + " " + key;
//                        order += key + " | amount: " + orderpairs.get(key) + "\n";
                    }

                    txtlisten.setText("Order confirmed.");
                    break;
                } else if (d.toLowerCase().contains("no") | d.toLowerCase().contains("nope")) {
                    speak("Okay, order canceled.");
                    reorder.setVisibility(View.VISIBLE);
                    txtlisten.setText("Order canceled.");
                    hasOrdered = false;
//                    reprompt(10);
                    break;
                } else {
                    speak("Sorry I didn't catch that, can you say that again?");
                    txtlisten.setText("Didn't catch that, can you say that again?");
                    reprompt(5);
                    break;
                }
            }
        }
    }


    public boolean processMeal(List<String> typelist,  ArrayList<String> output){

        orderpairs.clear();

        Logic logic = new Logic(typelist, output);
        orderpairs = logic.generate();

        System.out.println(typelist);
        System.out.println(orderpairs);

        if(orderpairs.size() < 1){
            txtlisten.setText("I didn't quite catch that");
            speak("I can't seem to figure out what you said, please try again.");
            return false;
        } else {
            Set<String> keys = orderpairs.keySet();

            String order = "Order:\n";
            String speakorder = "Your order consists of the following: ";

            for (String key : keys) {
//                System.out.println(orderpairs.get(key) + " " + key);
                speakorder += orderpairs.get(key) + " " + key;
                order += key + " | amount: " + orderpairs.get(key) + "\n";
            }

            speakorder += ". Confirm by saying yes";

            System.out.println("ORDERPAIRS: " + orderpairs);
            txtlisten.setText(order);
            speak(speakorder);
            hasOrdered = true;
            return true;

        }


    }

    public void reprompt(Integer sleepduration){
        try {
            TimeUnit.SECONDS.sleep(sleepduration);
            speech.startListening(intent);
        } catch (InterruptedException e) {
            System.out.println("Error from reprompt");
        }
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
                if(status != TextToSpeech.ERROR)
                    tts.setLanguage(Locale.US);
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
