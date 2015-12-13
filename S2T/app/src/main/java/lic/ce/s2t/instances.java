package lic.ce.s2t;

import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.speech.RecognitionListener;
import android.app.Activity;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.view.MenuInflater;

import java.util.Arrays;
import java.util.Properties;
import java.io.IOException;
import java.io.FileOutputStream;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.content.Intent;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.media.AudioManager;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;

import java.util.ArrayList;

public class instances extends Activity implements RecognitionListener{


    private TextView returnedText, statusText;
    private ToggleButton toggleButton;
    private ProgressBar progressBar;
    ListView List;
    private SpeechRecognizer speech = null;
    public ArrayList<String> matches;
    public ArrayAdapter<String> arrayAdapter;
    private Intent recognizerIntent;
    private String LOG_TAG = "S2T Log";
    private ImageView I1,I2;

    public void onSaveInstanceState(Bundle savedState) {

        super.onSaveInstanceState(savedState);

        // Note: getValues() is a method in your ArrayAdaptor subclass
        if (matches!=null){
            String[] values = Arrays.copyOf(matches.toArray(),matches.toArray().length,String[].class);
            savedState.putStringArray("listview", values);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instances);
        I1 = (ImageView) findViewById(R.id.save_icon);
        I2 = (ImageView) findViewById(R.id.about_icon);
        returnedText = (TextView) findViewById(R.id.recognizedText);
        statusText = (TextView) findViewById(R.id.recognizerStatus);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        List=(ListView) findViewById(R.id.listView);

        I1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), exports.class);
                i.putExtra("text", matches.get(0).toString());
                startActivityForResult(i, 100); // 100 is some code to identify the returning result
            }
        });

        I2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MessageBox("S2T\nSpeech to Text - CE Lab\n\nHaz click en Encender y espera a que el estado se torne 'Escuchando...'. Toma la muestra de audio en ese momento y espera a que S2T realice la transcripción.");
            }
        });
        if (savedInstanceState != null) {
            String[] values = savedInstanceState.getStringArray("listview");
            matches = new ArrayList<String> (Arrays.asList(values));
            if (values != null) {
                arrayAdapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,
                        matches);
                List.setAdapter(arrayAdapter);
            }
        }

        List.setOnItemClickListener(new OnItemClickListener(){
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id){
                String data=arrayAdapter.getItem((int)id);
                List.clearChoices();
                arrayAdapter.clear();
                arrayAdapter.add(data);
                List.setAdapter(arrayAdapter);
            }
        });
        // Hay archivo de configuracion?
        if (ExistsPropertiesFile()){
            // Si hay, entonces nada.

        }else{
        // Si no hay, entonces:
            CreatePropertiesFile(this);
        }

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

    @Override
    public void onBufferReceived(byte[] buffer) {
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
        matches = results
                .getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        arrayAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_list_item_1,
                matches );

        List.setAdapter(arrayAdapter);
        MessageBox(this.getString(R.string.select_better));
        //showText(text);
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
                message = "No se encontró una coincidencia exacta.";
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
        //getMenuInflater().inflate(R.menu.menu_instances, menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_instances, menu);
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
            Intent i = new Intent(getApplicationContext(), exports.class);
            startActivityForResult(i, 100); // 100 is some code to identify the returning result
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


    private boolean ExistsPropertiesFile(){
        return true;
    }
    private void CreatePropertiesFile(Context context) {
        Properties prop = new Properties();
        String propertiesPath = context.getFilesDir().getPath().toString() + "/app.properties";
        try {
            FileOutputStream out = new FileOutputStream(propertiesPath);
            prop.setProperty("HomeVersion", "0");
            prop.setProperty("DatePlaySquare", "0");
            prop.setProperty("CustomerID", "0");
            prop.setProperty("DeviceToken", "0");
            prop.setProperty("CurrentVersionMobile", "0");
            prop.setProperty("Domain", "Megazy");
            prop.setProperty("DownloadNewVersion", "0");
            prop.store(out, null);
            out.close();
        } catch (IOException e) {
            System.err.println("Failed to open app.properties file");
            e.printStackTrace();
        }
    }
    public void MessageBox(String message){
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
