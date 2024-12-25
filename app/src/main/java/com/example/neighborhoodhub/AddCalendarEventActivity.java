package com.example.neighborhoodhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.core.graphics.Insets;
import androidx.core.net.ParseException;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AddCalendarEventActivity extends BaseActivity {

    /* Views variables */
    Button submitButton;
    EditText eventTitleEditText, dateEditText, locationNameEditText, locationAddressEditText, timeEditText;
    /* Prototype USER_ID that is stored to Firebase Events -> Event -> "userId" key */
    private static final String USER_ID = "PROTOTYPE_USER_1";

    /* Firebase variables */
    DatabaseReference root, subroot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_calendar_event);
        setupTopBar(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Find views by IDs */
        submitButton = findViewById(R.id.submitButtonId);
        eventTitleEditText = findViewById(R.id.eventTitleEditTextId);
        dateEditText = findViewById(R.id.dateEditTextId);
        timeEditText = findViewById(R.id.timeEditTextId);
        locationNameEditText = findViewById(R.id.locationNameEditTextId);
        locationAddressEditText = findViewById(R.id.locationAddressEditTextId);

        /* Connecting to firebase */
        root = FirebaseDatabase.getInstance().getReference();
        /* Get to the Events root in the firebase */
        subroot = root.child("Events");
        MaterialDatePicker<Long> datePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Select date").build();
        SimpleDateFormat inputFormat = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
        datePicker.addOnPositiveButtonClickListener(selection -> {
                    try {
                        Date date = inputFormat.parse(datePicker.getHeaderText());
                        String outputDate = outputFormat.format(date);
                        dateEditText.setText(outputDate);
                    } catch (java.text.ParseException e) {
                        Toast.makeText(AddCalendarEventActivity.this, "Error parsing date: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        dateEditText.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                datePicker.show(getSupportFragmentManager(), "DATE_PICKER");
                }
        });

        Format formatter = new SimpleDateFormat("HH:mm", Locale.getDefault());
        MaterialTimePicker.Builder timePicker = new MaterialTimePicker.Builder();
        MaterialTimePicker picker = timePicker.setHour(12).setMinute(10).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).setTimeFormat(TimeFormat.CLOCK_24H).build();
        picker.addOnPositiveButtonClickListener(selection -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, picker.getHour());
            calendar.set(Calendar.MINUTE, picker.getMinute());
            timeEditText.setText(formatter.format(calendar.getTime()));
        });
        timeEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                picker.show(getSupportFragmentManager(), "TIME_PICKER");
            }
        });

        /* Buttons functionality */
        submitButton.setOnClickListener(new SubmitButtonClickListener());
    }

    class SubmitButtonClickListener implements View.OnClickListener{
        @Override
        public void onClick(View view){
            /* Initialize variables for use */
            Toast toast;
            int duration = Toast.LENGTH_LONG;
            CharSequence fillEventTitle = "Please enter the event title";
            CharSequence fillDateTime = "Please enter the date and time of event";
            CharSequence fillLocationName = "Please enter the location name of the event";
            CharSequence fillLocationAddress = "Please enter the location address of the event";
            boolean canProceedToSaveData = false;

            /* Extract the text user put into the EditText views */
            String eventTitle = eventTitleEditText.getText().toString();
            String date = dateEditText.getText().toString();
            String time = timeEditText.getText().toString();
            String locationName = locationNameEditText.getText().toString();
            String locationAddress = locationAddressEditText.getText().toString();

            if(eventTitle.length() < 8){
                toast = Toast.makeText(getApplicationContext(), fillEventTitle, duration);
                toast.show();
            }else if(date.length() < 8 || time.length() < 4){
                toast = Toast.makeText(getApplicationContext(), fillDateTime, duration);
                toast.show();
            }else if(locationName.length() < 8){
                toast = Toast.makeText(getApplicationContext(), fillLocationName, duration);
                toast.show();
            }else if(locationAddress.length() < 8){
                toast = Toast.makeText(getApplicationContext(), fillLocationAddress, duration);
                toast.show();
            }else{
                canProceedToSaveData = true;
            }

            /* If can proceed, save the data to Firebase */
            if(canProceedToSaveData){
                DatabaseReference currentData = subroot.child(eventTitle);
                currentData.child("dateTime").setValue(date + " " + time);
                currentData.child("locationAddress").setValue(locationAddress);
                currentData.child("locationName").setValue(locationName);
                /* Set user id to firebase key "userId" that stores userId information */
                currentData.child("userId").setValue(USER_ID);
                /* Return to previous activity */
                Intent intent = new Intent(AddCalendarEventActivity.this, CalendarViewActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }
    }
}