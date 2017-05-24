package com.production.sidorov.ivan.tabata;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.production.sidorov.ivan.tabata.sync.TimerService;

/**
 * Created by Иван on 09.03.2017.
 */

public class NotificationUtils {

    public static final int NOTIFICATION_ID = 1;

    private static final int PENDING_INTENT_WORKOUT_ID =148 ;

    public static final int PENDING_INTENT_CANCEL_NOTIFICATION_ID =432 ;

    public static final String ACTION_CANCEL_WORKOUT_NOTIFICATION ="action-cancel-notification";


    public static Notification showWorkoutNotification(Context context, Uri uri) {
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context).
                setColor(ContextCompat.getColor(context,R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_timer_black_24dp)
                .setLargeIcon(largeIcon(context))
                .setContentTitle(context.getString(R.string.workout_notification_title))
                .setContentText(context.getString(R.string.workout_notification_body))
                .setStyle(new NotificationCompat.BigTextStyle().bigText(
                        context.getString(R.string.workout_notification_body)))
                .setContentIntent(contentIntent(context,uri))
                .addAction(cancelWorkoutAction(context,uri))
                .setAutoCancel(true);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            notificationBuilder.setPriority(Notification.PRIORITY_HIGH);
        }

        return notificationBuilder.build();
    }

    private static PendingIntent contentIntent(Context context, Uri uri){
        Intent startWorkoutActivity = new Intent(context,WorkoutActivity.class);
        startWorkoutActivity.setData(uri);
        return PendingIntent.getActivity(context,
                PENDING_INTENT_WORKOUT_ID,
                startWorkoutActivity,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static NotificationCompat.Action cancelWorkoutAction(Context context, Uri uri) {

        Intent cancelWorkoutIntent = new Intent(context, TimerService.class);
        cancelWorkoutIntent.setData(uri);

        cancelWorkoutIntent.setAction(ACTION_CANCEL_WORKOUT_NOTIFICATION);

        PendingIntent cancelWorkoutPendingIntent = PendingIntent.getService(
                context,
                PENDING_INTENT_CANCEL_NOTIFICATION_ID,
                cancelWorkoutIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

       return  new NotificationCompat.Action(R.drawable.ic_close_black_24dp,
                context.getString(R.string.cancel_workout_message),
                cancelWorkoutPendingIntent);

    }


    private static Bitmap largeIcon(Context context) {

        Resources res = context.getResources();
        return  BitmapFactory.decodeResource(res, R.drawable.timer);
    }

}
