package nepheus.capacitor.fullscreennotification;

import android.content.Context;
import android.os.PowerManager;

public class Utils {
    public static void wake(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = pm.newWakeLock(PowerManager.FULL_WAKE_LOCK
                // | PowerManager.PARTIAL_WAKE_LOCK
                | PowerManager.ACQUIRE_CAUSES_WAKEUP
                | PowerManager.ON_AFTER_RELEASE, "MyWakeLock:Tag");

        wakeLock.acquire();
    }
}
