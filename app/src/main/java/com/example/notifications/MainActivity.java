package com.example.notifications;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private Button startService,stopService;
    private NotificationManager notificationManager;
    private boolean userAssertsStopTracking;
    private boolean clickedOnZeroSelections;
    public static HashMap<String,Double[]> storedLocations=new HashMap<>();
    public static HashMap<String,Boolean> chosenLocations=new HashMap<>();
    public static boolean changedInside = false;
    public static int ringerChoice;
    public static ArrayList<Boolean> result = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//                        Toast.makeText(this, new Throwable().getStackTrace()[0].getMethodName(), Toast.LENGTH_SHORT).show();
        Toast.makeText(this, "onCreate", Toast.LENGTH_SHORT).show();
        SharedPreferences sharedPreferences = getSharedPreferences("Preference", MODE_PRIVATE);
        boolean appOpenedOnce = sharedPreferences.getBoolean("appOpenedOnce", false);
        userAssertsStopTracking = sharedPreferences.getBoolean("userAssertsStopTracking",false);
        ringerChoice = sharedPreferences.getInt("ringerChoice",AudioManager.RINGER_MODE_SILENT);



//        locationStatusTextView = findViewById(R.id.locationStatusTextView);
        ListView listView = findViewById(R.id.listView);
        RadioButton radioButtonSilent = findViewById(R.id.Silent);
        RadioButton radioButtonVibrate = findViewById(R.id.Vibrate);
        RadioGroup radioGroup = findViewById(R.id.radioGroup);
        if(ringerChoice == AudioManager.RINGER_MODE_SILENT)
            radioButtonSilent.setChecked(true);
        else{
            radioButtonVibrate.setChecked(true);
        }

//        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @SuppressLint("NonConstantResourceId")
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
//                Toast.makeText(MainActivity.this, "Changed inside is true", Toast.LENGTH_SHORT).show();
                if (result.contains(true)) {
//                    Toast.makeText(MainActivity.this, "Changed inside is true2", Toast.LENGTH_SHORT).show();
                    changedInside = true;
                }
                switch (checkedId) {
                    case R.id.Vibrate:
                        ringerChoice = AudioManager.RINGER_MODE_VIBRATE;
                        Toast.makeText(MainActivity.this, "Ringer Choice: " + ringerChoice, Toast.LENGTH_SHORT).show();
                        break;
                    case R.id.Silent:
                        ringerChoice = AudioManager.RINGER_MODE_SILENT;
                        Toast.makeText(MainActivity.this, "Ringer Choice: " + ringerChoice, Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });
        startService = findViewById(R.id.startService);
        stopService = findViewById(R.id.stopService);
//        if(userAssertsStopTracking)
            startService.setVisibility(View.INVISIBLE);
