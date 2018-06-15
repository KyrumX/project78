package team.smartwaiter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import team.smartwaiter.TTS.TextToSpeechIniListener;
import team.smartwaiter.TTS.TextToSpeechInitializer;
import team.smartwaiter.api.ApiController;
import team.smartwaiter.api.MenuProcessor;
import team.smartwaiter.api.OrderProcessor;
import team.smartwaiter.api.Serializer;
import team.smartwaiter.tools.GeneralTools;
import team.smartwaiter.tools.TypeWriter;

import static team.smartwaiter.MainActivity.orderDataSingleton;
import static team.smartwaiter.tools.GeneralTools.animateTxt;
import static team.smartwaiter.tools.GeneralTools.setAlphaAnimation;

public class ListenActivity extends Activity implements RecognitionListener, TextToSpeechIniListener {
    private TextToSpeechInitializer i;
    private TextToSpeech talk;

    public ListenActivity() {
        orderDataSingleton.setFirstLaunch(false);
    }

    private TextView status;
    public static TypeWriter txtlisten;
    private ProgressBar progresslisten;
    public static Handler handler = new Handler();

    private static boolean hasOrdered = false;
    private boolean flag = false;
    private boolean orderstatus = false;

    private Button reorder;
    private Button backtomenu;
    final SpeechRecognizer speech = SpeechRecognizer.createSpeechRecognizer(this);
    final Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
    private static Intent intentmenu;
    private boolean hasEnteredInfo = false;

    private final int REQ_CODE_SPEECH_INPUT = 100;

    //VARS for processing orders
    HashMap orderpairs = new HashMap();
    ApiController controller = new ApiController();
    OrderProcessor orderProcessor = new OrderProcessor();
    MenuProcessor menuProcessor = new MenuProcessor();

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



        txtlisten = (TypeWriter) findViewById(R.id.txtlisten);



        speech.setRecognitionListener(this);

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);


