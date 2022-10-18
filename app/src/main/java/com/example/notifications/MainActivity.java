package com.example.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

//    Notification notification;
//    NotificationChannel notificationChannel;
//    NotificationManager notificationManager;

    private Button startService,stopService;
//    private TextView locationStatusTextView;
    //    private Boolean serviceStarted = false;
    private NotificationManager notificationManager;
    private boolean userAssertsStopTracking = false;
    public static HashMap<String,Double[]> storedLocations=new HashMap<>();
    public static HashMap<String,Boolean> choosenLocations=new HashMap<>();
    public static boolean changedInside = false;
    public static int ringerChoice;
    public static ArrayList<Boolean> result = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);



//        locationStatusTextView = findViewById(R.id.locationStatusTextView);
        ListView listView = findViewById(R.id.listView);
        RadioButton radioButton = findViewById(R.id.Silent);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        radioButton.setChecked(true);

//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Toast.makeText(MainActivity.this, "Changed inside is true", Toast.LENGTH_SHORT).show();
                if(result.contains(true)){
//                    Toast.makeText(MainActivity.this, "Changed inside is true2", Toast.LENGTH_SHORT).show();
                    changedInside=true;
                }
                switch (checkedId){
                    case R.id.Vibrate:
                        ringerChoice=AudioManager.RINGER_MODE_VIBRATE;
                        Toast.makeText(MainActivity.this, "Ringer Choice: "+ ringerChoice, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.Silent:
                        ringerChoice=AudioManager.RINGER_MODE_SILENT;
                        Toast.makeText(MainActivity.this, "Ringer Choice: "+ ringerChoice, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        startService = findViewById(R.id.startService);
        stopService = findViewById(R.id.stopService);
        startService.setVisibility(View.INVISIBLE);
        stopService.setVisibility(View.INVISIBLE);
//        listView = findViewById(R.id.listView);

        notificationManager= (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        storedLocations.put("LHC-A",new Double[] {13.009479836915006,74.79383910164134,30.0});
        storedLocations.put("LHC-C",new Double[] {13.010503861128768,74.79230771755365,50.0});
        storedLocations.put("Nescafe",new Double[] {13.00745327019348,74.79687176610611,75.0});//8.0
        storedLocations.put("Mechanical Department",new Double[] {13.011908880121169,74.79592254544531,50.0});
        storedLocations.put("Central Library",new Double[] {13.009996881255617,74.79481607444873,27.0});
        storedLocations.put("Silver Jubilee Auditorium",new Double[] {13.008515309354053,74.79572417338625,32.0});
        storedLocations.put("IT Department",new Double[] {13.010964123250119,74.79220034541154,28.0});
        storedLocations.put("EC Department",new Double[] {13.011554420916156,74.79215445705805,30.0});
        storedLocations.put("Physics and Chemistry Department",new Double[] {13.008684588199557,74.79480948040532,32.0});
        storedLocations.put("CS Department",new Double[] {13.01268915079315,74.79114312643043,35.0});

        for (Map.Entry<String, Double[]> pair : storedLocations.entrySet()) {
            choosenLocations.put(pair.getKey(),false);
        }

        ArrayList<String> locationNames=new ArrayList<>();

        for (Map.Entry<String, Boolean> pair : choosenLocations.entrySet()){
            locationNames.add(pair.getKey());
        }

        Collections.sort(locationNames);
        LocationAdapter locationAdapter = new LocationAdapter(MainActivity.this, R.layout.cardview,locationNames,choosenLocations);
        listView.setAdapter(locationAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                int prevNumberOfSelections = numberSelected();
                TextView tv=view.findViewById(R.id.Location);
                choosenLocations.put(String.valueOf(tv.getText()), Boolean.FALSE.equals(choosenLocations.get(String.valueOf(tv.getText()))));
                locationAdapter.notifyDataSetChanged();
                if(numberSelected() == 0){
                    StopService();
                }
                if(numberSelected() == 1 && prevNumberOfSelections == 0){
                    StartService();
                }
            }
        });

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAssertsStopTracking = false;
//                startService.setVisibility(View.INVISIBLE);
//                locationStatusTextView.setText("Choose any of the Locations");
                startService.setVisibility(View.INVISIBLE);
                stopService.setVisibility(View.VISIBLE);
                Toast.makeText(MainActivity.this, "You have given permission to monitor your Location", Toast.LENGTH_SHORT).show();
                StartService();


            }
        });

        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAssertsStopTracking = true;
                StopService();
            }
        });
