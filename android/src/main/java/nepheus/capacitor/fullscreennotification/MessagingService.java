package nepheus.capacitor.fullscreennotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PowerManager;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.getcapacitor.JSObject;
import com.getcapacitor.Logger;
import com.google.firebase.messaging.RemoteMessage;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class MessagingService extends com.capacitorjs.plugins.pushnotifications.MessagingService {
    public static final int NOTIFICATION_ID = 33330;
    private static final int PENDING_INTENT_REQUEST_CODE = 33331;
    private static final int PENDING_INTENT_ANSWER_CODE = 33332;
    private static final int PENDING_INTENT_REJECT_CODE = 33333;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        this.processPush(remoteMessage);
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processPush(RemoteMessage remoteMessage) {
        Map<String, String> data = remoteMessage.getData();
        try {
            JSONObject payload = new JSONObject(data.get("payload"));
            String fn = payload.getString("fn");
            if ("call_new".equals(fn)) {
                processCallNew(remoteMessage);
            }
        } catch (JSONException e) {
            Logger.info("push err " + e);
        }
    }

    private PendingIntent createPendingIntent(Context context, int requestCode, JSONObject data) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(context.getPackageName())
                .setPackage(null)
                .setAction(context.getResources().getString(R.string.intent_filter_action_fullscreennotification_trigger))
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.replaceExtras(new Bundle());
        if (data != null) {
            Iterator<String> keys = data.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                try {
                    intent.putExtra(key, data.get(key).toString());
                } catch (JSONException e) {
                    Logger.error("Could not put '" + key + "' to intent extras", e);
                }
            }
        }
        return PendingIntent.getActivity(context, requestCode, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
    //region Call
    @RequiresApi(api = Build.VERSION_CODES.O)
    private NotificationChannel createCallChannel(NotificationManager manager) {
        String channelId = this.getPackageName() + ".callnew";
        String channelName = "돌봄앱";
        String channelDescription = "돌봄앱";
        NotificationChannel channel = new NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_HIGH);
        channel.setDescription(channelDescription);
        Uri soundUri = Uri.parse(
                ContentResolver.SCHEME_ANDROID_RESOURCE + "://" + getPackageName() + "/" + R.raw.ring20s);
        channel.setSound(soundUri, new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build());
        manager.createNotificationChannel(channel);
        return channel;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processCallNew(RemoteMessage remoteMessage) throws JSONException {
        Map<String, String> data = remoteMessage.getData();
        JSONObject payload = new JSONObject(data.get("payload"));

        String fullScreenId = remoteMessage.getMessageId();
        int defaultTimeout = 1 * 60 * 1000; // 1min
        Integer timeout = data.get("timeout") == null ? defaultTimeout :
                Integer.parseInt(Objects.requireNonNull(data.get("timeout")));

        JSONObject caller = payload.getJSONObject("Caller");
        String roomId = caller.getString("ConnectionId");
        String userName = caller.getString("Username");

        JSObject notificationIntentData = new JSObject();
        notificationIntentData.put("fullScreenId", fullScreenId);
        notificationIntentData.put("roomId", roomId);
        notificationIntentData.put("userName", userName);
        notificationIntentData.put("timeout", timeout);
        notificationIntentData.put("actionId", "call_new");
        PendingIntent fullScreenPendingIntent = createPendingIntent(this, PENDING_INTENT_REQUEST_CODE, notificationIntentData);
        notificationIntentData.put("actionId", "answer");
        PendingIntent pendingAnswerIntent = createPendingIntent(this, PENDING_INTENT_ANSWER_CODE, notificationIntentData);
        notificationIntentData.put("actionId", "reject");
        PendingIntent pendingRejectIntent = createPendingIntent(this, PENDING_INTENT_REJECT_CODE, notificationIntentData);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel = createCallChannel(manager);

        // Setup RemoteView Noti
        RemoteViews remoteView = new RemoteViews(getPackageName(), R.layout.incoming_call_noti);
        remoteView.setTextViewText(R.id.caller_text, userName + "에게 전화가 왔습니다");
        remoteView.setOnClickPendingIntent(R.id.answer_button, pendingAnswerIntent);
        remoteView.setOnClickPendingIntent(R.id.reject_button, pendingRejectIntent);

        NotificationCompat.Builder notification = new NotificationCompat.Builder(getApplicationContext(), channel.getId())
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_CALL)
                .setSmallIcon(getResources().getIdentifier("ic_launcher", "mipmap", getPackageName()))
                .setCustomContentView(remoteView)
                .setCustomBigContentView(remoteView)
                .setCustomHeadsUpContentView(remoteView)
                .setAutoCancel(true)
                .setOngoing(true)
                .setOnlyAlertOnce(false)
                .setTimeoutAfter(timeout)
                .setFullScreenIntent(fullScreenPendingIntent, true);
        manager.notify(NOTIFICATION_ID, notification.build());
        Utils.wake(this);
    }
    //endregion Call
}
