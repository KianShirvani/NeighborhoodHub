package com.example.neighborhoodhub;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;

public class MarketPlaceItemDetailActivity extends BaseActivity {

    ImageView itemImageView;
    TextView itemTypeTextView, itemObjectTextView, missingRegionTextView, contactPersonTextView, mobileContactTextView, userIdTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_market_place_item_detail);

        setupTopBar(false);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        itemImageView = findViewById(R.id.itemImageViewId);
        itemTypeTextView = findViewById(R.id.itemTypeTextViewId);
        itemObjectTextView = findViewById(R.id.itemObjectTextViewId);
        missingRegionTextView = findViewById(R.id.missingRegionTextViewId);
        contactPersonTextView = findViewById(R.id.contactPersonTextViewId);
        mobileContactTextView = findViewById(R.id.mobileContactTextViewId);
        userIdTextView = findViewById(R.id.userIdTextViewId);

        // Get the data passed from the previous activity
        Intent intent = getIntent();
        String imageUrl = intent.getStringExtra("imageUrl");
        String itemType = intent.getStringExtra("itemType");
        String itemObject = intent.getStringExtra("itemObject");
        String missingRegion = intent.getStringExtra("missingRegion");
        String contactPerson = intent.getStringExtra("contactPerson");
        String mobileContact = intent.getStringExtra("mobileContact");
        String userId = intent.getStringExtra("userId");

        // Set the data to the views
        Glide.with(this).load(imageUrl).into(itemImageView);
        itemTypeTextView.setText(itemType);
        itemObjectTextView.setText(itemObject);
        missingRegionTextView.setText(missingRegion);
        contactPersonTextView.setText(contactPerson);
        mobileContactTextView.setText(mobileContact);
        userIdTextView.setText(userId);

    }
}