package com.example.neighborhoodhub;

import android.content.Intent;
import android.os.Bundle;

public class PostCategoryActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_category);
        setupTopBar(true);
        // Set click listeners for category buttons
        findViewById(R.id.categoryLocalQuestion).setOnClickListener(v -> navigateToDetails("Local Question"));
        findViewById(R.id.categoryLostFound).setOnClickListener(v -> navigateToDetails("Lost & Found"));
        findViewById(R.id.categorySafetyIncident).setOnClickListener(v -> navigateToDetails("Safety Incident"));
        findViewById(R.id.categoryEvent).setOnClickListener(v -> navigateToDetails("Event Announcement"));

    }

    // Onclick to send user to post details screen when user selects category
    // Send category to PostDetailsActivity page
    private void navigateToDetails(String category) {
        if (category.equals("Event Announcement")){
            // create calendar event
            Intent intent = new Intent(this, AddCalendarEventActivity.class);
            startActivity(intent);
        }
        else if (category.equals("Lost & Found")){
            // create lost and found item
            Intent intent = new Intent(this, AddMarketPlaceItemActivity.class);
            startActivity(intent);
        }
        else {
            Intent intent = new Intent(PostCategoryActivity.this, PostDetailsActivity.class);
            intent.putExtra("category", category); // Pass selected category to details screen
            startActivity(intent);
        }
    }



}
