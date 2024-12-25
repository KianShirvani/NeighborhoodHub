package com.example.neighborhoodhub;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neighborhoodhub.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class EditPostActivity extends BaseActivity {
    /*TODO: add a dialog box to confirm exit
       dialog: https://github.com/material-components/material-components-android/blob/master/docs/components/Dialog.md
     */
    private EditText editTextDescription, editTextLocation;
    private Button buttonSavePost;
    private DatabaseReference databaseReference;

    private String postId;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_post);
        setupTopBar(true);
        // Initialize views
        editTextDescription = findViewById(R.id.editTextPostDescription);
        editTextLocation = findViewById(R.id.editTextPostLocation);
        buttonSavePost = findViewById(R.id.buttonSavePost);

        // Retrieve Intent data
        postId = getIntent().getStringExtra("postId");
        userId = getIntent().getStringExtra("userId");
        String description = getIntent().getStringExtra("description");
        String location = getIntent().getStringExtra("location");

        if (postId == null || userId == null) {
            Toast.makeText(this, "Error loading post. Missing data.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields with post data
        editTextDescription.setText(description);
        editTextLocation.setText(location);

        // Firebase reference for the post
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("posts")
                .child(userId)
                .child(postId);

        // Save button listener
        buttonSavePost.setOnClickListener(v -> savePostEdits());
    }

    private void savePostEdits() {
        String updatedDescription = editTextDescription.getText().toString().trim();
        String updatedLocation = editTextLocation.getText().toString().trim();

        if (updatedDescription.isEmpty() || updatedLocation.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        // Push the updates to Firebase
        databaseReference.child("description").setValue(updatedDescription);
        databaseReference.child("location").setValue(updatedLocation)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Post updated successfully!", Toast.LENGTH_SHORT).show();
                        finish(); // Close the activity and go back
                    } else {
                        Toast.makeText(this, "Failed to update post.", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
