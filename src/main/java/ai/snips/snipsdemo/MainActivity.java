package ai.snips.snipsdemo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.os.Bundle;
import android.os.Environment;
import android.os.Process;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ai.snips.hermes.InjectionOperation;
import ai.snips.hermes.InjectionRequestMessage;
import ai.snips.hermes.IntentMessage;
import ai.snips.hermes.SessionEndedMessage;
import ai.snips.hermes.SessionQueuedMessage;
import ai.snips.hermes.SessionStartedMessage;
import ai.snips.platform.SnipsPlatformClient;
import ai.snips.platform.SnipsPlatformClient.SnipsPlatformError;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import kotlin.jvm.functions.Function1;

import static ai.snips.hermes.InjectionKind.Add;
import static ai.snips.snipsdemo.DangerZoneActivity.PI_RAD;
import static android.media.MediaRecorder.AudioSource.MIC;
import static android.speech.tts.TextToSpeech.QUEUE_FLUSH;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.util.Collections.singletonList;

public class MainActivity extends AppCompatActivity {

    private static final int AUDIO_ECHO_REQUEST = 0;
    private static final String TAG = "MainActivity";

    private static final int FREQUENCY = 16_000;
    private static final int CHANNEL = AudioFormat.CHANNEL_IN_MONO;
    private static final int ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private SnipsPlatformClient client;

    private AudioRecord recorder;

    private TextToSpeech mTTS;
    private volatile boolean continueStreaming = true;
    private LocationManager m;
    private LocationListener l;
    private double lati;
    private double longi;
    private String p;
   /* private void timer(){
        new Timer().schedule(new TimerTask() {
            public void run () {
                speak();
            }
        }, 20000, 20000);
    }
*/

/*
    private void speak(){
        mTTS.speak("Hallo Johannes hier kommt eine Gefahrenmeldung", TextToSpeech.QUEUE_FLUSH, null,null);
    }
*/

