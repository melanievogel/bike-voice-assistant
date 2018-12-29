package ai.snips.snipsdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
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

import org.w3c.dom.Text;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadLocalRandom;

import ai.snips.hermes.InjectionKind;
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
import static android.media.MediaRecorder.AudioSource.MIC;
import static java.util.Collections.indexOfSubList;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ensurePermissions();

        mTTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = mTTS.setLanguage(Locale.GERMAN);

                    if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "Language not supported");
                    }else {
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

                    timer();

                }
            }
        });
    }

    private void timer(){
        new Timer().schedule(new TimerTask() {
            public void run () {
                speak();
            }
        }, 20000, 20000);
    }


    private void speak(){
        mTTS.speak("Hallo Johannes hier kommt eine Gefahrenmeldung", TextToSpeech.QUEUE_FLUSH, null,null);
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
                    Log.d(TAG, "received an intent: " + intentMessage);
                    // Do your magic here :D

                    // For now, lets just use a random sentence to tell the user we understood but
                    // don't know what to do

                    List<String> answers = Arrays.asList(
                            "This is only a demo app. I understood you but I don't know how to do that",
                            "Can you teach me how to do that?",
                            "Oops! This action has not be coded yet!",
                            "Yes Master! ... hum, ..., er, ... imagine this as been done",
                            "Let's pretend I've done it! OK?");


                    int index = Math.abs(ThreadLocalRandom.current().nextInt()) % answers.size();
                    client.endSession(intentMessage.getSessionId(), answers.get(index));
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

    private volatile boolean continueStreaming = true;

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


    @Override
    protected void onResume() {
        super.onResume();
        if (client != null) {
            startStreaming();
            client.resume();
        }
    }

    @Override
    protected void onPause() {
        continueStreaming = false;
        if (client != null) {
            client.pause();
        }
        super.onPause();
    }
}
