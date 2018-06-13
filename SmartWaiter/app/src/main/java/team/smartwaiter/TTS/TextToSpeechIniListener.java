package team.smartwaiter.TTS;

import android.speech.tts.TextToSpeech;

public interface TextToSpeechIniListener {

    public void onSuccess(TextToSpeech tts);

    public void onFinishedSpeaking();

    public void onBeginSpeaking();

    public void onFailure(TextToSpeech tts);

    public void execReprompt();
}
