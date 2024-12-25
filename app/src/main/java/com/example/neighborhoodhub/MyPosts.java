package com.example.neighborhoodhub;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.neighborhoodhub.EditPostActivity;
import com.example.neighborhoodhub.Post;
import com.example.neighborhoodhub.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyPosts extends BaseActivity {

    private ImageView postMedia;
    private TextView postDescription, postLocation;
    private Button buttonNext;
    private Button buttonPrevious;
    protected static final ArrayList<Post> myPosts = new ArrayList<>();
    private int currentIndex = 0; // Keeps track of the current post index
    private static final String USER_ID = "PROTOTYPE_USER_1"; // Hardcoded userId for the prototype

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_posts);
        setupTopBar(true);
        // Initialize UI components
        postMedia = findViewById(R.id.postMedia);
        postDescription = findViewById(R.id.postDescription);
        postLocation = findViewById(R.id.postLocation);
        buttonNext = findViewById(R.id.button_next);
        buttonPrevious = findViewById(R.id.button_previous);
        Button buttonCreate = findViewById(R.id.buttonCreate);
        // Fetch the user's posts from Firebase
        fetchUserPosts();

        // Set up button click listeners
        buttonNext.setOnClickListener(v -> showNextPost());
        buttonPrevious.setOnClickListener(v -> showPreviousPost());
        buttonCreate.setOnClickListener(v -> {
            Intent intent = new Intent(this, PostCategoryActivity.class);
            startActivity(intent);
        });

    }

    private void fetchUserPosts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("posts").child(USER_ID); // Navigate directly to the user's posts

        database.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                myPosts.clear(); // Clear the list to avoid duplicates

                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                    // Fetch each post for the specific user
                    Post post = postSnapshot.getValue(Post.class);
                    if (post != null) {
                        post.setUserId(USER_ID); // Assign user ID to the post
                        myPosts.add(post);
                    }
                }

                // Refresh UI
                if (!myPosts.isEmpty()) {
                    displayPost(0);
                } else {
                    Toast.makeText(MyPosts.this, "No posts found for this user.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MyPosts.this, "Failed to fetch posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displayPost(int index) {
        // Check if index is in valid range
        if (myPosts.isEmpty() || index < 0 || index >= myPosts.size()) return;

        // Retrieve index post details
        Post post = myPosts.get(index);

        // Set the post details
        postDescription.setText(String.format("%s - %s", post.getCategory(), post.getDescription()));
        postLocation.setText(post.getLocation());

        // Load media using Glide
        if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
            Glide.with(this)
                    .load(post.getMediaUrl())
                    .placeholder(R.drawable.person) // Placeholder image while loading
                    .error(R.drawable.error_icon) // Error image if the URL fails
                    .into(postMedia);
        } else {
            postMedia.setImageResource(R.drawable.person);
        }

        // Button constraints so user knows which one is available to click
        buttonPrevious.setEnabled(index > 0);
        buttonNext.setEnabled(index < myPosts.size() - 1);
    }

    private void showNextPost() {
        if (currentIndex < myPosts.size() - 1) {
            currentIndex++;
            displayPost(currentIndex);
        }
    }

    private void showPreviousPost() {
        if (currentIndex > 0) {
            currentIndex--;
            displayPost(currentIndex);
        }
    }

    //EDIT POST INTENT

    public void handleEditPost(View view) {
        // Check if there are posts to edit
        if (myPosts.isEmpty() || currentIndex < 0 || currentIndex >= myPosts.size()){
            Toast.makeText(this, "No posts to edit.", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(this, EditPostActivity.class);
        Post currentPost = myPosts.get(currentIndex);
        intent.putExtra("postId", currentPost.getPostId());
        intent.putExtra("userId", currentPost.getUserId());
        intent.putExtra("description", currentPost.getDescription());
        intent.putExtra("location", currentPost.getLocation());
        startActivity(intent);
    }

    public void refreshFeed(View view) {
        myPosts.clear(); //Clear posts
        currentIndex = 0;

        fetchUserPosts();
    }

    //CODE FOR USERS TO DELETE POSTS FROM DB
    public void deletePost(View view) {
        // Check if there are posts to delete
        if (myPosts.isEmpty() || currentIndex < 0 || currentIndex >= myPosts.size()) {
            Toast.makeText(this, "No posts to delete.", Toast.LENGTH_SHORT).show();
            return;
        }
        // Get the current post
        Post post = myPosts.get(currentIndex);

        // Show confirmation dialog
        new AlertDialog.Builder(this)
                .setTitle("Delete Post")
                .setMessage("Are you sure you want to delete this post? This action cannot be undone.")
                .setPositiveButton("Confirm", (dialog, which) -> {
                    // User selects yes so delete post from FireBase
                    DatabaseReference postRef = FirebaseDatabase.getInstance()
                            .getReference("posts")
                            .child(post.getUserId())
                            .child(post.getPostId());

                    postRef.removeValue()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    Toast.makeText(this, "Post deleted successfully.", Toast.LENGTH_SHORT).show();
                                    myPosts.remove(currentIndex);
                                    currentIndex = Math.max(0, currentIndex - 1); // check index range
                                    if (!myPosts.isEmpty()) {
                                        displayPost(currentIndex); // Show next post
                                    } else {
                                        // Clear UI if no posts left
                                        postDescription.setText("");
                                        postLocation.setText("");
                                        postMedia.setImageResource(R.drawable.person);
                                        buttonNext.setEnabled(false);
                                        buttonPrevious.setEnabled(false);
                                    }
                                } else {
                                    Toast.makeText(this, "Failed to delete post.", Toast.LENGTH_SHORT).show();
                                }
                            });
                })
                .setNegativeButton("Cancel", null)
                .show();

    }
}