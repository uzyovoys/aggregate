require(
    ['underscore', 'Recognizer', 'TTS', 'util/Storage', 'util/i18n'],
    function(_, Recognizer, TTS, Storage, i18n) {
        var options, listening;
        var beep = new Audio('beep.mp3');
        var eventBus;

        var rec = new Recognizer(
            {
                result: recognized,
                partial: partial,
                error: onRecognizerError
            }
        );

        var tts = new TTS({ complete: listen });

        chrome.runtime.onMessage.addListener(function(request) {
            if (request.init === true) {
                init();
            }
        });

        init();
        listen();

        function listen() {
            rec.start();
        }

        function onRecognizerError(error) {
            if (error === 'no-speech') {
                listen();
            } else if (error === 'audio-capture') {
                notify({text: i18n('settings.blocked')});
            } else if (error === 'not-allowed') {
                if (!options) {
                    options = true;
                    chrome.runtime.openOptionsPage();
                }
            }
        }

        function recognized(result) {
            if (tts && tts.isSpeaking()) return;
            var input = getCommand(result);
            if (input) {
                beep.play();
                process(input);
            }
            listen();
        }

        function partial(result) {
        }

        function getCommand(text) {
            if (!text) return '';
            if (listening) return text;
            var cmd = text;
            var name = Storage.local('name', i18n('settings.assistant.name.default'));
            text = text.toLowerCase();
            var pos = text.indexOf(name.toLowerCase());
            if (pos === -1 || text.length === name.length) return '';
            return cmd.substring(pos + name.length + 1);
        }

        function process(input) {
            listening = false;
            if (eventBus && eventBus.state === EventBus.OPEN) {
                eventBus.send('asr.result', input);
            } else {
                init(_.partial(process, input));
            }
        }

        function say(speech, lang) {
            rec.stop();
            var speeches = speech.split('\\|');
            speeches.forEach(function(s) {
                tts.speak(s, lang);
            });
        }

        function notify(msg) {
            var messages = _.isString(msg) ? [msg] : msg.text ? [msg.text] : msg['speeches'];
            if (messages) {
                var name = Storage.local('name', i18n('settings.assistant.name.default'));
                messages.forEach(function(msg) {
                    chrome.notifications.create({
                        type: "basic", iconUrl: "./img/icon128.png",
                        title: name,
                        message: msg
                    });
                });
            }
        }

        function init(callback) {
            if (eventBus) {
                eventBus.close();
            }
            eventBus = new EventBus('http://' + Storage.local('address', i18n('settings.address.default')) + '/eventbus');
            eventBus.onopen = function() {
                eventBus.registerHandler('tts.say', function(error, message) {
                    say(message['body']);
                });
                eventBus.registerHandler('tts.stop', function(error, message) {
                    tts && tts.stop();
                });
                eventBus.registerHandler('response', function(error, response) {
                    var msg = response['body'];
                    listening = !!msg.modal;
                    notify(msg);
                });
                if (_.isFunction(callback)) {
                    callback();
                }
            };
        }
    }
);

chrome.browserAction.onClicked.addListener(function() {
    chrome.runtime.openOptionsPage();
});