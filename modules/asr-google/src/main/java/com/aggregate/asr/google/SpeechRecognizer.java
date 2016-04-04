package com.aggregate.asr.google;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import javax.sound.sampled.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

public class SpeechRecognizer extends AbstractVerticle {
    private static Logger log = LoggerFactory.getLogger(SpeechRecognizer.class);

    private static final float SAMPLE_RATE = 16000;
    private static final int SAMPLE_SIZE = 16;
    private static final int CHANNELS = 1;

    private static final long MIN_LENGTH = 400;
    private static final long SILENCE_TIMEOUT = 1200;
    private static final long MIN_SPEECH = 1000;

    private static final String LANG = "ru-ru";

    private static final String URL = "https://www.google.com/speech-api/v2/recognize?output=json";

    private long myMinLength;
    private String myApiKey;
    private String myLang;
    private TargetDataLine myDataLine;

    private volatile boolean myListening;
    private volatile boolean myStop;
    private volatile boolean myError;
    private volatile boolean myCanceled;

    private long minSpeech;
    private long silenceTimeout;

    private volatile long startTimestamp;
    private volatile long silenceTimestamp;
    private volatile int silenceLevel;

    private AudioFormat format;
    private RecorderThread myRecorderThread;
    private RecognizerThread myRecognizerThread;

