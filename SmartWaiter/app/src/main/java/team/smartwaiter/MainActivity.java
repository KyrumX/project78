package team.smartwaiter;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Locale;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Hypothesis;
import edu.cmu.pocketsphinx.RecognitionListener;
import edu.cmu.pocketsphinx.SpeechRecognizer;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;
import team.smartwaiter.TTS.TextToSpeechIniListener;
import team.smartwaiter.TTS.TextToSpeechInitializer;
import team.smartwaiter.api.ApiController;
import team.smartwaiter.storage.OrderDataSingleton;
import team.smartwaiter.tools.Fade;

import static team.smartwaiter.tools.GeneralTools.setAlphaAnimation;


public class MainActivity extends Activity implements
        RecognitionListener, TextToSpeechIniListener{

    public MainActivity() {
        orderDataSingleton.update();
    }

    private final ApiController API = new ApiController();
    public static TextView orderTextView;
    private TextView idTextView, txt;
    public static OrderDataSingleton orderDataSingleton = OrderDataSingleton.getInstance();

    private TextToSpeech talk;
    private TextToSpeechInitializer i;
    private boolean flag = false;

    /* Named searches allow to quickly reconfigure the decoder */
    private static final String KWS_SEARCH = "wakeup";
//    private static final String ORDER_SEARCH = "order";
//    private static final String MENU_SEARCH = "menu";

    /* Keyword we are looking for to activate menu */
    private static final String KEYPHRASE = "hey iris";

    private static final String OTHER = "back";

    private static ProgressBar progress;
    private static TextView introText;
    private static Button testbutton;
    //private static Button infobutton;

    private Intent in = new Intent(this, TextToSpeechInitializer.class);
    private String[] voorbeeldzinnen = new String[5];
    private HashMap<String, String> speakingprogress = new HashMap<String, String>();

    /* Used to handle permission request */
    private static final int PERMISSIONS_REQUEST_RECORD_AUDIO = 1;

    private SpeechRecognizer recognizer;
    private HashMap<String, Integer> captions;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);

        final TextToSpeechIniListener ini = this;

        i = new TextToSpeechInitializer(this, Locale.US, this);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                i = new TextToSpeechInitializer(getApplicationContext(), Locale.US, ini);
            }
        });

        voorbeeldzinnen = new String[]{getString(R.string.cap1), getString(R.string.cap2), getString(R.string.cap3), getString(R.string.cap4), getString(R.string.cap5)};

        orderTextView = (TextView) findViewById(R.id.orderTextView);
        orderTextView.setText("#order: " + Integer.toString(orderDataSingleton.getOrderID()));

        // Create InfoButton. When pressed / when hearing "help", it shows example commands.
        //infobutton = new Button(this);

        txt = (TextView) findViewById(R.id.result_text);
//        txt.setText("Starting text");

//        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");
//        txt.setTypeface(roboto);

        startExamples();

        progress = findViewById(R.id.progressBar);

//        // Prepare the data for UI
        captions = new HashMap<>();
        captions.put(KWS_SEARCH, R.string.kws_caption);

        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");

        introText = findViewById(R.id.caption_text);
        introText.setTypeface(roboto);

