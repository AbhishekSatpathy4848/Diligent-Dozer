package com.example.notifications;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.AudioManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ExampleService extends Service {
    private FusedLocationProviderClient fusedLocationProviderClient;
    private LocationRequest locationRequest;
    private Notification notification;
    private PendingIntent pendingIntent;
    private NotificationChannel notificationChannel;
    private NotificationManager notificationManager;
    private final HashMap<String,Double> DistanceMap=new HashMap<>();
    private boolean isInside=false;
    private boolean isOutside=false;
    private int currentStreamVolume = 0;


    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            if (locationResult == null) {
                return;
            } else {
                for (Location location : locationResult.getLocations()) {
//                    Toast.makeText(ExampleService.this, "hi", Toast.LENGTH_SHORT).show();
                    double latitude = location.getLatitude();
                    double longitude = location.getLongitude();
//                    Toast.makeText(ExampleService.this, "Latitude" + latitude + "Longitude" + longitude, Toast.LENGTH_SHORT).show();
                    for (Map.Entry<String, Boolean> pair : MainActivity.choosenLocations.entrySet()) {
//                        Toast.makeText(MainActivity.this, String.valueOf(pair.getValue()), Toast.LENGTH_SHORT).show();
                        if (pair.getValue()) {
//                            Toast.makeText(MainActivity.this, "True "+pair.getKey(), Toast.LENGTH_SHORT).show();
                            double Distance = distance(Objects.requireNonNull(MainActivity.storedLocations.get(pair.getKey()))[0], Objects.requireNonNull(MainActivity.storedLocations.get(pair.getKey()))[1], latitude, longitude);
//                            d.setText(String.valueOf(Distance));
                            Toast.makeText(ExampleService.this, "Distance from" + pair.getKey() + " " + Distance, Toast.LENGTH_SHORT).show();

//                                                Toast.makeText(MainActivity.this, ""+Distance, Toast.LENGTH_SHORT).show();

//                            AddressText.setText(pair.getKey() + " " + "Latitude:" + storedLocations.get(pair.getKey())[0] + "\n" + "Longitude: " + storedLocations.get(pair.getKey())[1]);
                            DistanceMap.put(pair.getKey(), Distance);
                        } else {
                            DistanceMap.remove(pair.getKey());
                        }
                    }
                }
                MainActivity.result.clear();
                for (Map.Entry<String, Double> pair : DistanceMap.entrySet()) {
                    boolean isPhoneInside = Objects.requireNonNull(DistanceMap.get(pair.getKey())) <= Objects.requireNonNull(MainActivity.storedLocations.get(pair.getKey()))[2];
                    MainActivity.result.add(isPhoneInside);
                    if(isPhoneInside) {
                        //    private boolean changedInside=false;
                        String currentLandmark = pair.getKey();
                        Notification notificationWithLocation = new Notification.Builder(ExampleService.this,"MyNotifications")
                                .setContentTitle("You are currently inside "+ currentLandmark)
                                .setContentText("Your Location is currently being used in the background")
                                .setSmallIcon(R.drawable.ic_launcher_background)
                                .setContentIntent(pendingIntent)
                                .setOnlyAlertOnce(true)
                                .build();
                        notificationManager.notify(1,notificationWithLocation);
                    }
                }
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                if (MainActivity.result.contains(true)) {
                    if(audioManager.getStreamVolume(AudioManager.STREAM_MUSIC) != 0){
                        currentStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
                    }
                    if (!isInside || MainActivity.changedInside) {
                        if (audioManager.getRingerMode() != MainActivity.ringerChoice) {
//                            Toast.makeText(ExampleService.this, ""+MainActivity.ringerChoice, Toast.LENGTH_SHORT).show();
                            audioManager.setRingerMode(MainActivity.ringerChoice);
                            if(!isInside)
                                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
                            MainActivity.changedInside = false;
                        }
                        isInside = true;
                        isOutside = false;
                    }
                } else {
                    notificationManager.notify(1,notification);
                    if(!isOutside) {
                        if (audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
                            audioManager.setRingerMode(AudioManager.RINGER_MODE_NORMAL);
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,currentStreamVolume,0);
                            currentStreamVolume = 0;
                        }
                        isInside = false;
                        isOutside=true;
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
//        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        locationRequest = LocationRequest.create();
        locationRequest.setInterval(3000);
        locationRequest.setFastestInterval(3000);
        locationRequest.setPriority(Priority.PRIORITY_LOW_POWER);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel("MyNotifications", "MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(notificationChannel);
        }
        Intent notificationIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);
        notification = new NotificationCompat.Builder(this,"MyNotifications")
                .setContentText("Your Location is currently being used in the background")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setCategory(Notification.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .build();
    }




    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForeground(1,notification);
        checkSettingsAndStartLocationUpdates();
        return START_NOT_STICKY;
    }


    @Override
    public void onDestroy() {
        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
        stopLocationUpdates();
        super.onDestroy();
    }

    //only used for Bound Services
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private double deg2rad(double deg) {
        return (deg * (Math.PI / 180.0));
    }

    private double distance(double lat1, double long1, double lat2, double long2) {
        double R = 6371 * 1000;
        double diffLat = deg2rad(lat2 - lat1);
        double diffLong = deg2rad(long2 - long1);
        double dist = Math.sin(diffLat / 2) * Math.sin(diffLat / 2) + Math.cos(deg2rad(lat1)) * Math.cos(deg2rad(lat2)) * Math.sin(diffLong / 2) * Math.sin(diffLong / 2);
        double d = 2 * Math.atan2(Math.sqrt(dist), Math.sqrt(1 - dist));
        return R * d;
    }

    private void checkSettingsAndStartLocationUpdates(){
        LocationSettingsRequest request=new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest).build();
        SettingsClient client= LocationServices.getSettingsClient(this);

        Task<LocationSettingsResponse> locationSettingsResponseTask=client.checkLocationSettings(request);

        locationSettingsResponseTask.addOnSuccessListener(new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                //Settings of device are satisfied and we can start location updates
                startLocationUpdates();
            }
        });
        locationSettingsResponseTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
//                if(e instanceof ResolvableApiException){
//                    ResolvableApiException apiException=(ResolvableApiException) e;
//                    try {
//                        apiException.startResolutionForResult(,1001);
//                    } catch (IntentSender.SendIntentException ex) {
//                        ex.printStackTrace();
//                    }
//
//                }
                Toast.makeText(ExampleService.this, "Unable to Access Location Services", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void startLocationUpdates(){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        }
    }
    private void stopLocationUpdates(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

}