    @Override
    public void start(Future<Void> f) throws Exception {
        myApiKey = config().getString("key");
        myLang = config().getString("lang", LANG);
        myMinLength = config().getLong("length", MIN_LENGTH);
        minSpeech = config().getLong("minSpeech", MIN_SPEECH);
        silenceTimeout = config().getLong("silenceTimeout", SILENCE_TIMEOUT);
        if (myApiKey == null || myApiKey.isEmpty()) {
            f.fail("Provide your Google ASR API key");
            return;
        }
        format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, true, false);
        DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, format);
        Mixer.Info[] infos = AudioSystem.getMixerInfo();
        TargetDataLine dataLine = null;
        for (Mixer.Info info : infos) {
            Mixer mixer = AudioSystem.getMixer(info);
            try {
                dataLine = (TargetDataLine) mixer.getLine(dataLineInfo);
                break;
            } catch (Exception e) {
            }
        }
        if (dataLine == null) {
            f.fail("No data line is available");
            return;
        }
        myDataLine = dataLine;

        vertx.eventBus().consumer("asr.listen", e -> toggle());
        vertx.eventBus().consumer("asr.start", e -> listen());
        vertx.eventBus().consumer("asr.stop", e -> finish());
        vertx.eventBus().consumer("asr.cancel", e -> cancel());

        f.complete();
    }

    @Override
    public void stop() throws Exception {
        destroy();
    }

    public void destroy() {
        cancel();
        if (myDataLine.isOpen()) {
            myDataLine.close();
        }
    }

    public synchronized void toggle() {
        if (myListening && myStop) {
            finish();
        } else {
            listen();
            myStop = true;
        }
    }

    public synchronized void listen() {
        if (myListening) return;
        myListening = true;
        myStop = false;
        myError = false;
        myCanceled = false;
        ArrayBlockingQueue<byte[]> queue = new ArrayBlockingQueue<>(8192);
        myRecorderThread = new RecorderThread(queue);
        myRecognizerThread = new RecognizerThread(queue);
        myRecorderThread.start();
        myRecognizerThread.start();
    }

    public synchronized void finish() {
        myListening = false;
        if (myRecorderThread != null && myRecognizerThread != null) {
            myRecorderThread.finish();
            myRecognizerThread.finish();
            myRecorderThread = null;
            myRecognizerThread = null;
        }
    }

    public synchronized void cancel() {
        myCanceled = true;
        finish();
    }

    private void error(String error) {
        myError = true;
        vertx.eventBus().publish("asr.error", error);
    }

    private void onRmsCalculated(int rms) {
        long now = System.currentTimeMillis();
        if (startTimestamp == 0) startTimestamp = now;
        if (rms >= silenceLevel) {
            silenceLevel = rms;
            silenceTimestamp = now;
        } else if (now - startTimestamp > minSpeech) {
            if (now - silenceTimestamp > silenceTimeout && myRecorderThread != null) {
                finish();
            }
        }
    }


    private static int calculateRMSLevel(byte[] data) {
        long lSum = 0;
        for (byte aData1 : data) lSum = lSum + aData1;

        double dAvg = lSum / data.length;

        double sumMeanSquare = 0d;
        for (byte aData : data) sumMeanSquare = sumMeanSquare + Math.pow(aData - dAvg, 2d);

        double averageMeanSquare = sumMeanSquare / data.length;
        return (int)(Math.pow(averageMeanSquare, 0.5d) + 0.5);
    }


    private class RecorderThread extends Thread {
        private BlockingQueue<byte[]> queue;
        private byte[] buffer = new byte[1024];
        private volatile boolean stopped;

        private RecorderThread(BlockingQueue<byte[]> queue) {
            this.queue = queue;
        }

        private void finish() {
            stopped = true;
        }

        @Override
        public void run() {
            try {
                myDataLine.open(format);
                myDataLine.start();
                silenceLevel = 0;
                silenceTimestamp = 0;
                startTimestamp = 0;
                vertx.eventBus().publish("asr.state", "start");
                while (!stopped && !myError) {
                    record();
                }
            } catch (LineUnavailableException e) {
                log.error("Cannot access microphone", e);
            } finally {
                myDataLine.stop();
                myDataLine.close();
            }
            if (!myCanceled && !myError) {
                vertx.eventBus().publish("asr.state", "end");
            }
        }

        private void record() {
            int len = myDataLine.read(buffer, 0, buffer.length);
            if (len > 0) {
                queue.add(Arrays.copyOf(buffer, len));
                if (myStop) {
                    onRmsCalculated(calculateRMSLevel(buffer));
                }
            }
        }
    }

    private class RecognizerThread extends Thread {
        private ArrayBlockingQueue<byte[]> queue;
        private volatile boolean stopped;
        private OutputStream outputStream;

        private RecognizerThread(ArrayBlockingQueue<byte[]> queue) {
            this.queue = queue;
        }

        private void finish() {
            stopped = true;
        }

        private void flushQueue() throws IOException {
            ArrayList<byte[]> data = new ArrayList<>();
            queue.drainTo(data);
            for (byte[] buffer : data) {
                outputStream.write(buffer, 0, buffer.length);
            }
        }

        private HttpsURLConnection connect() throws Exception {
            vertx.eventBus().publish("asr.state", "rec");
            URL url = new URL(URL + "&key=" + myApiKey + "&lang=" + myLang);
            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "audio/l16; rate=16000;");
            outputStream = connection.getOutputStream();
            return connection;
        }

        private void stream() throws Exception {
            if (!stopped) flushQueue();
            while (!stopped) {
                byte[] buffer = queue.poll(10, TimeUnit.MILLISECONDS);
                if (buffer != null) {
                    outputStream.write(buffer, 0, buffer.length);
                }
            }
            flushQueue();
            outputStream.flush();
            vertx.eventBus().publish("asr.state", "done");
        }

        private List<String> parseResponse(InputStream inputStream) throws IOException {
            int len;
            byte[] buffer = new byte[1024];
            List<String> results = new ArrayList<>();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            while ((len = inputStream.read(buffer)) != -1) {
                stream.write(buffer, 0, len);
            }
            String response = new String(stream.toByteArray(), "UTF-8");
            String[] data = response.split("\\n");
            for (String json : data) {
                json = json.trim();
                if (json.isEmpty()) continue;
                JsonObject object = new JsonObject(json);
                JsonArray array = object.getJsonArray("result");
                for (int i = 0; i < array.size(); i++) {
                    JsonObject result = array.getJsonObject(i);
                    JsonArray alternatives = result.getJsonArray("alternative");
                    for (int j = 0; j < alternatives.size(); j++) {
                        JsonObject alternative = alternatives.getJsonObject(j);
                        results.add(alternative.getString("transcript"));
                    }
                }
            }
            return results;
        }

        @Override
        public void run() {
            HttpsURLConnection connection = null;
            try {
                Thread.sleep(myMinLength);
                if (stopped || myCanceled || myError) {
                    return;
                }
                connection = connect();
                stream();
                if (!myCanceled && !myError) {
                    List<String> results = parseResponse(connection.getInputStream());
                    String result = results.isEmpty() ? null : results.get(0);
                    vertx.eventBus().publish("asr.result", result);
                }
            } catch (Exception e) {
                log.error("Cannot recognize", e);
                error(e.getMessage());
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }
    }
}
