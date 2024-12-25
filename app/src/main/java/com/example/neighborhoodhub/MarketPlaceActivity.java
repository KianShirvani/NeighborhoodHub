package com.example.neighborhoodhub;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class MarketPlaceActivity extends BaseActivity {

    /* Set up Variables */
    RadioGroup lostAndFoundRadioGroup;
    GridView gridView;
    Button clearFilterButton;
    FloatingActionButton floatAddItemButton;

    /* Initialize List and Adapters */
    ArrayList<MarketPlaceItem> itemList = new ArrayList<>();
    ArrayList<MarketPlaceItem> filteredList = new ArrayList<>();
    ItemAdapter itemAdapter;

    /* Firebase connection */
    DatabaseReference root;
    StorageReference storeRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_market_place);
        setupTopBar(false);
        setupBottomNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Initialize variables and connections */
        lostAndFoundRadioGroup = findViewById(R.id.lostAndFoundRadioGroupId);
        gridView = findViewById(R.id.gridViewId);
        clearFilterButton = findViewById(R.id.clearFilterButtonId);
        floatAddItemButton = findViewById(R.id.floatingActionButtonId);

        /* Item Adapter initialization */
        itemAdapter = new ItemAdapter(this, itemList);
        gridView.setAdapter(itemAdapter);


        /* Firebase real database and storage setup */
        root = FirebaseDatabase.getInstance().getReference("Items");
        storeRoot = FirebaseStorage.getInstance().getReference("ItemImages");

        /* Load items data from Firebase */
        //loadData();
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MarketPlaceItem item = snapshot.getValue(MarketPlaceItem.class);
                    if (item != null) {
                        itemList.add(item);
                    } else {
                        Log.e("FirebaseError", "Failed to parse item: " + snapshot.getKey());
                    }
                }
                itemAdapter.notifyDataSetChanged(); // Notify adapter of data change
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });

        /* Radio Group Selection Listener */
        lostAndFoundRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                String filterType = "";
                if (checkedId == R.id.marketPlaceItemRadioButtonId) {
                    filterType = "Market Place Item";
                } else if (checkedId == R.id.lostPetRadioButtonId) {
                    filterType = "Lost Pet";
                } else if (checkedId == R.id.lostItemRadioButtonId) {
                    filterType = "Lost Item";
                }
                Log.d("RadioButton", "Selected Filter: " + filterType);  // Add this log
                filterData(filterType);
            }
        });


        /* Clear Selection on radio buttons function */
        clearFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                lostAndFoundRadioGroup.clearCheck();  // Deselect all radio buttons
                filterData(null);  // Show all items
            }
        });

        /* Floating Add Item Button leads to another item to the grid_view */
        floatAddItemButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MarketPlaceActivity.this, AddMarketPlaceItemActivity.class);
                startActivity(intent);
            }
        });
    }

    /* Load data from the Firebase */
    void loadData(){
        root.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                itemList.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    MarketPlaceItem item = snapshot.getValue(MarketPlaceItem.class);
                    if (item != null) {
                        itemList.add(item);
                    } else {
                        Log.e("FirebaseError", "Failed to parse item: " + snapshot.getKey());
                    }
                }
                filterData(null); // Display all items by default
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("FirebaseError", "Database error: " + databaseError.getMessage());
            }
        });
    }

    /* Implement filtering based on the selected radio button */
    private void filterData(String filter) {
        filteredList.clear();

        if (filter == null) {
            filteredList.addAll(itemList); // Show all items
        } else {
            for (MarketPlaceItem item : itemList) {
                if (item != null) {
                    String itemType = item.getItemType();
                    if (itemType != null && !itemType.isEmpty() && itemType.equals(filter)) {
                        filteredList.add(item);
                    }
                }
            }
        }

        // Create a new adapter with the updated filtered list
        itemAdapter = new ItemAdapter(this, filteredList);
        gridView.setAdapter(itemAdapter); // Re-set the adapter to update the GridView
    }

    /* MarketPlaceItem class implementation */
    public static class MarketPlaceItem {
        private String itemType;
        private String itemObject;
        private String missingRegion;
        private String contactPerson;
        private String mobileContact;
        private String imageUrl;
        private String userId;

        public MarketPlaceItem() {}

        public MarketPlaceItem(String itemType, String itemObject, String missingRegion, String contactPerson, String mobileContact, String imageUrl, String userId) {
            this.itemType = itemType;
            this.itemObject = itemObject;
            this.missingRegion = missingRegion;
            this.contactPerson = contactPerson;
            this.mobileContact = mobileContact;
            this.imageUrl = imageUrl;
            this.userId = userId;
        }

        // Getter and Setter methods
        public String getItemType() {
            return itemType;
        }

        public void setItemType(String itemType) {
            this.itemType = itemType;
        }

        public String getItemObject() {
            return itemObject;
        }

        public void setItemObject(String itemObject) {
            this.itemObject = itemObject;
        }

        public String getMissingRegion() {
            return missingRegion;
        }

        public void setMissingRegion(String missingRegion) {
            this.missingRegion = missingRegion;
        }

        public String getContactPerson() {
            return contactPerson;
        }

        public void setContactPerson(String contactPerson) {
            this.contactPerson = contactPerson;
        }

        public String getMobileContact() {
            return mobileContact;
        }

        public void setMobileContact(String mobileContact) {
            this.mobileContact = mobileContact;
        }

        public String getImageUrl() {
            return imageUrl;
        }

        public void setImageUrl(String imageUrl) {
            this.imageUrl = imageUrl;
        }

        public String getUserId() { return userId; }

        public void setUserId(String userId) { this.userId = userId; }
    }

    /* Custom adapter for the grid list */
    /* When moving the project to the repository, remember to add the dependencies to gradle app-level */
    // First add to Gradle Scripts/libs.versions.toml below lines in [libraries]
    // glide = { module = "com.github.bumptech.glide:glide", version = "4.15.1" }
    // glide-compiler = { module = "com.github.bumptech.glide:compiler", version = "4.15.1" }
    // Then add below lines to app-level build.gradle.kts file
    // implementation(libs.glide)
    // annotationProcessor(libs.glide.compiler)
    // Then sync project, import Glide
    // add this to Manifest file: <uses-permission android:name="android.permission.INTERNET" />
    private class ItemAdapter extends BaseAdapter {

        private Context context; // To store the context
        private ArrayList<MarketPlaceItem> filteredList; // To store the filtered list of items

        // Constructor to initialize the adapter
        public ItemAdapter(Context context, ArrayList<MarketPlaceItem> filteredList) {
            this.context = context;
            this.filteredList = filteredList;
        }

        @Override
        public int getCount() {
            return filteredList.size();
        }

        @Override
        public Object getItem(int position) {
            return filteredList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                // Inflate the grid item layout
                convertView = LayoutInflater.from(context).inflate(R.layout.grid_item_market_place, parent, false);
            }

            // Get the current item
            MarketPlaceItem currentItem = filteredList.get(position);

            // Bind data to views
            ImageView imageView = convertView.findViewById(R.id.itemImageId);
            TextView textView = convertView.findViewById(R.id.itemTitleId);

            String imageUrl = currentItem.getImageUrl();

            // Check if the image URL is not null and not empty
            if (imageUrl != null && !imageUrl.isEmpty()) {
                // Load the image using Glide
                Glide.with(context)
                        .load(imageUrl) // Firebase image URL
                        .error(R.drawable.left_arrow) // Fallback image
                        .into(imageView);
            } else {
                // If the image URL is null or empty, set the fallback image
                imageView.setImageResource(R.drawable.left_arrow); // Fallback image when URL is null
            }

            // Set the text (e.g., Animal Type)
            textView.setText(currentItem.getItemObject());

            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, MarketPlaceItemDetailActivity.class);
                    intent.putExtra("imageUrl", currentItem.getImageUrl());
                    intent.putExtra("itemType", currentItem.getItemType());
                    intent.putExtra("itemObject", currentItem.getItemObject());
                    intent.putExtra("missingRegion", currentItem.getMissingRegion());
                    intent.putExtra("contactPerson", currentItem.getContactPerson());
                    intent.putExtra("mobileContact", currentItem.getMobileContact());
                    intent.putExtra("userId", currentItem.getUserId());
                    context.startActivity(intent);
                }
            });

            return convertView;
        }
    }

}