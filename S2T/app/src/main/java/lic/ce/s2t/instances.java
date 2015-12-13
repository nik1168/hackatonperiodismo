package lic.ce.s2t;

import android.content.Context;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.app.Activity;
import android.speech.RecognitionService;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.RecognitionService.Callback;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;
import android.media.AudioManager;

import java.util.ArrayList;

public class instances extends Activity implements RecognitionListener{


    private TextView returnedText, statusText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    private SpeechRecognizer speech = null;
    private Intent recognizerIntent;
    private String LOG_TAG = "S2T Log";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instances);
        returnedText = (TextView) findViewById(R.id.recognizedText);
        statusText = (TextView) findViewById(R.id.recognizerStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //ToggleButton Encender:Apagar debe ser inverso. Cuando esta apagado, dice encendido y
        //viceversa
        toggleButton = (ToggleButton) findViewById(R.id.OnOff);
        toggleButton.setTextOn(this.getString(R.string.Off));
        toggleButton.setTextOff(this.getString(R.string.On));
        toggleButton.setText(this.getString(R.string.On));

        progressBar.setVisibility(View.INVISIBLE);
        speech = SpeechRecognizer.createSpeechRecognizer(this);
        speech.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,"20000");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS,"600000");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS,"10000");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE,"es");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_WEB_SEARCH);
        //recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3);

        toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView,
                                         boolean isChecked) {
                if (isChecked) {
                    progressBar.setVisibility(View.VISIBLE);
                    progressBar.setIndeterminate(true);
                    speech.startListening(recognizerIntent);
                    muteSound();
                } else {
                    progressBar.setIndeterminate(false);
                    progressBar.setVisibility(View.INVISIBLE);
                    speech.stopListening();
                    enableSound();
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (speech != null) {
            speech.destroy();
            Log.i(LOG_TAG, "destroy");
        }

    }

    @Override
    public void onBeginningOfSpeech() {
        Log.i(LOG_TAG, "onBeginningOfSpeech");

        setStatus(this.getString(R.string.listening));

        progressBar.setIndeterminate(false);
        progressBar.setMax(10);
    }


    byte[] sig = new byte[500000] ;
    int sigPos = 0 ;

    @Override
    public void onBufferReceived(byte[] buffer) {
        System.arraycopy(buffer, 0, sig, sigPos, buffer.length) ;
        sigPos += buffer.length ;
        Log.i(LOG_TAG, "onBufferReceived: " + buffer);
    }

    @Override
    public void onEndOfSpeech() {
        Log.i(LOG_TAG, "onEndOfSpeech");
        progressBar.setIndeterminate(true);
        setStatus(this.getString(R.string.idle));
        // BEWARE:
        toggleButton.setChecked(false);
    }

    @Override
    public void onError(int errorCode) {
        String errorMessage = getErrorText(errorCode);
        Log.d(LOG_TAG, "FAILED " + errorMessage);
        showText(errorMessage);
        toggleButton.setChecked(false);
    }

    @Override
    public void onEvent(int arg0, Bundle arg1) {
        Log.i(LOG_TAG, "onEvent");
    }

    @Override
    public void onPartialResults(Bundle arg0) {
        // not always work.
        Log.i(LOG_TAG, "onPartialResults");
    }

    @Override
    public void onReadyForSpeech(Bundle arg0) {
        setStatus(this.getString(R.string.listening));
        Log.i(LOG_TAG, "onReadyForSpeech");
    }

    @Override
    public void onResults(Bundle results) {
        Log.i(LOG_TAG, "onResults");
        ArrayList<String> matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        String text = "";

        for (String result : matches)
            text += result + "\n";

        showText(text);
        //returnedText.setText(text);
    }

    @Override
    public void onRmsChanged(float rmsdB) {
        //Log.i(LOG_TAG, "onRmsChanged: " + rmsdB);
        progressBar.setProgress((int) rmsdB);
    }

    public static String getErrorText(int errorCode) {
        String message;
        switch (errorCode) {
            case SpeechRecognizer.ERROR_AUDIO:
                message = "Error de grabación.";
                break;
            case SpeechRecognizer.ERROR_CLIENT:
                // hablaste suficientemente alto?
                message = "Error en la recepción.";
                break;
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                // permisos insuficientes:
                message = "La aplicación no cuenta con suficientes permisos.";
                break;
            case SpeechRecognizer.ERROR_NETWORK:
                // tienes internet?
                message = "Error de Red.";
                break;
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                // network timeout, tienes internet?
                message = "Tiempo de solicitud excedido.";
                break;
            case SpeechRecognizer.ERROR_NO_MATCH:
                // no match:
                message = "No se encontró una coincidencia.";
                break;
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                // El servicio esta ocupado. No hay una aplicación que este usandolo?
                message = "El servicio está ocupado.";
                break;
            case SpeechRecognizer.ERROR_SERVER:
                // error from server
                message = "El servidor arrojo un error.";
                break;
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                // No speech input. dijiste algo?
                message = "No se detectó ninguna conversación.";
                break;
            default:
                // No fue comprendido
                message = "No fue posible comprender la conversación, intenta de nuevo.";
                break;
        }
        return message;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_instances, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }



    // Local Functions

    private void enableSound(){
        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, false);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, false);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, false);
        amanager.setStreamMute(AudioManager.STREAM_RING, false);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, false);
    }

    private void muteSound(){
        AudioManager amanager=(AudioManager)getSystemService(Context.AUDIO_SERVICE);
        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
        amanager.setStreamMute(AudioManager.STREAM_RING, true);
        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
    }

    // Simple functions:
    private void setStatus(String x){
        statusText.setText(x);
    }

    private void showText(String x){
        returnedText.setText(x);
    }
}
