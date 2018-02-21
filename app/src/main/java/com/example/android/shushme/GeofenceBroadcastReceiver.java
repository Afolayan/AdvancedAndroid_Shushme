package com.example.android.shushme;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by Seamfix-PC on 2/21/2018.
 */

public class GeofenceBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();

    public GeofenceBroadcastReceiver() {
        super();
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "onReceive: called");
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        // Get the transition type
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        //check which transition type has triggered this event
        if(geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER){
            setRingerMode(context, AudioManager.RINGER_MODE_SILENT, geofenceTransition);
        } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT){
            setRingerMode(context, AudioManager.RINGER_MODE_NORMAL, geofenceTransition);
        }else {
            Log.e(TAG, "onReceive: "+String.format("Unknown transition : %d",
                    geofenceTransition));
            return;
        }
    }


    private void setRingerMode(Context context, int mode, int transitionType){
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null && (Build.VERSION.SDK_INT < 24 || (
                Build.VERSION.SDK_INT >= 24 && !notificationManager.isNotificationPolicyAccessGranted()))) {
            AudioManager audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
            audioManager.setRingerMode(mode);
        }
        if (notificationManager != null) {
            notificationManager.notify(0, sendNotification(context, transitionType));
        }
    }

    private Notification sendNotification(Context context, int transitionType){
        Notification.Builder builder = new Notification.Builder(context);
        if( transitionType == Geofence.GEOFENCE_TRANSITION_ENTER){
            builder.setSmallIcon(R.drawable.ic_volume_off_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_volume_off_white_24dp))
                    .setContentTitle(context.getString(R.string.silent_mode_activated));
        } else if (transitionType == Geofence.GEOFENCE_TRANSITION_EXIT){
            builder.setSmallIcon(R.drawable.ic_volume_up_white_24dp)
                    .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_volume_up_white_24dp))
                    .setContentTitle(context.getString(R.string.back_to_normal));
        }
        return builder.build();
    }
}
