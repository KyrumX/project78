package team.smartwaiter;

import android.app.Activity;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;


import java.util.concurrent.TimeUnit;

import java.util.Arrays;
import java.util.List;
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
import team.smartwaiter.api.OrderProcessor;
import team.smartwaiter.api.Serializer;
import team.smartwaiter.tools.GeneralTools;

public class ListenActivity extends Activity implements RecognitionListener, TextToSpeech.OnInitListener{
    private TextView txtlisten;
    private static boolean hasOrdered = false;
    private static Intent intentmenu;
    private Button reorder;
    private Button backtomenu;
    private TextToSpeech tts;
    final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private final int REQ_CODE_SPEECH_INPUT = 100;

    //VARS for processing orders
    HashMap orderpairs = new HashMap();
    ApiController controller = new ApiController();
    OrderProcessor orderProcessor = new OrderProcessor();

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_listen);

        intentmenu = new Intent(this, MainActivity.class);

        backtomenu = findViewById(R.id.button3);
        backtomenu.setVisibility(View.GONE);

        backtomenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                intentmenu.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity ( intentmenu );
            }
        });

        reorder = findViewById(R.id.button2);
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

        txtlisten = findViewById(R.id.txtlisten);

        try {
            speech.startListening(intent);
        } catch (ActivityNotFoundException a) {
            System.out.println("activity not found exception");
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQ_CODE_SPEECH_INPUT: {
                if (resultCode == RESULT_OK && null != data) {
                    ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
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

        //List<String> food = Arrays.asList("burger", "rice", "spaghetti", "mixed grill", "soup", "steak", "salad", "macaroni");
        List<String> menu;
        menu = Serializer.ConvertMenu(controller.getMenu(), "name");

        if (!hasOrdered) {
            if (hasInfo(menu, matches)) {
                System.out.println("has info !!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            } else {
                System.out.println("doesn't have info");
                if (!processMeal(menu, matches)) {
                    // If order not complete, trying again here
                    System.out.println("Couldn't find item");
                    reprompt(7);
                } else {
                    // If the order complete, asking for confirmation here
                    reprompt(5);
                }
            }
        } else {
            List<String> confirmationlist = Arrays.asList("yes", "yeah", "sure", "alright", "okay", "affirmative");
            List<String> denylist = Arrays.asList("no", "nope", "nah", "not", "cancel");

            if (GeneralTools.checkForWords(matches, confirmationlist) != "null"){
                reorder.setVisibility(View.VISIBLE);
                speak("Order confirmed.");

                //Now that one orderline has been confirmed (e.g. 2 cola's) we need to push it to the db
                orderProcessor.createNewOrderLine(orderpairs); // <-- Process all the ordered items

                //OrderLines have been processed.

                txtlisten.setText("Order confirmed.");
            } else if(GeneralTools.checkForWords(matches, denylist) != "null"){
                speak("Okay, order canceled.");
                reorder.setVisibility(View.VISIBLE);
                txtlisten.setText("Order canceled.");
                hasOrdered = false;
            } else {
                speak("Sorry I didn't catch that, can you say that again?");
                txtlisten.setText("Didn't catch that, can you say that again?");
                reprompt(5);
            }
        }
    }


    public boolean processMeal(List<String> typelist,  ArrayList<String> output){

        orderpairs.clear();

        Logic logic = new Logic(typelist, output);
        orderpairs = logic.generate();

        System.out.println("typelist: " + typelist);
        System.out.println("orderpairs: " + orderpairs);

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

    public Boolean hasInfo(List<String> consumables, List<String> output) {
        List<String> generalinfolist = Arrays.asList("description", "information", "info", "about");
        List<String> price = Arrays.asList("price", "cost");
        List<String> allergies = Arrays.asList("allergy", "allergies", "allergic");

        String menuitem = GeneralTools.checkForWords(output, consumables);

        if (menuitem == "null"){
            // couldn't find a menuitem in the output
            speak("I can't seem to figure out what you said, please try again.");
            return false;
        }

        String generalinfo;
        String infotype;
        if ((generalinfo = GeneralTools.checkForWords(output, generalinfolist, true)) != "null") {
            // user asks for description of a product
            System.out.println("debug hasinfo_____________________");
            System.out.println(generalinfo);
            System.out.println(menuitem);
            System.out.println("end debug hasinfo_________________");
            showinfo(generalinfo, menuitem);
            return true;
        } else if ((infotype = (GeneralTools.checkForWords(output, price, true))) != "null") {
            // user asks for the price of a product
            showinfo(infotype, menuitem, "The price of " + menuitem + " is ", "euros", "€");
            return true;
        } else if ((infotype = (GeneralTools.checkForWords(output, allergies, true))) != "null") {
            // user asks about potential allergy substances in a product
            if (getInformation.showInformation(menuitem, "allergy").equals("none")) {
                speak(menuitem + " contains no potential allergy substances");
                txtlisten.setText("No allergies inside " + menuitem);
                backtomenu.setVisibility(View.VISIBLE);
            } else {
                showinfo(infotype, menuitem, menuitem + " ");
            }
            return true;
        }

        return false;
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

    public void showinfo(String infotype, String menuitem, String... args){
        String prefix =  (args.length == 0) ? "" : args[0];
        String suffix = (args.length < 2) ? "" : args[1];
        String prefix_text = (args.length < 3) ? "" : args[2];

        backtomenu.setVisibility(View.VISIBLE);

        if (suffix.equals("euros")){
            speak(prefix + GeneralTools.outputMoney(getInformation.showInformation(menuitem, infotype)));
        } else {
            speak(prefix + getInformation.showInformation(menuitem, infotype));
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        txtlisten.setText(GeneralTools.capitalize(menuitem) + "\n\n" + prefix_text + getInformation.showInformation(menuitem, infotype));
    }

}
