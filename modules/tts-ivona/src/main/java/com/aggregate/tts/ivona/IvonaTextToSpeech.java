package com.aggregate.tts.ivona;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.internal.StaticCredentialsProvider;
import com.ivona.services.tts.IvonaSpeechCloudClient;
import com.ivona.services.tts.model.CreateSpeechRequest;
import com.ivona.services.tts.model.Input;
import com.ivona.services.tts.model.Voice;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;

import java.io.UnsupportedEncodingException;

/**
 * Created by morfeusys on 10.02.16.
 */
public class IvonaTextToSpeech extends AbstractVerticle {
    private static final String ENDPOINT = "https://tts.eu-west-1.ivonacloud.com";

    private IvonaSpeechCloudClient client;

    @Override
    public void start(Future<Void> f) throws Exception {
        String accessKey = config().getString("accessKey");
        String secretKey = config().getString("secretKey");
        if (accessKey == null || accessKey.isEmpty() || secretKey == null || secretKey.isEmpty()) {
            f.fail("Provide your access and secret keys in config file");
            return;
        }

        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        client = new IvonaSpeechCloudClient(new StaticCredentialsProvider(credentials));
        client.setEndpoint(ENDPOINT);
        vertx.eventBus().consumer("tts.say", msg -> {
            Input input = new Input();
            Voice voice = new Voice();
            voice.setLanguage(config().getString("lang", "ru-RU"));
            voice.setName(config().getString("voice", "Tatyana"));
            voice.setGender(config().getString("gender", "Female"));
            input.setData((String) msg.body());
            CreateSpeechRequest request = new CreateSpeechRequest();
            request.setInput(input);
            request.setVoice(voice);
            try {
                msg.reply(client.getCreateSpeechUrl(request).toString());
            } catch (UnsupportedEncodingException e) {
                msg.fail(1, e.getMessage());
            }
        });

        f.complete();
    }
}