//        ((TextView) findViewById(R.id.caption_text))
//                .setText("Preparing the application");

        // Check if user has given permission to record audio
        int permissionCheck = ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO);
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, PERMISSIONS_REQUEST_RECORD_AUDIO);
            return;
        }
        // Recognizer initialization is a time-consuming and it involves IO,
        // so we execute it in async task
        new SetupTask(this).execute();
    }

    @Override
    public void onSuccess(TextToSpeech tts) {
        this.talk = tts;
        flag = true;
//        startService(in);
        this.talk.setOnUtteranceProgressListener(new UtteranceProgressListener() {
            @Override
            public void onStart(String s) {
                System.out.println("IS THIS WORKINGGGGGGGGGGGGGGGGGGGGGGGGGGGGGG");
                recognizer.stop();
            }

            @Override
            public void onDone(String s) {
                System.out.println("DoneDOINGIT_____________________________________________");
                recognizer.startListening(KWS_SEARCH);
            }

            @Override
            public void onError(String s) {
                System.out.println("Error");
            }
        });
        if (orderDataSingleton.isFirstLaunch())
            speak("Hi, my name is Iris. I will be your smart waitress today.", "main");
        else {
            speak("Please activate me when you have any questions or want to place an order. " +
                    "If you need any assistance, press the information icon or say: help.", "");
        }
    }

    @Override
    public void onFinishedSpeaking() {
        recognizer.startListening(KWS_SEARCH);
        System.out.println("on finished speaking");
    }

    @Override
    public void onBeginSpeaking() {
        recognizer.stop();
    }

    @Override
    public void onFailure(TextToSpeech tts) {
        flag = false;
        finish();
    }

    @Override
    public void execReprompt() {

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
            progress.setVisibility(View.GONE);
            introText.setText("Waiting for 'hey Iris'");
            setAlphaAnimation(introText);
            if (result != null) {
                ((TextView) activityReference.get().findViewById(R.id.caption_text))
                        .setText("Failed to init recognizer " + result);
            } else {
//                activityReference.get().switchSearch(KWS_SEARCH);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PERMISSIONS_REQUEST_RECORD_AUDIO) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recognizer initialization is a time-consuming and it involves IO,
                // so we execute it in async task
                new SetupTask(this).execute();
            } else {
                finish();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (recognizer != null) {
            recognizer.stop();
            recognizer.shutdown();
//            stopService()
        }
    }

    /**
     * In partial result we get quick updates about current hypothesis. In
     * keyword spotting mode we can react here, in other modes we need to wait
     * for final result in onResult.
     */
    @Override
    public void onPartialResult(Hypothesis hypothesis) {
        if (hypothesis == null)
            return;

        String text = hypothesis.getHypstr();
        System.out.println("THIS IS TEXT: " + text);
        if (text.equals(KEYPHRASE)) {
//            txt.setText("hey customer, how are you doing today?");
//            speak("hey customer, how can I help you?");
//            stopService(in);
            recognizer.stop();
            Intent intent = new Intent(this, ListenActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity ( intent );
            recognizer.shutdown();
            finish();
        }
    }

    /**
     * This callback is called when we stop the recognizer.
     */
    @Override
    public void onResult(Hypothesis hypothesis) {
        if (hypothesis != null) {

        }
    }

    @Override
    public void onBeginningOfSpeech() {

    }

    /**
     * We stop recognizer here to get a final result
     */
    @Override
    public void onEndOfSpeech() {
//        if (!recognizer.getSearchName().equals(KWS_SEARCH))
//            switchSearch(KWS_SEARCH);
    }

    private void switchSearch(String searchName) {
        recognizer.stop();

        // If we are not spotting, start listening with timeout (10000 ms or 10 seconds).
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
        recognizer.addListener(this);

        /* In your application you might not need to add all those searches.
          They are added here for demonstration. You can leave just one.
         */

        // Create keyword-activation search.
        recognizer.addKeyphraseSearch(KWS_SEARCH, KEYPHRASE);

    }

    @Override
    public void onError(Exception error) {
        ((TextView) findViewById(R.id.caption_text)).setText(error.getMessage());
    }

    @Override
    public void onTimeout() {
        switchSearch(KWS_SEARCH);
    }

    //Aarons API tester button :)
    public void TestLogger(View v) {
        API.Print();
    }

    public void startExamples(){

        System.out.println(voorbeeldzinnen[0]);
        Typeface roboto = Typeface.createFromAsset(getAssets(), "fonts/roboto.ttf");

        TextView speech = (TextView) findViewById(R.id.example);
        speech.setTypeface(roboto, Typeface.ITALIC);
        System.out.println(speech.getTypeface());

        Fade animator = new Fade(speech, voorbeeldzinnen, 6000);
        animator.startAnimation();
    }
    public void speak(String text, String uttID){
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, uttID);
//        talk.speak(text, TextToSpeech.QUEUE_FLUSH, map);
        i.speak(text, map);
    }

}
