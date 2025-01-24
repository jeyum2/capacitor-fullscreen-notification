var capacitorFullScreenNotification = (function (exports, core) {
    'use strict';

    const FullScreenNotification = core.registerPlugin('FullScreenNotification', {
        web: () => Promise.resolve().then(function () { return web; }).then(m => new m.FullScreenNotificationWeb()),
    });

    class FullScreenNotificationWeb extends core.WebPlugin {
        cancelNotification() {
            throw new Error('Method not implemented.');
        }
        addListener(eventName, listenerFunc) {
            listenerFunc(null);
            return Promise.reject(`Method 'addListener' for event '${eventName}' not implemented.`);
        }
    }

    var web = /*#__PURE__*/Object.freeze({
        __proto__: null,
        FullScreenNotificationWeb: FullScreenNotificationWeb
    });

    exports.FullScreenNotification = FullScreenNotification;

    Object.defineProperty(exports, '__esModule', { value: true });

    return exports;

}({}, capacitorExports));
//# sourceMappingURL=plugin.js.map
