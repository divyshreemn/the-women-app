package com.example.womensafetyapp;

import static com.example.womensafetyapp.R.id.nameEditText;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class ProfileActivity extends AppCompatActivity {
    private EditText nameEditText;
    private EditText phoneNumberEditText;
    private Button updateButton;
    private Button sendOtpButton;
    private EditText otpEditText;

    private FirebaseAuth firebaseAuth;
    private PhoneAuthProvider.OnVerificationStateChangedCallbacks verificationCallbacks;
    private String verificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase authentication instance
        firebaseAuth = FirebaseAuth.getInstance();

        // Initialize views
        nameEditText = findViewById(R.id.nameEditText);
        phoneNumberEditText = findViewById(R.id.phoneNumberEditText);
        updateButton = findViewById(R.id.updateButton);
        sendOtpButton = findViewById(R.id.sendOtpButton);
        otpEditText = findViewById(R.id.otpEditText);

        // Retrieve user data from SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        String username = sharedPreferences.getString("username", "");
        String phoneNumber = sharedPreferences.getString("phonenumber", "");

        // Set the retrieved data to the EditText fields
        nameEditText.setText(username);
        phoneNumberEditText.setText(phoneNumber);

        // Set click listener for the "Update" button
        updateButton.setOnClickListener(view -> {
            // Get updated data from EditText fields
            String updatedUsername = nameEditText.getText().toString();
            String updatedPhoneNumber = phoneNumberEditText.getText().toString();

            // Check if the phone number has changed
            if (!updatedPhoneNumber.equals(phoneNumber)) {
                // If the phone number has changed, send OTP for verification
                if (otpEditText.getText().toString().isEmpty()) {
                    Toast.makeText(ProfileActivity.this, "Please enter the OTP", Toast.LENGTH_SHORT).show();
                    return;
                }
                String otp = otpEditText.getText().toString();
                verifyOtp(otp, updatedUsername, updatedPhoneNumber);
            } else {
                // If the phone number has not changed, update the profile directly
                updateProfile(updatedUsername, updatedPhoneNumber);
            }
        });

        // Set click listener for the "Send OTP" button
        sendOtpButton.setOnClickListener(view -> {
            String updatedPhoneNumber = phoneNumberEditText.getText().toString();
            if (updatedPhoneNumber.length() != 10) {
                Toast.makeText(ProfileActivity.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
                return;
            }
            sendOtp("+91" + updatedPhoneNumber);
        });

        // Initialize the verification callbacks
        verificationCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                // Automatically handles the verification for a verified phone number
                String updatedUsername = nameEditText.getText().toString();
                String updatedPhoneNumber = phoneNumberEditText.getText().toString();
                updateProfile(updatedUsername, updatedPhoneNumber);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                // Handle verification failure
                Toast.makeText(ProfileActivity.this, "Verification failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCodeSent(String verificationId, PhoneAuthProvider.ForceResendingToken token) {
                // Save the verification ID and update the UI to enter the OTP
                ProfileActivity.this.verificationId = verificationId;
                Toast.makeText(ProfileActivity.this, "OTP has been sent to your mobile number", Toast.LENGTH_SHORT).show();
            }
        };
    }

    private void sendOtp(String phoneNumber) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,
                60, // Timeout duration
                TimeUnit.SECONDS,
                this,
                verificationCallbacks
        );
    }

    private void verifyOtp(String otp, String updatedUsername, String updatedPhoneNumber) {
        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(verificationId, otp);
        updateProfileWithPhoneCredential(credential, updatedUsername, updatedPhoneNumber);
    }

    private void updateProfileWithPhoneCredential(PhoneAuthCredential credential, String updatedUsername, String updatedPhoneNumber) {
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Phone authentication successful, proceed to update the profile
                            updateProfile(updatedUsername, updatedPhoneNumber);
                        } else {
                            // Phone authentication failed
                            Toast.makeText(ProfileActivity.this, "Phone authentication failed", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void updateProfile(String updatedUsername, String updatedPhoneNumber) {
        // Store the updated data in SharedPreferences
        SharedPreferences sharedPreferences = getSharedPreferences("userData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("username", updatedUsername);
        editor.putString("phonenumber", updatedPhoneNumber);
        editor.apply();

        // Display a toast message to indicate successful update
        Toast.makeText(ProfileActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
        otpEditText.setText("");
    }
}
