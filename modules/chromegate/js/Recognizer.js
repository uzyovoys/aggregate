define(['util/i18n'], function(i16n) {
    function Recognizer(handler) {
        var recognition = initRecognition();
        var result;

        function initRecognition() {
            var recognition = new webkitSpeechRecognition();
            recognition.interimResults = true;
            recognition.continuous = false;
            recognition.lang = i16n('locale');

            recognition.onstart = function() {
                result = '';
            };

            recognition.onresult = function(event) {
                result = '';
                for (var i = event.resultIndex; i < event.results.length; ++i) {
                    result += event.results[i][0].transcript;
                }
            };

            recognition.onerror = function(event) {
                handler.error(event.error);
            };

            recognition.onend = function() {
                handler.result(result);
            };

            return recognition;
        }

        function start() {
            recognition.start();
        }

        function stop() {
            recognition.stop();
        }

        return {
            start: start,
            stop: stop
        }
    }

    Recognizer.isSupported = function() {
        return !!window['webkitSpeechRecognition'];
    };

    return Recognizer;
});