package com.example.neighborhoodhub;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.icu.text.SimpleDateFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class CalendarViewActivity extends BaseActivity {

    /* All the different views variables */
    CalendarView calendarView;
    ListView eventListView;

    // private DatabaseReference root;
    List<Event> allEvents = new ArrayList<>();
    List<String> selectedDateEvents = new ArrayList<>();
    ArrayAdapter<String> eventAdapter;

    /* Firebase */
    DatabaseReference root, subroot;

    /* Time zone information from the user phone */
    TimeZone timeZone = TimeZone.getDefault();
    String timeZoneID = timeZone.getID(); // e.g., "America/New_York"
    String timeZoneDisplayName = timeZone.getDisplayName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_calendar_view);
        setupTopBar(false);
        setupBottomNavigation();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        /* Find views by ID */
        calendarView = findViewById(R.id.calendarViewId);
        eventListView = findViewById(R.id.eventListViewId);

        /* Firebase connection, set up root and subroot to extract Event in the Events */
        root = FirebaseDatabase.getInstance().getReference();
        subroot = root.child("Events");

        eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, selectedDateEvents);
        eventListView.setAdapter(eventAdapter);

        /* Add the addValueEventListener to update real time of the changes made to the Firebase */
        subroot.addValueEventListener(new EventDataListener(allEvents, selectedDateEvents,this));

        loadAllEventsFromFirebase();

        /* Handle Calendar selected date changes */
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // Format the selected date as "mm-dd-yyyy"
                String selectedDate = String.format("%02d-%02d-%04d", month + 1, dayOfMonth, year);

                // Filter and display events for the selected date
                filterEventsForDate(selectedDate);
            }
        });

        /* Handle clicks on the listView of events */
        eventListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedEventInfo = selectedDateEvents.get(position);

                // Find the matching Event object
                for (Event event : allEvents) {
                    if ((event.getDateTime() + " - " + event.getTitle()).equals(selectedEventInfo)) {
                        // Pass the selected event to the next activity
                        Intent intent = new Intent(CalendarViewActivity.this, EventDetailsActivity.class);
                        intent.putExtra("selectedEvent", event);
                        startActivity(intent);
                        break;
                    }
                }
            }
        });
