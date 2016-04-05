define(['util/i18n'], function(i18n) {
    function module(postfix, name) {
        return i18n(postfix, {ns: name});
    }

    return {
        getLabel: _.partial(module, 'label'),
        getSummary: _.partial(module, 'summary'),
        getSamples: function(name) {
            return module('samples', name).split(',').map(function(n) { return n.trim(); });
        }
    }
});