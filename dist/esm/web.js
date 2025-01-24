import { WebPlugin } from '@capacitor/core';
export class FullScreenNotificationWeb extends WebPlugin {
    cancelNotification() {
        throw new Error('Method not implemented.');
    }
    addListener(eventName, listenerFunc) {
        listenerFunc(null);
        return Promise.reject(`Method 'addListener' for event '${eventName}' not implemented.`);
    }
}
//# sourceMappingURL=web.js.map