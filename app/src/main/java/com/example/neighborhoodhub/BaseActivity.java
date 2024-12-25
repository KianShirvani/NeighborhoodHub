package com.example.neighborhoodhub;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public abstract class BaseActivity extends AppCompatActivity {
    @Override
    protected void onResume(){
        super.onResume();
        resetActiveIndicator();
    }
    protected void setupTopBar(Boolean setConfirmation) {
        MaterialToolbar topAppBar = findViewById(R.id.topAppBar);
        if (topAppBar == null) {
            return;
        }
        String currentActivityName = this.getClass().getSimpleName();
        topAppBar.setNavigationOnClickListener(item -> {
            if (currentActivityName.equals("MainActivity")) {
                DrawerLayout drawerLayout = findViewById(R.id.drawer_layout);
                drawerLayout.openDrawer(GravityCompat.START);

            } else {
//                return to calling activity
                if(setConfirmation){
                    AlertDialog.Builder builder = getBuilder("back");
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else
                    finish();
            }
        });
        topAppBar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.top_home) {
                AlertDialog.Builder builder = getBuilder("home");
                AlertDialog dialog = builder.create();
                dialog.show();
                return true;
            }
            return false;
        });

    }

    private AlertDialog.Builder getBuilder(String type) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirm Action");
        builder.setMessage("Any changes will be lost. Are you sure you want to leave?");

        // Positive Button (Yes action)
        builder.setPositiveButton("Confirm", (dialog, which) -> {
            // Perform the cancellation action
            dialog.dismiss();
            if(type.equals("home")){
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
            finish(); //Go back to home screen
        });
        // Negative Button (No action)
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        return builder;
    }

    protected void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            // return if no bottom navigation view is found
            return;
        }
        String currentActivityName = resetActiveIndicator();

        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.nav_post) {
                Intent intent = new Intent(this, PostCategoryActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_home) {
                //if current activity is MainActivity, do nothing
                if (currentActivityName.equals("MainActivity")) {
                    return true;
                }
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_calendar) {
                if(currentActivityName.equals("CalendarViewActivity")){
                    return true;
                }
                Intent intent = new Intent(this, CalendarViewActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_market) {
                // Handle Map navigation (future implementation)
                if(currentActivityName.equals("MarketPlaceActivity")){
                    return true;
                }
                Intent intent = new Intent(this, MarketPlaceActivity.class);
                startActivity(intent);
                return true;
            }
            if (item.getItemId() == R.id.nav_profile) {
                // Send user to settings page
                if(currentActivityName.equals("Settings")){
                    return true;
                }
                Intent intent = new Intent(this, Settings.class);
                startActivity(intent);
                return true;
            }
            return false;
        });
    }
    protected String resetActiveIndicator(){
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView == null) {
            return null;
        }
        String currentActivityName = this.getClass().getSimpleName();

        //set default navigation bar active indicator by current activity
        if (currentActivityName.contains("Main")) {
            bottomNavigationView.setSelectedItemId(R.id.nav_home);
        } else if (currentActivityName.contains("Post")) {
            bottomNavigationView.setSelectedItemId(R.id.nav_post);
        } else if (currentActivityName.contains("Calendar")) {
            bottomNavigationView.setSelectedItemId(R.id.nav_calendar);
        } else if (currentActivityName.contains("Market")) {
            bottomNavigationView.setSelectedItemId(R.id.nav_market);
        } else if (currentActivityName.equals("Settings")) {
            bottomNavigationView.setSelectedItemId(R.id.nav_profile);
        }
        return currentActivityName;
    }
}

