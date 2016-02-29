package com.aggregate.tts.yandex;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by morfeusys on 29.02.16.
 */
public class YandexTextToSpeech extends AbstractVerticle {
    private static final String URL = "https://tts.voicetech.yandex.net/generate?format=mp3&lang=ru-RU";

    @Override
    public void start(Future<Void> f) throws Exception {
        String key = config().getString("key");
        String speaker = config().getString("speaker", "omazh");
        if (key == null || key.isEmpty()) {
            f.fail("Define your key in the conf file");
            return;
        }
        vertx.eventBus().consumer("tts.say", msg -> {
            try {
                msg.reply(URL + "&key=" + key + "&speaker=" + speaker
                        + "&text=" + URLEncoder.encode(msg.body().toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
            }
        });
    }
}
