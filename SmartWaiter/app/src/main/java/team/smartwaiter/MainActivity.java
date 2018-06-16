package team.smartwaiter;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.os.Bundle;


import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.*;

import android.content.ActivityNotFoundException;

import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;

import android.support.annotation.NonNull;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.speech.RecognizerIntent;
import android.content.Intent;

import java.util.ArrayList;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import team.smartwaiter.TTS.TextToSpeechIniListener;
import team.smartwaiter.TTS.TextToSpeechInitializer;
import team.smartwaiter.api.ApiController;
import team.smartwaiter.api.OrderProcessor;
import team.smartwaiter.api.Serializer;
import team.smartwaiter.storage.OrderDataSingleton;
import team.smartwaiter.tools.Fade;
import team.smartwaiter.tools.GeneralTools;
import team.smartwaiter.tools.TypeWriter;

//import static team.smartwaiter.MainActivity.orderDataSingleton;
import static team.smartwaiter.tools.GeneralTools.animateTxt;
import static team.smartwaiter.tools.GeneralTools.setAlphaAnimation;

public class MainActivity extends Activity implements edu.cmu.pocketsphinx.RecognitionListener, android.speech.RecognitionListener, TextToSpeechIniListener {
    private TextToSpeechInitializer i;
    private TextToSpeech talk;
    boolean hasSaidHeyIris = false;
    private TextView status;
    public static TypeWriter txtlisten;
    public static OrderDataSingleton orderDataSingleton = OrderDataSingleton.getInstance();
    private ProgressBar progresslisten;
    public static Handler handler = new Handler();
    private static boolean hasOrdered = false;
    private boolean flag = false;
    private boolean hasLeftBeginScreen = false;
    private Fade animator;
    private String[] voorbeeldzinnen = new String[5];
    private boolean notInListenContentView = false;
    private static final String KWS_SEARCH = "wakeup";
    private static final String KEYPHRASE = "hey iris";
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;
    private edu.cmu.pocketsphinx.SpeechRecognizer recognizer;
    final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private static Intent intentmenu;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    //VARS for processing orders
    HashMap orderpairs = new HashMap();
    ApiController controller = new ApiController();
    OrderProcessor orderProcessor = new OrderProcessor();

    public MainActivity() {
        orderDataSingleton.update();
    }

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        i = new TextToSpeechInitializer(this, Locale.US, this);

        voorbeeldzinnen = new String[]{getString(R.string.cap1), getString(R.string.cap2), getString(R.string.cap3), getString(R.string.cap4), getString(R.string.cap5)};

        startExamples();

        final TextToSpeechIniListener ini = this;

        txtlisten = (TypeWriter) findViewById(R.id.txtlisten);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                i = new TextToSpeechInitializer(getApplicationContext(), Locale.US, ini);
            }
        });

        speech.setRecognitionListener(this);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);


//        Voor Nederlands, gebruik onderstaande code. Apparaat moet wel ingesteld zijn op Nederlands.
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        status = findViewById(R.id.status);
        setAlphaAnimation(status);
