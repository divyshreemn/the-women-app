package com.example.womensafetyapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.telephony.SmsManager;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class PanicService {
    private Context context;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private DatabaseHelper relativesDB;
    private List<String> phoneNumbers;
    private boolean isServiceRunning;

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private String message;
    private int delay;
    private boolean recording;

    private ExecutorService executorService;

    public PanicService(Context context) {
        this.context = context;
        relativesDB = new DatabaseHelper(context);
        isServiceRunning = false;
        sharedPreferences = context.getSharedPreferences("setting", Context.MODE_PRIVATE);
        editor=sharedPreferences.edit();

        // Retrieve the stored values from SharedPreferences
        message = sharedPreferences.getString("message", "Hey, your relative is in danger!");
        delay = sharedPreferences.getInt("delay", 30);
        recording = sharedPreferences.getBoolean("recording", false);

        executorService = Executors.newSingleThreadExecutor();
    }

    public boolean isPanicServiceRunning() {
        return isServiceRunning;
    }
    public void startPanicService() {
        if (isServiceRunning) {
            showToast("Service is already running");
            return;
        }

        isServiceRunning = true;
        showToast("Panic Service started");

        // Check if location permission is granted
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            // Location permission not granted, request it
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
            return;
        }

        // Check if location service is enabled
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        boolean isLocationEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (!isLocationEnabled) {
            // Location service not enabled, prompt user to enable it
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
            return;
        }

        // Retrieve phone numbers from RelativesDB
        phoneNumbers = relativesDB.getAllPhoneNumbers();
        // Start getting location updates
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                // Location update received, send current location to phone numbers
                sendLocationMessage(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                showToast(provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onProviderDisabled(String provider) {
            }
        };
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        editor.putBoolean("isServiceRunning",true);
        editor.apply();
    }

    public void stopPanicService() {
        if (!isServiceRunning) {
            showToast("Service is already stopped");
            return;
        }

        if (locationManager != null && locationListener != null) {
            // Stop getting location updates
            locationManager.removeUpdates(locationListener);
            isServiceRunning = false;
            editor.putBoolean("isServiceRunning",false);
            editor.apply();
            showToast("Panic Service stopped");
        } else {
            showToast("Panic Service is not running");
        }
    }

    private void showToast(String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    private void sendLocationMessage(Location location) {
        // Iterate through phone numbers and send location message
        for (String phoneNumber : phoneNumbers) {
            String smsMessage = message+" http://maps.google.com/maps?q=loc:" + location.getLatitude() + "," + location.getLongitude() + " (" + "Your relative location" + ")";
            sendSMS(phoneNumber, smsMessage);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        executorService.execute(() -> {
            try {
                // Simulate delay if set
                if (delay > 0) {
                    TimeUnit.SECONDS.sleep(delay);
                }

                // Send SMS
                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, message, null, null);

                // Show toast on UI thread
                new Handler(context.getMainLooper()).post(() -> {
                    showToast("SMS sent to " + phoneNumber);
                });
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }
}
