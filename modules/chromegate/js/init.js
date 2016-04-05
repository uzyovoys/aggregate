require.config({
    baseUrl: chrome.extension.getURL('js'),
    paths: {
        promise: 'lib/requirejs-promise',
        jquery: 'lib/jquery-1.11.1.min',
        underscore: 'lib/underscore-min',
        i18next: 'lib/i18n/i18next',
        jqueryI18next: 'lib/i18n/jqueryI18next',
        i18nextXHRBackend: 'lib/i18n/i18nextXHRBackend'
    }
});
