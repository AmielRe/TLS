package com.amiel.tls;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.core.app.NotificationCompat;
import static com.amiel.tls.MainActivity. NOTIFICATION_CHANNEL_ID;

public class AlarmReceiver extends BroadcastReceiver {

    public static String NOTIFICATION_ID = "notification-id";
    public static String NOTIFICATION = "notification";
    public void onReceive (Context context , Intent intent) {
        NotificationManager notificationManager = (NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE) ;
        Notification notification = intent.getParcelableExtra(NOTIFICATION) ;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID, "NOTIFICATION_CHANNEL_NAME" ,importance);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(notificationChannel);
        }
        int id = intent.getIntExtra(NOTIFICATION_ID, 0);
        assert notificationManager != null;
        setNextAlarm(context, notification);
        notificationManager.notify(id ,notification);
    }

    private void setNextAlarm(Context context, Notification notification) {
        long time = System.currentTimeMillis();
        notification.when = time;
        Intent notificationIntent = new Intent( context, AlarmReceiver.class );
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION_ID , 1 );
        notificationIntent.putExtra(AlarmReceiver.NOTIFICATION , notification);
        PendingIntent pendingIntent = PendingIntent. getBroadcast ( context, 0 , notificationIntent ,0);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE) ;
        assert alarmManager != null;

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, time + (AlarmManager.INTERVAL_DAY * 7), pendingIntent);
    }
}
