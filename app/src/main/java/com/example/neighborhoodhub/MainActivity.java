package com.example.neighborhoodhub;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.bumptech.glide.Glide;
import com.example.neighborhoodhub.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MainActivity extends BaseActivity {

    private ImageView postMedia;
    private TextView postDescription, postLocation;
    private Button buttonNext, buttonPrevious;
    private DatabaseReference database;
    private ArrayList<Post> posts = new ArrayList<>();
    private int currentIndex = 0; // Keeps track of the current post index
    private DrawerLayout drawerLayout;
    private NavigationView navigationDrawer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Retrieve fields
        postMedia = findViewById(R.id.postMedia);
        postDescription = findViewById(R.id.postDescription);
        postLocation = findViewById(R.id.postLocation);
        buttonNext = findViewById(R.id.button_next);
        buttonPrevious = findViewById(R.id.button_previous);
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationDrawer = findViewById(R.id.drawer);
        setupTopBar(false);
        setupBottomNavigation();
        // Fetch posts from Firebase
        fetchPosts();
    }


    private void fetchPosts() {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference("posts");

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                posts.clear(); // Clear old data

                // Iterate through all users
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    // Iterate through each user's posts
                    for (DataSnapshot postSnapshot : userSnapshot.getChildren()) {
                        Post post = postSnapshot.getValue(Post.class);
                        if (post != null) {
                            post.setPostId(userSnapshot.getKey()); // Set the user ID
                            posts.add(post); // Add to the list
                        }
                    }
                }

                if (!posts.isEmpty()) {
                    displayPost(0); // Show first post
                } else {
                    Toast.makeText(MainActivity.this, "No posts available.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Toast.makeText(MainActivity.this, "Failed to load posts: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void displayPost(int index) {
        // Check if index is in valid range
        if (posts.isEmpty() || index < 0 || index >= posts.size()) return;

        // Retrieve post details
        Post post = posts.get(index);

        // Set description and location
        postDescription.setText(String.format("%s - %s", post.getCategory(), post.getDescription()));
        postLocation.setText(post.getLocation());

        // Load media using Glide
        if (post.getMediaUrl() != null && !post.getMediaUrl().isEmpty()) {
            Glide.with(this)
                    .load(post.getMediaUrl())
                    .placeholder(R.drawable.person) // Placeholder while loading
                    .error(R.drawable.error_icon) // Error image if the URL is invalid
                    .into(postMedia);
        } else {
            postMedia.setImageResource(R.drawable.person); // Set placeholder if no media URL
        }

        // Button constraints to indicate available navigation options
        buttonPrevious.setEnabled(index > 0);
        buttonNext.setEnabled(index < posts.size() - 1);
    }


    public void showNextPost(View view) {
        if (currentIndex < posts.size() - 1) {
            currentIndex++;
            displayPost(currentIndex);
        }
    }

    public void showPreviousPost(View view) {
        if (currentIndex > 0) {
            currentIndex--;
            displayPost(currentIndex);
        }
    }

    public void refreshFeed(View view) {
        posts.clear(); //Clear posts
        currentIndex = 0;

        fetchPosts();
    }

}
