package com.example.android.shushme;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingApi;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Seamfix-PC on 2/21/2018.
 */

public class GeoFencing implements ResultCallback {

    public static final String TAG = GeoFencing.class.getSimpleName();
    private static final long GEOFENCE_TIMEOUT = 24 * 60 * 60 * 1000; //24 hours
    private static final float GEOFENCE_RADIUS = 50; //50 meters
    private PendingIntent mGeofencePendingIntent;
    private ArrayList<Geofence> mGeofenceList;
    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    GeofencingApi geofencingApi;

    public GeoFencing(Context context, GoogleApiClient client) {
        mContext = context;
        mGoogleApiClient = client;
        mGeofencePendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void updateGeofencesList(PlaceBuffer places){
        mGeofenceList = new ArrayList<>();
        if(places == null|| places.getCount() == 0) return;
        for(Place place: places){
            //Read the place information from the DB cursor
            String placeUID = place.getId();
            double placeLat = place.getLatLng().latitude;
            double placeLng = place.getLatLng().longitude;

            //Build a Geofence object
            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeUID)
                    .setExpirationDuration(GEOFENCE_TIMEOUT)
                    .setCircularRegion(placeLat, placeLng, GEOFENCE_RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            // Add it to the list
            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingRequest(){
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        builder.addGeofences(mGeofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent(){
        //Reuse the pending intent if we already have it
        if(mGeofencePendingIntent != null){
            return mGeofencePendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mGeofencePendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
        return mGeofencePendingIntent;
    }

    public void registerAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()
                || mGeofenceList == null || mGeofenceList.size() == 0 ){
            return;
        }
        try {
            LocationServices.GeofencingApi.addGeofences(
                    mGoogleApiClient,
                    getGeofencingRequest(),
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException){
            Log.e(TAG, "registerAllGeofences: "+securityException.getMessage());
        }
    }
    public void unRegisterAllGeofences(){
        if(mGoogleApiClient == null || !mGoogleApiClient.isConnected()){
            return;
        }
        try{
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException securityException){
            Log.e(TAG, "unRegisterAllGeofences: "+ securityException.getMessage());
        }
    }
    @Override
    public void onResult(@NonNull Result result) {
        Log.e(TAG, "onResult: "+String.format("Error adding/removing geofence : %s",
                result.getStatus().toString()) );

    }
}
