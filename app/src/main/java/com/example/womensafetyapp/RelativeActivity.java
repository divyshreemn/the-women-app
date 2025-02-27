package com.example.womensafetyapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.Manifest;

public class RelativeActivity extends AppCompatActivity {
    private SQLiteOpenHelper dbHelper;
    private SQLiteDatabase db;

    private TableLayout tableLayout;
    private Button addButton;

    private static final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    String phonenum="";
    SharedPreferences sharedPreferences;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_relative);

        //Initialize the shared prefrences

        sharedPreferences=  getSharedPreferences("userData", Context.MODE_PRIVATE);
        // Initialize the views
        tableLayout = findViewById(R.id.tableLayout);
        addButton = findViewById(R.id.addButton);

        // Instantiate the SQLiteOpenHelper
        dbHelper = new DatabaseHelper(this);

        // Load and display the data
        loadData();

        // Add new data
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAddDataDialog();
            }
        });
    }

    private void loadData() {
        db = dbHelper.getReadableDatabase();

        // Query the database
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_NAME,
                null,
                null,
                null,
                null,
                null,
                null
        );

        // Clear the existing table rows
        tableLayout.removeAllViews();

        // Iterate through the cursor and add rows to the table
        while (cursor.moveToNext()) {
            final String parentName = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_PARENT_NAME));
            final String number = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_NUMBER));

            // Inflate the row layout
            TableRow row = (TableRow) LayoutInflater.from(this).inflate(R.layout.table_row_layout, null);

            // Set the data in the row views
            TextView parentNameTextView = row.findViewById(R.id.parentNameTextView);
            parentNameTextView.setText(parentName);

            TextView numberTextView = row.findViewById(R.id.numberTextView);
            numberTextView.setText(number);

            ImageButton deleteButton = row.findViewById(R.id.deleteButton);
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Delete the data from the database
                    db = dbHelper.getWritableDatabase();
                    db.delete(
                            DatabaseHelper.TABLE_NAME,
                            DatabaseHelper.COLUMN_PARENT_NAME + " = ? AND " + DatabaseHelper.COLUMN_NUMBER + " = ?",
                            new String[]{parentName, number}
                    );

                    // Reload the data and update the table
                    loadData();

                    Toast.makeText(RelativeActivity.this, "Data deleted successfully", Toast.LENGTH_SHORT).show();
                }
            });

            // Add the row to the table
            tableLayout.addView(row);
        }

        cursor.close();
    }

    private void showAddDataDialog() {
        // Inflate the dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_data, null);

        // Create the AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(dialogView);
        builder.setTitle("Add Data");

        final EditText parentNameEditText = dialogView.findViewById(R.id.parentNameEditText);
        final EditText numberEditText = dialogView.findViewById(R.id.numberEditText);

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String parentName = parentNameEditText.getText().toString();
                String number = numberEditText.getText().toString();

                if(!alphabetOnly(parentName)){
                    Toast.makeText(RelativeActivity.this, "Parent name contains alphabet only", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(parentName.length()>15){
                    Toast.makeText(RelativeActivity.this, "Parent Name can be of 15 letters only", Toast.LENGTH_SHORT).show();
                    return;
                }
                else if(number.length()!=10){
                    Toast.makeText(RelativeActivity.this, "Enter proper 10 digit mobile no", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Insert the new data into the database
                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(DatabaseHelper.COLUMN_PARENT_NAME, parentName);
                values.put(DatabaseHelper.COLUMN_NUMBER, number);
                db.insert(DatabaseHelper.TABLE_NAME, null, values);

                // Reload the data and update the table
                loadData();

                Toast.makeText(RelativeActivity.this, "Data added successfully", Toast.LENGTH_SHORT).show();
                phonenum=number;

                sendSMS(phonenum,"Hey "+sharedPreferences.getString("username","")+" added you to their relative list in women safety app");

            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
    public static boolean alphabetOnly(String input) {
        // Iterate through each character of the string
        for (int i = 0; i < input.length(); i++) {
            char ch = input.charAt(i);

            // Check if the character is not an alphabet
            if (!Character.isLetter(ch)) {
                return false;
            }
        }

        // All characters are alphabets
        return true;
    }
    private void sendSMS(String phoneNumber, String message) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else {
            sendSMSInternal(phoneNumber, message);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_PERMISSIONS_REQUEST_SEND_SMS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, send the SMS
                sendSMSInternal(phonenum, "Hey " + sharedPreferences.getString("username", "") + " added you to their relative list in the women safety app");
            } else {
                // Permission denied
                Toast.makeText(this, "SMS permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }


    private void sendSMSInternal(String phoneNumber, String message) {
        try {
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
            Toast.makeText(this, "SMS sent successfully", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            Toast.makeText(this, "Failed to send SMS", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}