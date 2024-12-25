package com.example.neighborhoodhub;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.neighborhoodhub.Post;
import com.example.neighborhoodhub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.UUID;

public class PostDetailsActivity extends BaseActivity {
    // Firebase Storage Reference
    StorageReference storageReference;
    Uri image;
    Button selectImage, sharePost;
    ImageView imageView;
    private EditText editTextDescription, editTextLocation;
    private TextView editCategoryLabel;
    private String category; // Selected category from the previous screen
    private DatabaseReference databaseReference;
    private static final String USER_ID = "PROTOTYPE_USER_1"; // Hardcoded userId for the prototype
    //USER ID MUST BE CHANGED IN PostDetailsActivity and MyPosts to access that user's view

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_details);
        setupTopBar(true);

        // Initialize views
        editTextDescription = findViewById(R.id.editTextPostDescription);
        editTextLocation = findViewById(R.id.editTextPostLocation);
        editCategoryLabel = findViewById(R.id.categoryLabel);
        selectImage = findViewById(R.id.buttonSelectImage);
        sharePost = findViewById(R.id.buttonSharePost);
        imageView = findViewById(R.id.imagePlaceholder);

        // Retrieve the user's selected category from previous screen
        Intent intent = getIntent();
        category = intent.getStringExtra("category");

        // Set category label to user's selected option
        editCategoryLabel.setText(String.format("Selected Category: %s", category));

        // Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("posts");
        storageReference = FirebaseStorage.getInstance().getReference();

        // OnClickListener for selecting an image
        selectImage.setOnClickListener(view -> {
            Intent selectIntent = new Intent(Intent.ACTION_PICK);
            selectIntent.setType("image/*");
            activityResultLauncher.launch(selectIntent);
        });

        // OnClickListener for sharing the post
        sharePost.setOnClickListener(view -> sharePost());

    }

    // Activity Result Launcher for image selection
    private final ActivityResultLauncher<Intent> activityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    image = result.getData().getData();
                    Glide.with(this).load(image).into(imageView);
                    Toast.makeText(PostDetailsActivity.this, "Image selected", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(PostDetailsActivity.this, "Please select an image", Toast.LENGTH_SHORT).show();
                }
            }
    );

    private void uploadImageAndSavePost(String description, String location, String postId) {
        if (image == null) {
            // If no image is selected, proceed without image
            savePost(description, location, "", postId);
            return;
        }

        // Generate a unique filename for the image
        StorageReference ref = storageReference.child("images/" + UUID.randomUUID().toString());
        ref.putFile(image)
                .addOnSuccessListener(taskSnapshot -> ref.getDownloadUrl().addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    savePost(description, location, imageUrl, postId);
                }))
                .addOnFailureListener(e -> Toast.makeText(PostDetailsActivity.this, "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void savePost(String description, String location, String imageUrl, String postId) {
        // Create a new Post object
        Post post = new Post(category, description, location);
        post.setPostId(postId); // Set the postId
        post.setMediaUrl(imageUrl); // Set the image URL

        // Save the post to Firebase Database
        DatabaseReference newPostRef = databaseReference.child(USER_ID).child(postId);
        newPostRef.setValue(post).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Toast.makeText(this, "Post shared successfully!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(PostDetailsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to share post.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void sharePost() {
        String description = editTextDescription.getText().toString().trim();
        String location = editTextLocation.getText().toString().trim();

        // Validate inputs
        if (description.isEmpty() || location.isEmpty() || category == null) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Generate a new post ID
        String postId = databaseReference.child(USER_ID).push().getKey();

        if (postId == null) {
            Toast.makeText(this, "Failed to generate post ID.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Upload image (if selected) and save post
        uploadImageAndSavePost(description, location, postId);
        Intent intent = new Intent(PostDetailsActivity.this, MyPosts.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    //Onclick code for Cancel button to allow user to confirm cancellation
    public void popupConfirmation(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Action");
        builder.setMessage("Are you sure you want to delete the post you are creating? (This setting cannot be undone)");

        // Positive Button (Yes action)
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Perform the cancellation action
                Toast.makeText(getApplicationContext(), "Action Cancelled", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish(); //Go back to home screen
            }
        });
        // Negative Button (No action)
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
