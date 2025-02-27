package com.example.womensafetyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Get the SharedPreferences object
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        // Check if a particular key exists
        boolean containsName = sharedPreferences.contains("username");
        boolean containsPhoneNumber = sharedPreferences.contains("phonenumber");

        // Check the result
        if (containsName && containsPhoneNumber) {
            //user already logged in redirecting to home page
            Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
            startActivity(intent);
            finish();

        } else {
            //user not logged in redirecting to login page
            Intent intent=new Intent(getApplicationContext(),Login.class);
            startActivity(intent);
            finish();
        }

    }
}