//        updateStatus("Waiting for 'hey Iris'", false);
        progresslisten = findViewById(R.id.progresslisten);
        progresslisten.setVisibility(View.GONE);

        TextView orderTextView = (TextView) findViewById(R.id.orderTextView);
        orderTextView.setText("#order: " + Integer.toString(orderDataSingleton.getOrderID()));

        new SetupTask(this).execute();

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
//        updateStatus("Waiting for 'hey Iris'", false);
    }


    @Override
    public void onError(int i) {
        animateTxt(txtlisten, "I didn't quite catch that..");
//        reorder.setVisibility(View.VISIBLE);
        updateStatus("Waiting for 'hey iris'", false);
        startListening(KWS_SEARCH);
    }

    public void onResults(Hypothesis hypothesis){

    }

    @Override
    public void onResults(Bundle results) {
        List<String> summarylist = Arrays.asList("overview", "summary");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        final String output = matches.get(0).toLowerCase();
        System.out.println("Output: " + output);

        //List<String> food = Arrays.asList("burger", "rice", "spaghetti", "mixed grill", "soup", "steak", "salad", "macaroni");
        List<String> menu;
        menu = Serializer.ConvertMenu(controller.getMenu(), "name");

        // if no order has been placed yet
        if (GeneralTools.checkForWords(matches, summarylist) != "null") {
//            setContentView(R.layout.activity_summ);
            notInListenContentView = true;
            System.out.println("SHOWING SUMMARY----------------------");
            getOrderSummary();
            // if no order has been placed yet
        }else if (!hasOrdered) {

            // if it doesn't have keywords for information e.g. 'allergies' or 'information'
            if (!hasInfo(menu, matches)) {
                getMeal(menu, matches);
//           speak("I can't seem to figure out what you said, please try again.", "nomenuitem_hasinfo", true);
            }
        } else {
            // if an order has been placed, this will confirm or cancel
            List<String> confirmationlist = Arrays.asList("yes", "yeah", "sure", "alright", "okay", "affirmative");
            List<String> denylist = Arrays.asList("no", "nope", "nah", "not", "cancel");

            // checks relation between the strings above and the output gotten from user speech
            if (GeneralTools.checkForWords(matches, confirmationlist) != "null"){
                speak("Order confirmed.", "confirmed");
                hasOrdered = false;

                //Now that one orderline has been confirmed (e.g. 2 cola's) we need to push it to the db
                orderProcessor.createNewOrderLine(orderpairs); // <-- Process all the ordered items

                //OrderLines have been processed.

                animateTxt(txtlisten, "Order confirmed.");

                // this updates the status bar at the top of the application
                updateStatus("Waiting for 'hey Iris'", false);
                startListening(KWS_SEARCH);
            } else if(GeneralTools.checkForWords(matches, denylist) != "null"){
                speak("Okay, order canceled","canceled");
                animateTxt(txtlisten, "Order canceled.");
                hasOrdered = false;
                updateStatus("Waiting for 'hey Iris'", false);
                startListening(KWS_SEARCH);
            } else {
                speak("Sorry I didn't catch that, can you say that again?", "failedtohear", true, true);
                animateTxt(txtlisten, "Didn't catch that, can you say that again?");
//                reprompt();
//                startListening(KWS_SEARCH);
            }
        }

        System.out.println("Got out of !hasordered if clause");
    }

    /**
     This method checks if the user speech contains an info-type message like 'allergies' or 'info'
     */
    public Boolean hasInfo(List<String> consumables, List<String> output) {
        startListening(KWS_SEARCH);
        List<String> generalinfolist = Arrays.asList("description", "information", "info", "about");
        List<String> price = Arrays.asList("price", "cost");
        List<String> allergies = Arrays.asList("allergy", "allergies", "allergic");

        String menuitem = GeneralTools.checkForWords(output, consumables);


        if (menuitem == "null"){
            // couldn't find a menuitem in the output
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
                // TODO
                speak(menuitem + " contains no potential allergy substances", "allergy_hasinfo");
                animateTxt(txtlisten, "No allergies inside " + menuitem);
                updateStatus("Waiting for 'hey Iris'", false);
//                backtomenu.setVisibility(View.VISIBLE);
            } else {
                showinfo(infotype, menuitem, menuitem + " ");
            }
            return true;
        }

        return false;
    }

    /**
     This method prompts the user for speech input
     */
    public void reprompt(){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                speech.startListening(intent);
            }
        });
    }

    @Override
    public void onPartialResults(Bundle bundle) {
    }

    @Override
    public void onEvent(int i, Bundle bundle) {

    }

    /**
     This initiates TextToSpeech
     */
    public void speak(String text, String uttID, boolean... r){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uttID);
        if (r.length != 0){
            i.speak(text, map, r);
        } else {
            i.speak(text, map);
        }
    }

    /**
     This shows information about the product
     */
    public void showinfo(String infotype, String menuitem, String... args){
        String prefix =  (args.length == 0) ? "" : args[0];
        String suffix = (args.length < 2) ? "" : args[1];
        String prefix_text = (args.length < 3) ? "" : args[2];

        if (suffix.equals("euros")){
            speak(prefix + GeneralTools.outputMoney(getInformation.showInformation(menuitem, infotype)), "euros");
        } else {
            speak(prefix + getInformation.showInformation(menuitem, infotype), "info1");
        }

        animateTxt(txtlisten, GeneralTools.capitalize(menuitem) + "\n\n" + prefix_text + getInformation.showInformation(menuitem, infotype));

        updateStatus("Waiting for 'hey Iris'", false);
    }

    /**
     This method updates the status bar found at the top of the application
     */
    public void updateStatus(String text, boolean withProgress){
        hasSaidHeyIris = false;
        status.setText(text);
//        setAlphaAnimation(status);
        if(!withProgress){
            progresslisten.setVisibility(View.INVISIBLE);
        } else {
            progresslisten.setVisibility(View.VISIBLE);
        }
    }

    /**
     If TTS has been succesful
     */
    @Override
    public void onSuccess(TextToSpeech tts) {
        this.talk = tts;
        flag = true;
//        startService(new Intent(this, TextToSpeechInitializer.class));
    }

    @Override
    public void onFinishedSpeaking() {
        System.out.println("Finished speaking.");
    }

    @Override
    public void onBeginSpeaking() {

    }

    @Override
    public void onFailure(TextToSpeech tts) {
        flag = false;
        finish();
    }

    @Override
    public void execReprompt() {
//        startListening(KWS_SEARCH);
        reprompt();
    }

    private void getMeal(List<String> typelist,  ArrayList<String> output){
        startListening(KWS_SEARCH);
        orderpairs.clear();
        Logic logic = new Logic(typelist, output);
        orderpairs = logic.generate();

        System.out.println("typelist: " + typelist);
        System.out.println("orderpairs: " + orderpairs);

        if(orderpairs.size() < 1){
            System.out.println("Get meal couldn't find anything");
            speak("I can't seem to figure out what you said, please try again.", "failed1", true, true);
            animateTxt(txtlisten, "I didn't quite catch that.");
            hasOrdered = false;
//            startListening(KWS_SEARCH);
        } else {
            Set<String> keys = orderpairs.keySet();

            String order = "Order:\n";
            String speakorder = "Your order consists of the following: ";

            for (String key : keys) {
//                System.out.println(orderpairs.get(key) + " " + key);
                speakorder += orderpairs.get(key) + " " + key;
                order += key + " | amount: " + orderpairs.get(key) + "\n";
            }

            final String order2 = order;

            speakorder += ". Confirm by saying yes";
            final String order1 = speakorder;


            speak(speakorder, "order", true, true);

            animateTxt(txtlisten, order2);

            System.out.println("ORDERPAIRS: " + orderpairs);

            updateStatus("Waiting for response..", true);
            hasOrdered = true;
//            orderstatus = true;

        }
    }


    // START CONTINUOUS SPEECH


    @Override
    public void onBeginningOfSpeech() {
//        updateStatus("Listening to your voice...", true);
    }

    @Override
    public void onRmsChanged(float v) {

    }

    @Override
    public void onBufferReceived(byte[] bytes) {

    }

    @Override
    public void onEndOfSpeech() {
//        if (hasSaidHeyIris)
//        updateStatus("Waiting for hey iris", false);
    }

    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null) {
            return;
        }

        if (!hasLeftBeginScreen)
            stopExamples();
            hasLeftBeginScreen = true;

        String text = hypothesis.getHypstr();
        System.out.println("THIS IS TEXT: " + text);
        if (text.equals(KEYPHRASE)) {
            hasSaidHeyIris = true;
            updateStatus("Processing...", true);
            try {
                recognizer.stop();
                speech.startListening(intent);
            } catch (ActivityNotFoundException a) {
                System.out.println("activity not found exception");
            }
        }
    }

    @Override
    public void onResult(Hypothesis hypothesis) {
        recognizer.stop();
    }

    @Override
    public void onError(Exception e) {

    }

    @Override
    public void onTimeout() {

    }

    private static class SetupTask extends AsyncTask<Void, Void, Exception> {
        WeakReference<MainActivity> activityReference;
        SetupTask(MainActivity activity) {
            this.activityReference = new WeakReference<>(activity);
        }
        @Override
        protected Exception doInBackground(Void... params) {
            try {
                Assets assets = new Assets(activityReference.get());
                File assetDir = assets.syncAssets();
                activityReference.get().setupRecognizer(assetDir);
            } catch (IOException e) {
                return e;
            }
            return null;
        }
        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.txtlisten))
                        .setText("Failed to init recognizer " + result);
            } else {
                activityReference.get().startListening(KWS_SEARCH);
            }
        }
    }

    public void startListening(String searchName){
        recognizer.stop();

        if (searchName.equals(KWS_SEARCH))
            recognizer.startListening(searchName);
        else
            recognizer.startListening(searchName, 1000);

    }

    private void setupRecognizer(File assetsDir) throws IOException {
        // The recognizer can be configured to perform multiple searches
        // of different kind and switch between them

        recognizer = SpeechRecognizerSetup.defaultSetup()
                .setAcousticModel(new File(assetsDir, "en-us-ptm"))
                .setDictionary(new File(assetsDir, "cmudict-en-us.dict"))
//                .setRawLogDir(assetsDir) // To disable logging of raw audio comment out this call (takes a lot of space on the device)
                .getRecognizer();
        recognizer.addListener((edu.cmu.pocketsphinx.RecognitionListener) this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new MainActivity.SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    public void getOrderSummary(){
        TextView orderTextView = (TextView) findViewById(R.id.orderTextView);
        orderTextView.setText("#order: " + Integer.toString(orderDataSingleton.getOrderID()));

        DecimalFormat df = new DecimalFormat("#.00");

        HashMap sum = orderProcessor.getOrderLines(orderDataSingleton.getOrderID());
        System.out.println("THIS IS SUM: " + sum);
        Set<String> keys = sum.keySet();


        double totalsum = orderProcessor.getOrderSum();
        System.out.println(totalsum);
        if (totalsum == 0){
            speak("No orders have been placed yet.", "nosummarypossible");
            animateTxt(txtlisten, "Nothing has been order just yet. Say 'hey Iris' to place an order.");
            System.out.println("is equal to 0");
            startListening(KWS_SEARCH);
        } else {
            TableLayout stk = (TableLayout) findViewById(R.id.table);
            TableRow tbrow0 = new TableRow(this);

            List<String> headers = Arrays.asList("Meal", "Unit Price", "Amount", "Total Sum");

            for (int i = 0; i < 4; i++) {
                TextView tableheaders = new TextView(this);
                tableheaders.setText(headers.get(i));
                tbrow0.addView(tableheaders);
            }

            stk.addView(tbrow0);

            for (String key : keys) {
                TableRow tbrow = new TableRow(this);
                TextView t2v = new TextView(this);
                t2v.setText("Product " + key);
                t2v.setGravity(Gravity.LEFT);
                tbrow.addView(t2v);
                TextView t3v = new TextView(this);
                t3v.setText("€" + getInformation.showInformation(key, "price"));
                t3v.setGravity(Gravity.CENTER);
                tbrow.addView(t3v);
                TextView t4v = new TextView(this);
                t4v.setText("" + sum.get(key));
                t4v.setGravity(Gravity.CENTER);
                tbrow.addView(t4v);
                TextView t1v = new TextView(this);
                Double calc = Double.parseDouble(getInformation.showInformation(key, "price")) * Double.parseDouble(sum.get(key).toString());
                t1v.setText("€" + df.format(calc).toString());
                t1v.setGravity(Gravity.CENTER);
                tbrow.addView(t1v);
                stk.addView(tbrow);
            }

            TableRow tbrow1 = new TableRow(this);
            for (int x = 0; x < 3; x++) {
                TextView tx = new TextView(this);
                tx.setText("");
                tbrow1.addView(tx);
            }

            TextView bill = new TextView(this);
            bill.setText("€" + df.format(totalsum));
            bill.setGravity(Gravity.CENTER);
            tbrow1.addView(bill);

            stk.addView(tbrow1);
            startListening(KWS_SEARCH);
            System.out.println(findViewById(android.R.id.content));
        }
    }

    public void startExamples(){

        System.out.println(voorbeeldzinnen[0]);
        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");

        TextView speech = (TextView) findViewById(R.id.example);
        speech.setTypeface(roboto, Typeface.ITALIC);
        System.out.println(speech.getTypeface());

        this.animator = new Fade(speech, voorbeeldzinnen, 6000);
        this.animator.startAnimation();
        this.animator.end();

    }

    public void stopExamples(){
        this.animator.end();
        TextView example = (TextView) findViewById(R.id.example);
        TextView exampletitle = (TextView) findViewById(R.id.exampletitle);
        example.setVisibility(View.GONE);
        exampletitle.setVisibility(View.GONE);
    }
}
