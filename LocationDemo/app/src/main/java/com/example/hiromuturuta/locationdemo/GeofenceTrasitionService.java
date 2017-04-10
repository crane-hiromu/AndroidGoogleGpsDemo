package com.example.hiromuturuta.locationdemo;

import android.app.IntentService;
import android.content.Intent;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingEvent;

/**
 * Created by hiromu.turuta on 2017/02/02.
 */
public class GeofenceTrasitionService extends IntentService {

    private static final String TAG = GeofenceTrasitionService.class.getSimpleName();

    public GeofenceTrasitionService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        //エラー処理
        if (geofencingEvent.hasError()) {
            return;
        }

        //Geofence内での動きを取得
        int geoFenceTransition = geofencingEvent.getGeofenceTransition();

        // Geofence内の出入りを判定
        switch (geoFenceTransition) {
            // Geofence内
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                System.out.println("in");
                break;

            // Geofence外
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                System.out.println("out");
                break;

            default:
                break;
        }
    }
}