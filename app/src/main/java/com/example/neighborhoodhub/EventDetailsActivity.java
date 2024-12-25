package com.example.neighborhoodhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.TimeZone;

public class EventDetailsActivity extends BaseActivity {

    TextView communityTextView, eventTitleTextView, dateTimeTextView, locationNameTextView, locationAddressTextView, userIdTextView;
    String timeZoneDisplayName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_event_details);
        setupTopBar(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Get the time zone on user phone */
        TimeZone timeZone = TimeZone.getDefault();
        timeZoneDisplayName = timeZone.getDisplayName();

        /* Get all the Views by ID */

        eventTitleTextView = findViewById(R.id.eventTitleTextViewId);
        dateTimeTextView = findViewById(R.id.dateTimeTextViewId);
        locationNameTextView = findViewById(R.id.locationNameTextViewId);
        locationAddressTextView = findViewById(R.id.locationAddressTextViewId);
        userIdTextView = findViewById(R.id.userIdTextViewId);

        /* Get the putextra intent information from previous activity */
        Event event = (Event) getIntent().getSerializableExtra("selectedEvent");

        /* setText in the relevant TextViews using the event object passed from previous activity */
        if(event != null){
            userIdTextView.setText(event.getUserId());
            eventTitleTextView.setText(event.getTitle());
            String dateTimeZone = event.getDateTime() + " " + timeZoneDisplayName;
            dateTimeTextView.setText(dateTimeZone);
            locationNameTextView.setText(event.getLocationName());
            locationAddressTextView.setText(event.getLocationAddress());
        }

        /* Make the locationAddress TextView clickable, and links to Google Map */
        locationAddressTextView.setClickable(true);
        locationAddressTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Get the address string */
                String address = event.getLocationAddress();

                Uri geoUri = Uri.parse("https://www.google.com/maps/search?q=" + Uri.encode(address));

                /* Create an intent to open the Google Maps app */
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, geoUri);

                /* Specify Google Maps package to directly open Google Maps app if installed */
                mapIntent.setPackage("com.google.android.apps.maps");

                /* Try to open Google Maps */
                if (mapIntent.resolveActivity(getPackageManager()) != null) {
                    startActivity(mapIntent); // Open with Google Maps app
                } else {
                    /* Fallback: Open the URL in the browser if Google Maps app is not installed */
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, geoUri);
                    startActivity(browserIntent); // Open in browser
                }
            }
        });

    }

}