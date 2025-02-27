package com.example.womensafetyapp;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class Login extends AppCompatActivity {
    private EditText mobileNoEditText;

    private EditText nameEditText;
    private CheckBox termCheckBox;
    private EditText otpEditText;
    private TextView sendOtpButton;
    private TextView backupbutton;
    private Button signUpButton;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize Firebase authentication instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        nameEditText=findViewById(R.id.name);
        termCheckBox=findViewById(R.id.term);
        mobileNoEditText = findViewById(R.id.mobileno);
        otpEditText = findViewById(R.id.otp);
        sendOtpButton = findViewById(R.id.sendotpbutton);
        signUpButton = findViewById(R.id.signup);
        backupbutton=findViewById(R.id.backup);


        backupbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //if otp dont work because of limit issue this can be used to signup to the app
                SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("username", nameEditText.getText().toString());
                editor.putString("phonenumber", mobileNoEditText.getText().toString());
                editor.apply();

                //redirecting to main page
                Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
                startActivity(intent);
                finish();
            }
        });

        // Set click listener for the "Send Otp" button
        sendOtpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(mobileNoEditText.getText().length()!=10){
                    Toast.makeText(Login.this, "Enter the correct mobile no of 10 digits", Toast.LENGTH_SHORT).show();
                    return;
                }
                String mobileNo = mobileNoEditText.getText().toString();
                sendOtp("+91"+mobileNo);
            }
        });

        // Set click listener for the "Sign up" button
        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(nameEditText.getText().toString().equals("")){
                    Toast.makeText(Login.this, "Enter your name", Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!termCheckBox.isChecked()){
                    Toast.makeText(Login.this, "Please accept the term and conditions.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String otp = otpEditText.getText().toString();
                verifyOtp(otp);
            }
        });

        // Initialize the verification callbacks
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // Automatically handles the verification for a verified phone number
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // Handle verification failure
                Toast.makeText(Login.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // Save the verification ID and update the UI to enter the OTP
                Login.this.verificationId = verificationId;
                Toast.makeText(Login.this, "OTP has been sent to your mobile number", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void sendOtp(String mobileNo) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                mobileNo,
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                verificationCallbacks
        );
    }

    private void verifyOtp(String otp) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        signInWithPhoneAuthCredential(credential);

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Phone authentication successful, proceed with your app logic
                            Toast.makeText(Login.this, "Phone authentication successful", Toast.LENGTH_SHORT).show();
                            //Storing username and contact number for further app use to shared prefrences

                            SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putString("username", nameEditText.getText().toString());
                            editor.putString("phonenumber", mobileNoEditText.getText().toString());
                            editor.apply();

                            //redirecting to main page
                            Intent intent=new Intent(getApplicationContext(),HomeActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Phone authentication failed
                            Toast.makeText(Login.this, "Phone authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}
