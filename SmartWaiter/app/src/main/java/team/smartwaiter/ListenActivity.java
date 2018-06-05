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
        ApiController controller = new ApiController();
        List<String> menu = null;
        menu = Serializer.ConvertMenu(controller.getMenu(), "name");

//        System.out.println("Printing menu items -------------------");
//        for (String b : menu) {
//            System.out.println(b);
//        }

//       List<String> drinks = Arrays.asList("cola", "ice tea", "fanta", "lemonade", "chocolate milk");

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
        List<String> orderlist = new ArrayList<>();
        List<String> orderamounts = new ArrayList<>();
        Hashtable orderpairs = new Hashtable<>();

        List<String> amount = Arrays.asList("one", "two", "three", "four", "five");
        List<String> amountnum = Arrays.asList("1", "2", "3", "4", "5");
        String meal = "";
        String totalamount = "";

        boolean foundconsumable = false;
        boolean foundamount = false;

        for (String line : output) {
//                System.out.println("LINE: " + line);
                if (foundconsumable && orderamounts.size() == orderlist.size()) {
                    System.out.println("foundboth");

                    System.out.println("PRINTING ORDERLIST_________________________________");
                    System.out.println(orderlist);
                    System.out.println("END ORDERLIST______________________________________*");


                    System.out.println("PRINTING ORDERAMOUNTS_______________________________");
                    System.out.println(orderamounts);
                    System.out.println("END ORDERAMOUNTS____________________________________*");


                    if (orderlist.size() > 1) {
                        System.out.println("SIZE OF ORDERLIST = " + orderlist.size());
                        System.out.println("_____________________________________");
                        System.out.println("PRINTING ITEMS AND AMOUNTS");
                        for (int i = 0; i <= orderlist.size() - 1; i++) {
                            System.out.println("food " + orderlist.get(i));
                            System.out.println("amount " + orderamounts.get(i));
                            orderpairs.put(orderlist.get(i), orderamounts.get(i));
                        }

                        Set<String> keys = orderpairs.keySet();

                        String order = "Order:\n";
                        String speakorder = "Your order consists of the following: ";

                        for (String key : keys) {
                            System.out.println(orderpairs.get(key) + " " + key);
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

//                    System.out.println("PRINTING ORDERAMOUNTS");
//                    for (String oa : orderamounts) {
//                        System.out.println(oa);
//                    }

//                orders.put(meal, Integer.parseInt(totalamount));

                    String order = "Order: " + meal + " | amount: " + totalamount;
                    txtlisten.setText(order);
                    speak(totalamount + " " + meal + ". Confirm by saying yes.");
                    hasOrdered = true;
                    return true;
                } else {
                    if (!foundconsumable) {
                        for (String word : line.split(" ")) {
                            for (String consumable : typelist) {
                                if (word.toLowerCase().contains(consumable)) {
                                    meal = consumable;
                                    orderlist.add(consumable);
                                    if (!foundconsumable) {
                                        foundconsumable = true;
                                    }
                                }
                            }
                        }
                    }

                        for (String word : line.split(" ")) {
                            for (String am : amount) {
                                if (word.contains(am)) {
                                    totalamount = am;
                                    orderamounts.add(am);
                                }
                            }
                        }
                        for (String word : line.split(" ")) {
                            for (String an : amountnum) {
                                if (word.contains(an)) {
                                    totalamount = an;
                                    orderamounts.add(an);
                                }
                            }
                        }
                    }
                }

        if (foundconsumable && orderlist.size() != orderamounts.size()){
            System.out.println("Found foods but no amount");
            if (orderlist.size() > 1) {
                System.out.println("SIZE OF ORDERLIST = " + orderlist.size());
                for (int i = 0; i <= orderlist.size() - 1; i++) {
                    System.out.println("food " + orderlist.get(i));
//                    System.out.println("amount " + orderamounts.get(i));
                    orderpairs.put(orderlist.get(i), "one");
                }

                Set<String> keys = orderpairs.keySet();

                String order = "Order:\n";
                String speakorder = "Your order consists of the following: ";

                for (String key : keys) {
                    System.out.println(orderpairs.get(key) + " " + key);
                    speakorder += orderpairs.get(key) + " " + key;
                    order += key + " | amount: " + orderpairs.get(key) + "\n";
                }

                speakorder += ". Confirm by saying yes";

                System.out.println("ORDERPAIRS: " + orderpairs);
                txtlisten.setText(order);
                speak(speakorder);
                hasOrdered = true;
                return true;

            } else {
                
            String order = "Order: " + meal + " | amount: one";
            txtlisten.setText(order);
            speak("One " + meal + ". Confirm by saying yes.");
            hasOrdered = true;
            return true;
            }
        }


        txtlisten.setText("I didn't quite catch that");
        speak("I can't seem to figure out what you said, please try again.");
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

}