//        floatAddItemButton = findViewById(R.id.floatingActionButtonId);
//        /* Floating Add Item Button leads to another item to the grid_view */
//        floatAddItemButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Intent intent = new Intent(CalendarViewActivity.this, AddCalendarEventActivity.class);
//                startActivity(intent);
//            }
//        });

        eventListView.setOnItemLongClickListener(new DeleteLongClickListener());
    }

    /* Load all Events from the Events subroot of the Firebase */
    private void loadAllEventsFromFirebase() {
        subroot.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allEvents.clear(); // Clear the list to avoid duplication

                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                    // Extract event data
                    String title = eventSnapshot.getKey();
                    String datetime = eventSnapshot.child("dateTime").getValue(String.class);
                    // String title = eventSnapshot.child("title").getValue(String.class);
                    String locationName = eventSnapshot.child("locationName").getValue(String.class);
                    String locationAddress = eventSnapshot.child("locationAddress").getValue(String.class);
                    String user_id = eventSnapshot.child("userId").getValue(String.class);

                    if (datetime != null && title != null) {
                        // Create and store Event object
                        Event event = new Event(datetime, title, locationName, locationAddress, user_id);
                        allEvents.add(event);
                    }
                }

                // Optionally, display today's events initially
                Calendar today = Calendar.getInstance();
                String todayDate = String.format("%02d-%02d-%04d", today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.YEAR));
                filterEventsForDate(todayDate);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(CalendarViewActivity.this, "Failed to load events.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /* Filter events from all the events on Firebase for a specified date, used in display all events on a selected date on Calendar */
    private void filterEventsForDate(String date) {
        // Create a temporary list for events of the selected date
        List<Event> filteredEvents = new ArrayList<>();

        for (Event event : allEvents) {
            if (event.getDateTime().startsWith(date + " ")) {
                filteredEvents.add(event);
            }
        }

        // Sort the filtered events by time (hh:mm:ss) using a comparator
        Collections.sort(filteredEvents, new Comparator<Event>() {
            @Override
            public int compare(Event e1, Event e2) {
                String time1 = e1.getDateTime().split(" ")[1]; // Extract time part
                String time2 = e2.getDateTime().split(" ")[1]; // Extract time part
                return time1.compareTo(time2); // Compare times lexicographically
            }
        });

        // Clear and update the displayed list
        selectedDateEvents.clear();
        for (Event event : filteredEvents) {
            selectedDateEvents.add(event.getDateTime() + " - " + event.getTitle());
        }

        // Notify the adapter of changes
        eventAdapter.notifyDataSetChanged();

        // Show a message if no events are found for the selected date
        if (selectedDateEvents.isEmpty()) {
            Toast.makeText(this, "No events found for " + date, Toast.LENGTH_SHORT).show();
        }
    }

    /* Get the date that's currently selected on the calendarView */
    private String getSelectedDate() {
        // Implement logic to get the currently selected date from your CalendarView
        // For example:
        Calendar today = Calendar.getInstance();
        return String.format("%02d-%02d-%04d", today.get(Calendar.MONTH) + 1, today.get(Calendar.DAY_OF_MONTH), today.get(Calendar.YEAR));
    }

    /* Implement the long-click delete functionality for the listView that display events */
    public class DeleteLongClickListener implements AdapterView.OnItemLongClickListener {

        @Override
        public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
            // Get the event's info from the selected item
            String selectedEventInfo = selectedDateEvents.get(position);

            // Find the matching Event object
            Event eventToDelete = null;
            for (Event event : allEvents) {
                if ((event.getDateTime() + " - " + event.getTitle()).equals(selectedEventInfo)) {
                    eventToDelete = event;
                    break;
                }
            }

            // If event found, prompt for confirmation
            if (eventToDelete != null) {
                showDeleteConfirmationDialog(eventToDelete);
            }

            return true;
        }

        private void showDeleteConfirmationDialog(Event eventToDelete) {
            new AlertDialog.Builder(CalendarViewActivity.this)
                    .setTitle("Delete Event")
                    .setMessage("Are you sure you want to delete this event?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            deleteEventFromFirebase(eventToDelete);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
        }

        private void deleteEventFromFirebase(Event eventToDelete) {
            // Delete the event from Firebase
            subroot.child(eventToDelete.getTitle()).removeValue()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                // Remove from the local list as well
                                allEvents.remove(eventToDelete);
                                selectedDateEvents.remove(eventToDelete.getDateTime() + " - " + eventToDelete.getTitle());
                                eventAdapter.notifyDataSetChanged();  // Notify the adapter of the data change
                                Toast.makeText(CalendarViewActivity.this, "Event deleted successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(CalendarViewActivity.this, "Failed to delete event", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }


    /* implementation of all events update listener */
    class EventDataListener implements ValueEventListener {

        private List<Event> allEvents;
        private List<String> selectedDateEvents;
        private Context context;

        public EventDataListener(List<Event> allEvents, List<String> selectedDateEvents, Context context) {
            this.allEvents = allEvents;
            this.selectedDateEvents = selectedDateEvents;
            this.context = context;
        }

        @Override
        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            // Update the events without causing duplicates
            List<Event> newEvents = new ArrayList<>();
            List<String> newSelectedDateEvents = new ArrayList<>();

            for (DataSnapshot eventSnapshot : dataSnapshot.getChildren()) {
                String title = eventSnapshot.getKey();
                String datetime = eventSnapshot.child("dateTime").getValue(String.class);
                String locationName = eventSnapshot.child("locationName").getValue(String.class);
                String locationAddress = eventSnapshot.child("locationAddress").getValue(String.class);
                String user_id = eventSnapshot.child("userId").getValue(String.class);

                if (datetime != null && title != null) {
                    Event event = new Event(datetime, title, locationName, locationAddress, user_id);
                    newEvents.add(event);

                    // Check if the event belongs to the currently selected date
                    String selectedDate = ((CalendarViewActivity) context).getSelectedDate();
                    if (event.getDateTime().startsWith(selectedDate)) {
                        newSelectedDateEvents.add(event.getDateTime() + " - " + event.getTitle());
                    }
                }
            }

            // Clear the old events and update the lists
            allEvents.clear();
            allEvents.addAll(newEvents);

            // Update the selected date events
            selectedDateEvents.clear();
            selectedDateEvents.addAll(newSelectedDateEvents);

            // Notify the adapter that the data has changed
            ((CalendarViewActivity) context).eventAdapter.notifyDataSetChanged();
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("FirebaseError", "Failed to load events: " + error.getMessage());
        }
    }
}