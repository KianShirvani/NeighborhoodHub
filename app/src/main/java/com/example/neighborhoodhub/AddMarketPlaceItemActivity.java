package com.example.neighborhoodhub;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class AddMarketPlaceItemActivity extends BaseActivity {

    Button uploadImageButton, submitButton;
    Spinner itemTypeSpinner;
    EditText itemDescriptionEditText, locationEditText, contactNameEditText, contactMobileEditText;
    TextView selectedFileNameTextView;
    Uri imageUri;
    String urlImage;
    StorageReference rootStorage;
    DatabaseReference root;
    ActivityResultLauncher<Intent> activityResultLauncher;
    /* Prototype USER_ID that is stored to Firebase Events -> Event -> "userId" key */
    private static final String USER_ID = "PROTOTYPE_USER_1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_add_market_place_item);
        setupTopBar(true);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Initialize components */
        uploadImageButton = findViewById(R.id.uploadImageButtonId);
        itemTypeSpinner = findViewById(R.id.itemTypeSpinnerId);
        itemDescriptionEditText = findViewById(R.id.itemDescriptionEditTextId);
        locationEditText = findViewById(R.id.locationEditTextId);
        contactNameEditText = findViewById(R.id.contactNameEditTextId);
        contactMobileEditText = findViewById(R.id.contactMobileEditTextId);
        submitButton = findViewById(R.id.submitButtonId);
        selectedFileNameTextView = findViewById(R.id.selectedFileNameTextViewId);

        /* Initialize Firebase */
        rootStorage = FirebaseStorage.getInstance().getReference("ItemImages");
        root = FirebaseDatabase.getInstance().getReference("Items");

        /* Initialize the ActivityResultLauncher */
        activityResultLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        imageUri = result.getData().getData();
                        selectedFileNameTextView.setText(imageUri != null ? imageUri.getLastPathSegment() : null);
                    }
                }
        );

        /* Set up Spinner using a simple array */
        String[] itemTypes = {"Lost Pet", "Lost Item", "Market Place Item"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                itemTypes
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        itemTypeSpinner.setAdapter(adapter);


        /* Submit Button Funtionality */
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputFields()) {
                    uploadImageToFirebase();
                }
                //Intent intent = new Intent(AddMarketPlaceItemActivity.this, MarketPlaceActivity.class);
                //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                //startActivity(intent);
            }
        });

        /* Upload Image Button Functionality */
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openFileChooser();
            }
        });

        /* backButton Functionality */
    }

    /* openFileChooser for uploadImageButton */
    void openFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        activityResultLauncher.launch(intent);
    }

    /* Validate if all the information and upload image are complete before user could hit submit button */
    private boolean validateInputFields() {
        if (TextUtils.isEmpty(itemDescriptionEditText.getText())) {
            Toast.makeText(this, "Please fill out the item description", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(locationEditText.getText())) {
            Toast.makeText(this, "Please fill out the location", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(contactNameEditText.getText())) {
            Toast.makeText(this, "Please fill out the contact person name", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(contactMobileEditText.getText())) {
            Toast.makeText(this, "Please fill out the contact mobile", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (imageUri == null) {
            Toast.makeText(this, "Please select an image to upload", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void uploadImageToFirebase() {
        if (imageUri != null) {
            String fileName = UUID.randomUUID().toString();
            StorageReference fileReference = rootStorage.child(fileName);

            fileReference.putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> fileReference.getDownloadUrl().addOnSuccessListener(uri -> {
                        urlImage = uri.toString();
                        saveItemToFirebase();
                    }))
                    .addOnFailureListener(e -> Toast.makeText(this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        }
    }

    /* Method to save item to Firebase */
    void saveItemToFirebase() {
        // Get values from input fields
        String description = itemDescriptionEditText.getText().toString().trim();
        String location = locationEditText.getText().toString().trim();
        String contactName = contactNameEditText.getText().toString().trim();
        String contactMobile = contactMobileEditText.getText().toString().trim();
        String itemType = itemTypeSpinner.getSelectedItem().toString();

        // Reference to Firebase Storage
        String fileName = System.currentTimeMillis() + ".jpg";
        StorageReference fileRef = FirebaseStorage.getInstance().getReference("uploads").child(fileName);

        // Upload the image to Firebase Storage
        fileRef.putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();

                    // Create a MarketPlaceItem object
                    MarketPlaceItem item = new MarketPlaceItem(itemType, description, location, contactName, contactMobile, imageUrl, USER_ID);

                    // Save to Firebase Database
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("Items");
                    String itemId = databaseRef.push().getKey(); // Generate a unique key for the item
                    if (itemId != null) {
                        databaseRef.child(itemId).setValue(item)
                                .addOnCompleteListener(task -> {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
                                        clearFields(); // Clear the fields after saving

                                        /* Bug fix: direct to previous activity here */
                                        Intent intent = new Intent(AddMarketPlaceItemActivity.this, MarketPlaceActivity.class);
                                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                        startActivity(intent);
                                    } else {
                                        Toast.makeText(this, "Failed to save item", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                }))
                .addOnFailureListener(e -> Toast.makeText(this, "Failed to upload image: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    /* Method to clear the EditText fields after data is saved */
    void clearFields() {
        itemDescriptionEditText.setText("");
        locationEditText.setText("");
        contactNameEditText.setText("");
        contactMobileEditText.setText("");
        selectedFileNameTextView.setText("");  // Clear the file name TextView
        imageUri = null; // Reset the image URI
        itemTypeSpinner.setSelection(0); // Reset spinner to the first item (optional)
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

        public String getUserId(){ return userId; }

        public void setUserId(String userId) { this.userId = userId; }
    }
}