//        else
//            startService.setVisibility(View.INVISIBLE);
        stopService.setVisibility(View.INVISIBLE);

        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        storedLocations.put("LHC-A", new Double[]{13.009479836915006, 74.79383910164134, 30.0});
        storedLocations.put("LHC-C", new Double[]{13.010503861128768, 74.79230771755365, 50.0});
        storedLocations.put("Nescafe", new Double[]{13.00745327019348, 74.79687176610611, 75.0});//8.0
        storedLocations.put("Mechanical Department", new Double[]{13.011908880121169, 74.79592254544531, 50.0});
        storedLocations.put("Central Library", new Double[]{13.009996881255617, 74.79481607444873, 27.0});
        storedLocations.put("Silver Jubilee Auditorium", new Double[]{13.008515309354053, 74.79572417338625, 32.0});
        storedLocations.put("IT Department", new Double[]{13.010964123250119, 74.79220034541154, 28.0});
        storedLocations.put("EC Department", new Double[]{13.011554420916156, 74.79215445705805, 30.0});
        storedLocations.put("Physics and Chemistry Department", new Double[]{13.008684588199557, 74.79480948040532, 32.0});
        storedLocations.put("CS Department", new Double[]{13.01268915079315, 74.79114312643043, 35.0});

        if (!appOpenedOnce) {
//            Toast.makeText(this, "inital", Toast.LENGTH_SHORT).show();
            for (Map.Entry<String, Double[]> pair : storedLocations.entrySet())
                chosenLocations.put(pair.getKey(), false);

            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("appOpenedOnce", true );
            editor.apply();
        }
        else {
//                Toast.makeText(this, "hy", Toast.LENGTH_SHORT).show();
//                String jsonString = sharedPreferences.getString("chosenLocations", null);
//                if (jsonString != null) {
//                    JSONObject jsonObject = new JSONObject(jsonString);
//                        Iterator<String> keysItr = jsonObject.keys();
//                        while (keysItr.hasNext()) {
//                            String key = keysItr.next();
//                        chosenLocations.put(key,(Boolean) jsonObject.get(key));
//                    }
//                }
//                SharedPreferences sharedPreferences = getSharedPreferences("Preference",MODE_PRIVATE);
                String hashMapString = sharedPreferences.getString("chosenLocations",null);
                Gson gson = new Gson();
                java.lang.reflect.Type type = new TypeToken<HashMap<String,Boolean>>(){}.getType();
//                Toast.makeText(this, hashMapString, Toast.LENGTH_SHORT).show();
                chosenLocations = gson.fromJson(hashMapString,type);
//            Toast.makeText(this, ""+numberSelected(), Toast.LENGTH_SHORT).show();
            }


        ArrayList<String> locationNames = new ArrayList<>();

        for (Map.Entry<String, Boolean> pair : chosenLocations.entrySet()) {
            locationNames.add(pair.getKey());
        }

        Collections.sort(locationNames);
        LocationAdapter locationAdapter = new LocationAdapter(MainActivity.this, R.layout.cardview, locationNames, chosenLocations);
        listView.setAdapter(locationAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if ((ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) &&
                        (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED)) {
                    int prevNumberOfSelections = numberSelected();
                    TextView tv = view.findViewById(R.id.Location);
                    boolean choice = Boolean.FALSE.equals(chosenLocations.get(String.valueOf(tv.getText())));
                    chosenLocations.put(String.valueOf(tv.getText()),choice);
                    locationAdapter.notifyDataSetChanged();
                    if(choice)
                        Toast.makeText(MainActivity.this, "Please Allow Location Tracking to use the Service", Toast.LENGTH_SHORT).show();

                    if (numberSelected() == 0) {
                        StopService();
                    }
                    if (numberSelected() == 1 && prevNumberOfSelections == 0) {
                        StartService();
                    }
//                    if(!isServiceRunning(LocationService.class))
//                        Toast.makeText(MainActivity.this, "Allow Location Tracking to use the Service", Toast.LENGTH_SHORT).show();
                }
            }
        });

        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                userAssertsStopTracking = false;
                clickedOnZeroSelections = false;
                if(numberSelected() == 0)
                    clickedOnZeroSelections = true;
