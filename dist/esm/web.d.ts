import { WebPlugin } from '@capacitor/core';
import { MessageListener, FullScreenNotificationPlugin } from './definitions';
export declare class FullScreenNotificationWeb extends WebPlugin implements FullScreenNotificationPlugin {
    cancelNotification(): Promise<void>;
    addListener(eventName: 'launch', listenerFunc: MessageListener): any;
}