    @SuppressLint("MissingPermission")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ensurePermissions();
        if (!new File(getApplicationContext().getFilesDir() + "/zones.bike").exists()) {
            write(getApplicationContext().getFilesDir() + "/zones.bike", null);
        }
        m = (LocationManager) getSystemService(LOCATION_SERVICE);
        doIt();
        m.requestLocationUpdates(p, 0, (float) 5, l);
        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.ENGLISH);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    } else {
                        Log.e("TTS", "working");
                    }
                } else {
                    Log.e("TTS", "Initialization failed");
                }
            }
        });

        findViewById(R.id.start).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ensurePermissions()) {
                    final Button button = (Button) findViewById(R.id.start);
                    button.setEnabled(false);
                    button.setText(R.string.loading);

                    final View scrollView = findViewById(R.id.scrollView);
                    scrollView.setVisibility(View.GONE);

                    final View loadingPanel = findViewById(R.id.loadingPanel);
                    loadingPanel.setVisibility(View.INVISIBLE);

                    startMegazordService();

                    //timer();

                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (client != null) {
            client.disconnect();
        }
        super.onDestroy();
    }

    private boolean ensurePermissions() {
        int status = ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.RECORD_AUDIO);
        if (status != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE
            }, AUDIO_ECHO_REQUEST);
            return false;
        }
        return true;
    }

    private void startMegazordService() {
        if (client == null) {
            // a dir where the assistant models was unziped. It should contain the folders
            // custom_asr, custom_dialogue, custom_hotword and nlu_engine
            File assistantDir = new File(Environment.getExternalStorageDirectory()
                    .toString(), "snips_android_assistant_j");

            client = new SnipsPlatformClient.Builder(assistantDir)
                    .enableDialogue(true) // defaults to true
                    .enableHotword(true) // defaults to true
                    .enableSnipsWatchHtml(true) // defaults to false
                    .enableLogs(true) // defaults to false
                    .withHotwordSensitivity(0.5f) // defaults to 0.5
                    .enableStreaming(true) // defaults to false
                    .enableInjection(true) // defaults to false
                    .build();

            client.setOnPlatformReady(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                            findViewById(R.id.scrollView).setVisibility(View.VISIBLE);

                            final Button button = findViewById(R.id.start);
                            button.setEnabled(true);
                            //Before string: Start a dialogue session - means that no hotword is needed to trigger the listening
                            button.setText(R.string.startVoice);
                            button.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    // programmatically start a dialogue session
                                    client.startSession(null, new ArrayList<String>(),
                                            false, null);
                                }
                            });
                            button.setOnLongClickListener(new OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    // inject new values in the "house_room" entity
                                    HashMap<String, List<String>> values = new HashMap<>();
                                    values.put("house_room", Arrays.asList("bunker", "batcave"));
                                    InjectionOperation op = new InjectionOperation(Add, values);
                                    client.requestInjection(new InjectionRequestMessage(
                                            singletonList(op),
                                            Collections.<String, List<String>>emptyMap(),
                                            null, null));

                                    return true;
                                }
                            });
                        }
                    });
                    return null;
                }
            });

            client.setOnPlatformError(new Function1<SnipsPlatformError, Unit>() {

                @Override
                public Unit invoke(SnipsPlatformError snipsPlatformError) {
                    findViewById(R.id.loadingPanel).setVisibility(View.VISIBLE);
                    findViewById(R.id.scrollView).setVisibility(View.GONE);

                    final Button button = findViewById(R.id.start);
                    button.setEnabled(false);
                    return null;
                }
            });


            client.setOnHotwordDetectedListener(new Function0<Unit>() {
                @Override
                public Unit invoke() {
                    Log.d(TAG, "an hotword was detected !");
                    // Do your magic here :D
                    return null;
                }
            });

            client.setOnIntentDetectedListener(new Function1<IntentMessage, Unit>() {
                @Override
                public Unit invoke(IntentMessage intentMessage) {
                    String answer;
                    Log.d(TAG, "received an intent: " + intentMessage);
                    // Do your magic here :D

                    // For now, lets just use a random sentence to tell the user we understood but
                    // don't know what to do

                   /* List<String> answers = Arrays.asList(
                            "This is only a demo app. I understood you but I don't know how to do that",
                            "Can you teach me how to do that?",
                            "Oops! This action has not be coded yet!",
                            "Yes Master! ... hum, ..., er, ... imagine this as been done",
                            "Let's pretend I've done it! OK?");
*/
                    switch (intentMessage.getIntent().getIntentName()) {
                        case "melanievogel:AskForMountain":
                            answer = CheckForDz();
                            break;
                        default:
                            answer = "I don't know how to do that";
                    }
                    //int index = Math.abs(ThreadLocalRandom.current().nextInt()) % answers.size();
                    client.endSession(intentMessage.getSessionId(), answer);
                    return null;
                }
            });

            client.setOnListeningStateChangedListener(new Function1<Boolean, Unit>() {
                @Override
                public Unit invoke(Boolean isListening) {
                    Log.d(TAG, "asr listening state: " + isListening);
                    // Do you magic here :D
                    return null;
                }
            });

            client.setOnSessionStartedListener(new Function1<SessionStartedMessage, Unit>() {
                @Override
                public Unit invoke(SessionStartedMessage sessionStartedMessage) {
                    Log.d(TAG, "dialogue session started: " + sessionStartedMessage);
                    return null;
                }
            });

            client.setOnSessionQueuedListener(new Function1<SessionQueuedMessage, Unit>() {
                @Override
                public Unit invoke(SessionQueuedMessage sessionQueuedMessage) {
                    Log.d(TAG, "dialogue session queued: " + sessionQueuedMessage);
                    return null;
                }
            });

            client.setOnSessionEndedListener(new Function1<SessionEndedMessage, Unit>() {
                @Override
                public Unit invoke(SessionEndedMessage sessionEndedMessage) {
                    Log.d(TAG, "dialogue session ended: " + sessionEndedMessage);
                    return null;
                }
            });

            // This api is really for debugging purposes and you should not have features depending
            // on its output. If you need us to expose more APIs please do ask !
            client.setOnSnipsWatchListener(new Function1<String, Unit>() {
                public Unit invoke(final String s) {
                    runOnUiThread(new Runnable() {
                        public void run() {
                            // We enabled html logs in the builder, hence the fromHtml. If you only
                            // log to the console, or don't want colors to be displayed, do not
                            // enable the option
                            ((EditText) findViewById(R.id.text)).append(Html.fromHtml(s + "<br />"));
                            findViewById(R.id.scrollView).post(new Runnable() {
                                @Override
                                public void run() {
                                    ((ScrollView) findViewById(R.id.scrollView)).fullScroll(View.FOCUS_DOWN);
                                }
                            });
                        }
                    });
                    return null;
                }
            });

            // We enabled steaming in the builder, so we need to provide the platform an audio
            // stream. If you don't want to manage the audio stream do no enable the option, and the
            // snips platform will grab the mic by itself
            startStreaming();

            client.connect(this.getApplicationContext());
        }
    }

    private void startStreaming() {
        continueStreaming = true;
        new Thread() {
            public void run() {
                android.os.Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                runStreaming();
            }
        }.start();
    }

    private void runStreaming() {
        Log.d(TAG, "starting audio streaming");
        final int minBufferSizeInBytes = AudioRecord.getMinBufferSize(FREQUENCY, CHANNEL, ENCODING);
        Log.d(TAG, "minBufferSizeInBytes: " + minBufferSizeInBytes);

        recorder = new AudioRecord(MIC, FREQUENCY, CHANNEL, ENCODING, minBufferSizeInBytes);
        recorder.startRecording();

        while (continueStreaming) {
            short[] buffer = new short[minBufferSizeInBytes / 2];
            recorder.read(buffer, 0, buffer.length);
            if (client != null) {
                client.sendAudioBuffer(buffer);
            }
        }
        recorder.stop();
        Log.d(TAG, "audio streaming stopped");
    }


    @SuppressLint("MissingPermission")
    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            startStreaming();
            client.resume();
        }
        m.requestLocationUpdates(p, 0, (float) 5, l);

    }

    @Override
    protected void onPause() {
        continueStreaming = false;
        if (client != null) {
            client.pause();
        }
        super.onPause();
        m.removeUpdates(l);
    }

    public void write(String file, ArrayList<DangerZoneObject> myArrayList) {
        FileOutputStream out;
        if (myArrayList == null) {
        }
        if (new File(file).exists()) {
            myArrayList.addAll(read(file));
        }
        try {
            out = new FileOutputStream(file);
            for (int i = myArrayList.size() - 1; i > -1; i--) {
                String content = "name\n" + myArrayList.get(i).getName() + "\n" + myArrayList.get(i).getLati() + "\n" + myArrayList.get(i).getLongi() + "\n";
                out.write(content.getBytes());
            }
            out.close();
        } catch (Exception e) { //fehlende Permission oder sd an pc gemountet
            e.printStackTrace();
        }
    }

    public ArrayList<DangerZoneObject> read(String file) {
        ArrayList<DangerZoneObject> result = new ArrayList<>();
        try {
            BufferedReader buf = new BufferedReader(new FileReader(file));
            String line;
            while ((line = buf.readLine()) != null) {
                if (line.equals("name")) {
                    DangerZoneObject object = new DangerZoneObject("", 0.0, 0.0, "");
                    object.setName(buf.readLine());
                    object.setLati(Double.parseDouble(buf.readLine()));
                    object.setLongi(Double.parseDouble(buf.readLine()));
                    result.add(object);
                }
            }
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    @SuppressLint("MissingPermission")
    private void doIt() {
        // LocationManager-Instanz ermitteln
        m = getSystemService(LocationManager.class);
        if (m == null) {
            finish();
        }
        // Provider mit genauer Auflösung
        // und mittlerem Energieverbrauch
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE);
        criteria.setPowerRequirement(Criteria.POWER_MEDIUM);
        p = m.getBestProvider(criteria, true);
        // LocationListener-Objekt erzeugen
        l = new LocationListener() {
            @Override
            public void onStatusChanged(String provider, int status,
                                        Bundle extras) {
            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }

            @Override
            public void onLocationChanged(Location location) {
                String warning;
                Log.d(TAG, "Loc changed");
                lati = location.getLatitude();
                longi = location.getLongitude();
                warning = CheckForDz();
                if (!warning.equals("No there isn't")) {
                    mTTS.speak(warning, QUEUE_FLUSH, null, null);
                }

            }
        };
    }

    public double greatCircleInKilometers(double lat1, double long1, double lat2, double long2) {
        double phi1 = lat1 * PI_RAD;
        double phi2 = lat2 * PI_RAD;
        double lam1 = long1 * PI_RAD;
        double lam2 = long2 * PI_RAD;

        return 6371.01 * acos(sin(phi1) * sin(phi2) + cos(phi1) * cos(phi2) * cos(lam2 - lam1));
    }

    public String CheckForDz() {
        String answer = "There is a ";
        Log.d(TAG, "entered");
        ArrayList<DangerZoneObject> arrayList = new ArrayList<>();
        if (arrayList.size() == 0) {
            arrayList.addAll(read(getApplicationContext().getFilesDir() + "/zones.bike"));
            Log.d(TAG, String.valueOf(arrayList.size()));
        }
        for (DangerZoneObject dz : arrayList) {
            double distance = greatCircleInKilometers(lati, longi, dz.getLati(), dz.getLongi()) * 1000;
            Log.d(TAG, String.valueOf(distance));
            if (200.00 > distance) {
                if (!answer.equals("There is a ")) {
                    answer = answer + " and a ";
                }
                answer = answer + dz.getName() + " in " + (int) distance + " metres";
            }
        }
        if (answer.equals("There is a ")) {
            answer = "No there isn't";
        } else {
            answer = answer + " coming";
        }

        return answer;
    }

}