//                startService.setVisibility(View.INVISIBLE);
//                locationStatusTextView.setText("Choose any of the Locations");
//                startService.setVisibility(View.INVISIBLE);
//                stopService.setVisibility(View.VISIBLE);
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
    }

    @Override
    protected void onStart() {
        super.onStart();
//        Toast.makeText(this, "onStart", Toast.LENGTH_SHORT).show();
        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                        requestBackgroundPermission();
                }
                else{
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
//            Toast.makeText(this, "Requesting", Toast.LENGTH_SHORT).show();
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
        }
    }

    @Override
    protected  void onStop() {
        super.onStop();
        Toast.makeText(MainActivity.this, "onStop", Toast.LENGTH_SHORT).show();
        JSONObject jsonObject = new JSONObject(chosenLocations);
        String jsonString = jsonObject.toString();
        SharedPreferences sharedPreferences = getSharedPreferences("Preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.remove("chosenLocations");
//        editor.apply();
        editor.putString("chosenLocations",jsonString).apply();
        editor.putBoolean("userAssertsStopTracking",userAssertsStopTracking).apply();
        editor.putInt("ringerChoice",ringerChoice).apply();
//        Gson gson = new Gson();
//        String hashMapString = gson.toJson(chosenLocations);
////        editor.remove("chosenLocation").apply();
//        SharedPreferences sharedPreferences1 = getSharedPreferences("Preference1", MODE_PRIVATE);
//        SharedPreferences.Editor editor = sharedPreferences1.edit();
//        Toast.makeText(this, hashMapString, Toast.LENGTH_SHORT).show();
//        editor.putString("chosenLocation",hashMapString);
//        boolean r = editor.commit();
//        Toast.makeText(MainActivity.this, "result"+r, Toast.LENGTH_SHORT).show();
//        Toast.makeText(this, "destroyed", Toast.LENGTH_SHORT).show();
//        Gson gson = new Gson();
//        String hashMapString = gson.toJson(chosenLocations);
//        editor.putString("chosenLocation",hashMapString);
//        editor.apply();
//        JSONObject jsonObject = new JSONObject(chosenLocations);
//        String jsonString = jsonObject.toString();
//        editor.remove("chosenLocations");
//        editor.apply();
//        editor.putString("chosenLocations",jsonString);
//        editor.apply();
//
    }

    @Override
    protected  void onDestroy() {
        super.onDestroy();
//        Toast.makeText(this, "destroyed", Toast.LENGTH_SHORT).show();


//        Toast.makeText(this, "onDestroy", Toast.LENGTH_SHORT).show();
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
        AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();
        alertDialog.setTitle("Background Permission");
        alertDialog.setMessage("Please Allow Background Location Access for the service to be functional");
        alertDialog.setCancelable(false);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Grant background Permission",
                (dialogInterface,n)-> {
                    alertDialog.dismiss();
//                    Toast.makeText(this, "Inside alert", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, 101);
                    }
                );
        
        alertDialog.show();

    }

    public void requestDoNotDisturbPermission(){
        Toast.makeText(this, "Do Not Disturb Permission is required to Silent the Phone", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Grant the Location Permission to start using the Service", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(this, "Background Location Access not Granted", Toast.LENGTH_SHORT).show();
                onStart();
//                Toast.makeText(this, "here", Toast.LENGTH_SHORT).show();
//                requestBackgroundPermission();

            }
        }

    }

    public void StartService(){
//        Toast.makeText(this, ""+isServiceRunning(LocationService.class), Toast.LENGTH_SHORT).show();
        if(!isServiceRunning(LocationService.class)) {
            if (userAssertsStopTracking) {
                startService.setVisibility(View.VISIBLE);
                stopService.setVisibility(View.INVISIBLE);
                return;
            }

            if (numberSelected() == 0) {
//            stopService.setVisibility(View.VISIBLE);
//                Toast.makeText(MainActivity.this, "No Locations have been selected", Toast.LENGTH_SHORT).show();
                    startService.setVisibility(View.INVISIBLE);
                stopService.setVisibility(View.INVISIBLE);
//                Toast.makeText(this, "1"+callingFunction, Toast.LENGTH_SHORT).show();
                if(clickedOnZeroSelections)
                    stopService.setVisibility(View.VISIBLE);
                return;
            }
            startService.setVisibility(View.INVISIBLE);
            stopService.setVisibility(View.VISIBLE);
            Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
            startForegroundService(serviceIntent);
        }
        else{
            startService.setVisibility(View.INVISIBLE);
            stopService.setVisibility(View.VISIBLE);
        }
    }

   public void StopService(){
        if(userAssertsStopTracking) {
            startService.setVisibility(View.VISIBLE);
            stopService.setVisibility(View.INVISIBLE);
            if(isServiceRunning(LocationService.class)) {
                Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
                stopService(serviceIntent);
            }
        }
        if(numberSelected() == 0 && userAssertsStopTracking)
            return;
        stopService.setVisibility(View.INVISIBLE);
       Toast.makeText(this, "Your Location is no longer being monitored", Toast.LENGTH_SHORT).show();
       if(isServiceRunning(LocationService.class)) {
           Intent serviceIntent = new Intent(MainActivity.this, LocationService.class);
           stopService(serviceIntent);
       }
   }

   public int numberSelected(){
        int numberOfItemsClicked = 0;
        for(Map.Entry<String,Boolean> pair: chosenLocations.entrySet()){
            if(pair.getValue())
                numberOfItemsClicked++;
        }
        return numberOfItemsClicked;
   }

    private boolean isServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}