//        Voor Nederlands, gebruik onderstaande code. Apparaat moet wel ingesteld zijn op Nederlands.
//        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());

        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");

        status = findViewById(R.id.status);
        progresslisten = findViewById(R.id.progresslisten);

        TextView orderTextView = (TextView) findViewById(R.id.orderTextView);
        orderTextView.setText("#order: " + Integer.toString(orderDataSingleton.getOrderID()));


    }

    @Override
    protected void onResume() {
        super.onResume();

        final TextToSpeechIniListener ini = this;

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                i = new TextToSpeechInitializer(getApplicationContext(), Locale.US, ini);
            }
        });


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
        status.setText("Processing..");
    }

    @Override
    public void onError(int i) {
        animateTxt(txtlisten, "I didn't quite catch that..");
        reorder.setVisibility(View.VISIBLE);
        updateStatus("Waiting for command..", false);
    }

    @Override
    public void onResults(Bundle results) {

        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

        final String output = matches.get(0).toLowerCase();
        System.out.println("Output: " + output);

        //List<String> food = Arrays.asList("burger", "rice", "spaghetti", "mixed grill", "soup", "steak", "salad", "macaroni");
        List<String> menu;
        List<String> summarylist = Arrays.asList("overview", "summary");
        menu = Serializer.convertMenu(controller.getMenu(), "name");


        if (GeneralTools.checkForWords(matches, summarylist) != "null") {
            setContentView(R.layout.show_summary);
            System.out.println("SHOWING SUMMARY----------------------");
            getOrderSummary();

            // if no order has been placed yet
        } else if (!hasOrdered) {


            // if it doesn't have keywords for information e.g. 'allergies' or 'information'
            if (!hasInfo(menu, matches)) {
                getMeal(menu, matches);
//                speak("I can't seem to figure out what you said, please try again.", "nomenuitem_hasinfo", true);
                System.out.println("No infotype gotten so doing getMeal()");

            }
            if (getInvoicePrice(matches)) {
                System.out.println("total price returned");}
            if(getGoesWellWith(matches, menu)){
                System.out.println("List returned");
            }

        }
        else {
            // if an order has been placed, this will confirm or cancel
            List<String> confirmationlist = Arrays.asList("yes", "yeah", "sure", "alright", "okay", "affirmative");
            List<String> denylist = Arrays.asList("no", "nope", "nah", "not", "cancel");

            // checks relation between the strings above and the output gotten from user speech
            if (GeneralTools.checkForWords(matches, confirmationlist) != "null"){
                reorder.setVisibility(View.VISIBLE);
                speak("Order confirmed.", "confirmed");

                //Now that one orderline has been confirmed (e.g. 2 cola's) we need to push it to the db
                orderProcessor.createNewOrderLine(orderpairs); // <-- Process all the ordered items
                //OrderLines have been processed.

                animateTxt(txtlisten, "Order confirmed.");

                // this updates the status bar at the top of the application
                updateStatus("Waiting for command..", false);
            } else if(GeneralTools.checkForWords(matches, denylist) != "null") {
//                reorder.setVisibility(View.VISIBLE);
//                animateTxt(txtlisten, "Order canceled.");
                hasOrdered = false;
//                updateStatus("Waiting for command..", false);

                goBackToMain();

                speak("Okay, order canceled", "canceled");

            } else {

                    speak("Sorry I didn't catch that, can you say that again?", "failedtohear", true);
                    animateTxt(txtlisten, "Didn't catch that, can you say that again?");
                    reprompt();

            }
        }

        System.out.println("Got out of !hasordered if clause");
    }

    /**
     This method checks if the user speech contains an info-type message like 'allergies' or 'info'
     */
    public Boolean hasInfo(List<String> consumables, List<String> output) {
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
                updateStatus("Waiting for command..", false);
                backtomenu.setVisibility(View.VISIBLE);
            } else {
                showinfo(infotype, menuitem, menuitem + " ");
            }
            return true;
        }

        return false;
    }

    public Boolean getInvoicePrice(List<String> output) {
        List<String> generalpricelist = Arrays.asList("bill", "invoice", "pay", "check");

        if (GeneralTools.checkForWords(output, generalpricelist) != "null") {
                    double sum = orderProcessor.getOrderSum();
                    String totalprice = "The total price is " + sum + " euros. Please go to the cash register, a waiter will be waiting for you there.";
                    speak(totalprice, "totalprice", true);
                    animateTxt(txtlisten, "Total: " + sum + " - A waiter will await you at the cash register.");
                    return true;
                }



        return false;
    }

    public Boolean getGoesWellWith(List<String> output, List<String> menu){
        List<String> goeswellwithlist = Arrays.asList("combine", "advice");
        String outputitem = GeneralTools.checkForWords(output, menu);
        String menuitem = outputitem.toLowerCase();
        String speakitems = "";
        if (GeneralTools.checkForWords(output, goeswellwithlist)  != "null") {
            if(menuitem.toLowerCase() != "null"){
                int id = orderProcessor.linkNameWithID(menuitem);
                ArrayList<String> listofitems = menuProcessor.goesWellWith(id);
                for(String item : listofitems) {
                    if(item.equals("None")) {
                        speak("I can not give you advice on" + menuitem, "emptyadvicelist" , true);
                        animateTxt(txtlisten, "I can not give you advice on " + menuitem);
                    }else{
                        speakitems += item;
                        speakitems += ", ";
                        speak("We advice the following items with " + menuitem + ": " + speakitems, "advice", true);
                        animateTxt(txtlisten, "We advice the following items with " + menuitem + ": " + speakitems);
                    }
                }
                return true;
            }else{
                speak("Im sorry but i can't give you advice on that", "noadvice", true);
                animateTxt(txtlisten, "Im sorry but i can't give you advice on that");
            }


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
            i.speak(text, map, r[0]);
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

        backtomenu.setVisibility(View.VISIBLE);

        if (suffix.equals("euros")){
            speak(prefix + GeneralTools.outputMoney(getInformation.showInformation(menuitem, infotype)), "euros");
        } else {
            speak(prefix + getInformation.showInformation(menuitem, infotype), "info1");
        }

        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println(e);
        }
        animateTxt(txtlisten, GeneralTools.capitalize(menuitem) + "\n\n" + prefix_text + getInformation.showInformation(menuitem, infotype));

        updateStatus("Waiting for command..", false);
    }

    /**
     This method updates the status bar found at the top of the application
     */
    public void updateStatus(final String text, final boolean withProgress){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                status.setText(text);
                setAlphaAnimation(status);
                if(!withProgress){
                    progresslisten.setVisibility(View.INVISIBLE);
                } else {
                    progresslisten.setVisibility(View.VISIBLE);
                }
            }
        });
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
    protected void onDestroy() {
        super.onDestroy();
        System.out.println("trying to destroy");
        if (speech != null){
            speech.destroy();
        }
    }

    @Override
    public void execReprompt() {
        reprompt();
    }

    public void goBackToMain(){
        Intent myIntent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        speech.destroy();
        startActivity(myIntent);
        finish();
        overridePendingTransition (0, 0);

    }

    private void getMeal(List<String> typelist, ArrayList<String> output){
        orderpairs.clear();
        Logic logic = new Logic(typelist, output);
        orderpairs = logic.generate();

        System.out.println("typelist: " + typelist);
        System.out.println("orderpairs: " + orderpairs);

        if(orderpairs.size() < 1){
            System.out.println("Get meal couldn't find anything");
            speak("I can't seem to figure out what you said, please try again.", "failed1", true);
            animateTxt(txtlisten, "I didn't quite catch that.");
//            orderstatus = true;
            hasOrdered = false;
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


            speak(speakorder, "order", true);

            animateTxt(txtlisten, order2);

            System.out.println("ORDERPAIRS: " + orderpairs);

            //TODO: Ralph -->
            System.out.println("SUM: " + orderProcessor.getOrderSum()); // <-- Returns a double of total sum
            System.out.println("GOESWELLWITH: " + menuProcessor.goesWellWith(1)); // <-- Returns an arraylist containing all items that go well with <id>

            updateStatus("Waiting for response..", true);
            hasOrdered = true;
//            orderstatus = true;

        }
    }

    public void getOrderSummary(){
        DecimalFormat df = new DecimalFormat("#.00");
        backtomenu = findViewById(R.id.button3);

        backtomenu.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                goBackToMain();
            }
        });

        HashMap sum = orderProcessor.getOrderLines(orderDataSingleton.getOrderID());
        System.out.println("THIS IS SUM: " + sum);
        Set<String> keys = sum.keySet();


        double totalsum = orderProcessor.getOrderSum();

        for (String key : keys) {
            System.out.println("KEY ---------------");
            System.out.println(sum.get(key) + " " + key);
            System.out.println(" ---------------ENDKEY");
        }


        TableLayout stk = (TableLayout) findViewById(R.id.table);
        TableRow tbrow0 = new TableRow(this);
        TextView tv1 = new TextView(this);
        tv1.setText(" Meal ");
        tbrow0.addView(tv1);
        TextView tv2 = new TextView(this);
        tv2.setText(" Unit Price ");
        tbrow0.addView(tv2);
        TextView tv3 = new TextView(this);
        tv3.setText(" Amount ");
        tbrow0.addView(tv3);
        TextView tv0 = new TextView(this);
        tv0.setText(" Total Sum ");
        tbrow0.addView(tv0);
        stk.addView(tbrow0);
        for (String key : keys) {
            TableRow tbrow = new TableRow(this);
            TextView t2v = new TextView(this);
            t2v.setText("Product " + key);
            t2v.setGravity(Gravity.LEFT);
            tbrow.addView(t2v);
            TextView t3v = new TextView(this);
            t3v.setText("€" + getInformation.showInformation(key,"price"));
            t3v.setGravity(Gravity.CENTER);
            tbrow.addView(t3v);
            TextView t4v = new TextView(this);
            t4v.setText("" + sum.get(key));
            t4v.setGravity(Gravity.CENTER);
            tbrow.addView(t4v);
            TextView t1v = new TextView(this);
            Double calc = Double.parseDouble(getInformation.showInformation(key,"price")) * Double.parseDouble(sum.get(key).toString());
            t1v.setText("€" + df.format(calc).toString());
            t1v.setGravity(Gravity.CENTER);
            tbrow.addView(t1v);
            stk.addView(tbrow);
        }

        TableRow tbrow1 = new TableRow(this);
        for (int x = 0; x < 3; x++){
            TextView tx = new TextView(this);
            tx.setText("");
            tbrow1.addView(tx);
        }

        TextView bill = new TextView(this);
        bill.setText("€" + df.format(totalsum));
        bill.setGravity(Gravity.CENTER);
        tbrow1.addView(bill);

        stk.addView(tbrow1);

    }

}
