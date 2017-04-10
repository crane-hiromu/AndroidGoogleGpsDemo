package com.example.hiromuturuta.locationdemo;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {
    private final MainActivity self = this;

    private TextView LaTxt, LoTxt;
    private GoogleApiClient googleApiClient;
    private Location lastLocation;
    private LocationRequest locationRequest;

    private final static int REQ_PERMISSION = 10;

    private static final long GEO_DURATION = 100;
    private static final String GEOFENCE_REQ_ID = "ID";

    private PendingIntent geoFencePendingIntent;
    private final int GEOFENCE_REQ_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LaTxt = (TextView) findViewById(R.id.activity_main_latitude_text);
        LoTxt = (TextView) findViewById(R.id.activity_main_longitude_text);

        Button button = (Button) findViewById(R.id.activity_main_get_location_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GeofencingRequest geofenceRequest = getGeofenceRequest(getGeofence(35.658775, 139.705223, 100));
                addGeofence(geofenceRequest);
            }
        });

        //googleApiClient の作成(onStartで接続開始)
        if (googleApiClient == null) {
            googleApiClient = new GoogleApiClient.Builder(self)
                    .addConnectionCallbacks(self) //接続完了リスナ
                    .addOnConnectionFailedListener(self) //接続失敗リスナ
                    .addApi(LocationServices.API) //利用するAPIの指定
                    .build();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        googleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        googleApiClient.disconnect();
    }

    /////////////////////////////////////////////GooglePlayサービス/////////////////////////////////////////////

    /**
     * googleApiClient の接続成功時
     */
    @Override
    public void onConnected(@Nullable Bundle bundle) {
        checkLastLocation();
    }

    /**
     * googleApiClient の接続失敗時
     */
    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
    }

    /**
     * googleApiClient の切断時
     */
    @Override
    public void onConnectionSuspended(int i) {
    }

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////

    /**
     * 位置情報が更新された時
     */
    @Override
    public void onLocationChanged(Location location) {
        System.out.println("-----------onLocationChanged-----------");
        lastLocation = location;
        writeLocation(location);
    }

    /**
     * 権限要求の結果を受け取り(askPermission)
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQ_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkLastLocation();
                } else {
                    //権限がないため動かないよ
                }
                break;
            }
        }
    }

    /**
     * 前回のLocation を取得
     */
    private void checkLastLocation() {
        if (checkPermission()) {
            lastLocation = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
            if (lastLocation != null) {
                writeLocation(lastLocation);
            }
            updateLocation();
        } else {
            askPermission();
        }
    }

    /**
     * Permission の確認
     */
    private boolean checkPermission() {
        return (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);
    }

    /**
     * Permission の要求
     */
    private void askPermission() {
        ActivityCompat.requestPermissions(self, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, REQ_PERMISSION);
    }

    /**
     * Location を View に反映
     */
    private void writeLocation(Location location) {
        LaTxt.setText(String.valueOf(location.getLatitude()));
        LoTxt.setText(String.valueOf(location.getLongitude()));
    }

    /**
     * Location の更新（設定）
     *
     * パラメータの解説
     * http://qiita.com/mattak/items/22be63f57b71287164bf
     * http://k-sugi.sakura.ne.jp/java/android/3904/
     * http://molehill.mole-kingdom.com/opencms/export/sites/default/translate/Android/TrainingDoc/building-userinfo/location/receive-location-updates/
     */
    private void updateLocation() {
        locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)//電力と精度の設定
                .setInterval(1000)//秒単位で位置情報更新の間隔を設定
                .setFastestInterval(1000);//秒単位で位置情報更新の正確な間隔を設定

        //位置情報の取得
        if (checkPermission()) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                    googleApiClient,
                    locationRequest,
                    self);
        }
    }

    /**
     * Geofence の取得
     */
    //private Geofence getGeofence(LatLng latLng, float radius) {
    @NonNull
    private Geofence getGeofence(double latitude, double longitude, float radius) {
        return new Geofence.Builder()
                .setRequestId(GEOFENCE_REQ_ID)
                //.setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setCircularRegion(latitude, longitude, radius) //緯度、経度、半径
                .setExpirationDuration(GEO_DURATION)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                .build();
    }

    /**
     * GeofenceRequest の取得
     */
    @NonNull
    private GeofencingRequest getGeofenceRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();
    }

    /**
     * PendingIntent の取得
     */
    private PendingIntent getPendingIntent() {
        if (geoFencePendingIntent != null) {
            return geoFencePendingIntent;
        }

        Intent intent = new Intent(self, GeofenceTrasitionService.class);
        return PendingIntent.getService(self, GEOFENCE_REQ_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private void addGeofence(GeofencingRequest request) {
        System.out.println("-----------addGeofence-----------");
        if (checkPermission()) {
            LocationServices.GeofencingApi.addGeofences(
                    googleApiClient,
                    request,
                    getPendingIntent());
        }
    }
}