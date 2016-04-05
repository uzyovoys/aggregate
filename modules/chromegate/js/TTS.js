define(['util/Storage', 'util/i18n'], function(Storage, i18n) {
    var TTS = function(handler) {
        var counter = 0;

        function speak(speech, lang) {
            if (!speech) return;
            counter++;
            var u = new SpeechSynthesisUtterance();

            var voice = Storage.local('voice', null);
            var voices = speechSynthesis.getVoices().filter(function(v) {
                return lang ? v.lang.indexOf(lang) !== -1 : v.name === voice;
            });

            u.text = speech;
            u.voice = voices.length ? voices[0] : null;
            u.lang = voices.length ? voices[0].lang : i18n('locale');
            u.pitch = parseInt(Storage.local('pitch', TTS.options));
            u.rate = parseFloat(Storage.local('rate', TTS.options));

            u.onend = complete;
            u.onerror = complete;

            speechSynthesis.speak(u);
        }

        function stop() {
            speechSynthesis.cancel();
            counter = 0;
        }

        function complete() {
            counter = Math.max(0, counter - 1);
            counter === 0 && handler.complete();
        }

        TTS.getVoices();

        return {
            speak: speak,
            stop: stop,
            isSpeaking: function() { return counter > 0; }
        }
    };

    TTS.getVoices = function() {
        if (!TTS.isSupported()) return [];
        return speechSynthesis.getVoices().filter(function(v) {
            return v.lang === i18n('locale');
        });
    };

    TTS.isSupported = function() {
        return !!window['speechSynthesis'];
    };

    TTS.options = {
      pitch: '1', rate: '1.05'
    };

    return TTS;
});