//        createNotificationChannel();
//        createNotification();






        //for older versions of android before oreo
//        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(this);
//        managerCompat.notify(1,notification);


    }
//    public void createNotificationChannel(){
//        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.O){
//            notificationChannel = new NotificationChannel("MyNotifications","MyNotifications", NotificationManager.IMPORTANCE_DEFAULT);
//            notificationManager = getSystemService(NotificationManager.class);
//            notificationManager.createNotificationChannel(notificationChannel);
//        }
//    }
//    public void createNotification(){
//        notification = new Notification.Builder(this,"MyNotifications")
//                .setContentTitle("Notification")
//                .setContentText("You received a Notification")
//                .setSmallIcon(R.drawable.ic_launcher_background)
//                .build();
//    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        requestBackgroundPermission();
                }
                else{
//                    if(ActivityCompat.checkSelfPermission(MainActivity.this,Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    if (!notificationManager.isNotificationPolicyAccessGranted()) {
                         requestDoNotDisturbPermission();
                    }
                    else {
                        StartService();
                    }


                }
            }
            else{
//                if(!serviceStarted)
                    if (!notificationManager.isNotificationPolicyAccessGranted()) {
                        requestDoNotDisturbPermission();
                    }
                    else {
                        StartService();

                    }
            }
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

//    private void askLocationPermission() {
//        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

//            } else {
//                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
//            }
//        }
//    }

    public void requestBackgroundPermission(){
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 101);
    }

    public void requestDoNotDisturbPermission(){
        if(!notificationManager.isNotificationPolicyAccessGranted()){
            startActivity(new Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS));
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        //just the request code. Any value will do. This corresponds to the requestCode while asking for permission.
        if(requestCode == 100){
            if(grantResults.length > 0 && grantResults[0] == (PackageManager.PERMISSION_GRANTED)){
                //permission granted
                requestBackgroundPermission();
            }
            else{
                //permission not granted
                Toast.makeText(this, "Grant the Location Permission to use the Service", Toast.LENGTH_SHORT).show();

            }
        }
        if(requestCode == 101){
//            Toast.makeText(this, "Permission"+permissions.length, Toast.LENGTH_SHORT).show();
            if(grantResults.length > 0 && grantResults[0] == (PackageManager.PERMISSION_GRANTED)){
//                serviceStarted = true;
                onStart();
            }
            else{
                //permission not granted
                Toast.makeText(this, "Grant the Background Location Access to use the Service", Toast.LENGTH_SHORT).show();

            }
        }

    }

    public void StartService(){
        if(userAssertsStopTracking){
            return;
        }

        if(numberSelected() == 0){
//            stopService.setVisibility(View.VISIBLE);
//            Toast.makeText(MainActivity.this, "No Locations have been selected", Toast.LENGTH_SHORT).show();
            return;
        }
        startService.setVisibility(View.INVISIBLE);
        stopService.setVisibility(View.VISIBLE);
        Intent serviceIntent =new Intent(MainActivity.this,ExampleService.class);
        startForegroundService(serviceIntent);
    }

   public void StopService(){
        if(numberSelected()>0 || userAssertsStopTracking)
            startService.setVisibility(View.VISIBLE);
        stopService.setVisibility(View.INVISIBLE);
       Toast.makeText(this, "Your Location is no longer being monitored", Toast.LENGTH_SHORT).show();
       Intent serviceIntent =new Intent(MainActivity.this,ExampleService.class);
       stopService(serviceIntent);
   }

   public int numberSelected(){
        int numberOfItemsClicked = 0;
        for(Map.Entry<String,Boolean> pair: choosenLocations.entrySet()){
            if(pair.getValue())
                numberOfItemsClicked++;
        }
        return numberOfItemsClicked;
   }
}