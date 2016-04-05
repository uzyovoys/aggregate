define('i18nPromise',
    ['jquery', 'underscore', 'i18next', 'jqueryI18next', 'i18nextXHRBackend'],

    function($, _, i18next, i18next$, i18nextXHR) {
        var def = $.Deferred();
        var ns = ['translation'];

        i18next.use(i18nextXHR).init({
            debug: true,
            lng: navigator.language,
            fallbackLng: 'en',
            ns: ns,
            defaultNs: ns[0],
            fallbackNS: ns[0]
        }, function() {
            i18next$.init(i18next, $, {
                useOptionsAttr: true
            });
            def.resolve(i18next);
        });

        return def.promise();
    }
);

define(['underscore', 'promise!i18nPromise'], function(_, i18nPromise) {
    return _.bind(i18nPromise.t, i18nPromise);
});