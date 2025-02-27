package com.example.womensafetyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.Manifest;


public class EmergencyNoActivity extends AppCompatActivity {
    private static final int REQUEST_PHONE_CALL = 1;
    private static String PHONE_NUMBER = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_emergency_no);

        findViewById(R.id.callpolice).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONE_NUMBER="100";
                makePhoneCall();

            }
        });
        findViewById(R.id.callfire).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONE_NUMBER="101";
                makePhoneCall();

            }
        });
        findViewById(R.id.callambulence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONE_NUMBER="102";
                makePhoneCall();

            }
        });
        findViewById(R.id.womenhelpline).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONE_NUMBER="1091";
                makePhoneCall();

            }
        });
        findViewById(R.id.domesticviolence).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PHONE_NUMBER="181";
                makePhoneCall();

            }
        });
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CALL_PHONE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CALL_PHONE}, REQUEST_PHONE_CALL);
        } else {
            if (!PHONE_NUMBER.equals("")) { // Replace "123456789" with the number you want to avoid calling
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse("tel:" + PHONE_NUMBER));
                startActivity(intent);
            }
        }
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_PHONE_CALL) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            }
        }
    }
}