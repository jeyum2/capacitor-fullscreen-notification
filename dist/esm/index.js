import { registerPlugin } from '@capacitor/core';
const FullScreenNotification = registerPlugin('FullScreenNotification', {
    web: () => import('./web').then(m => new m.FullScreenNotificationWeb()),
});
export * from './definitions';
export { FullScreenNotification };
//# sourceMappingURL=index.js.map