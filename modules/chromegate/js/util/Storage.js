define({
    listen: function(options) {
        chrome.storage.sync.get(options, function(items) {
            for (var key in options) {
                options[key] = items[key];
            }
        });
        chrome.storage.onChanged.addListener(function(changes) {
            for (var key in options) {
                var change = changes[key];
                if (change) {
                    options[key] = change.newValue;
                }
            }
        });
    },

    local: function(key, def) {
        var value = def && def.hasOwnProperty(key) ? def[key] : def;
        return localStorage[key] || value;
    }
});