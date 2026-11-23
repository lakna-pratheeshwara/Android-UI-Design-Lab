package com.example.uidesignlab;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import android.database.Cursor;
import android.util.Log;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UsersActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    Button btnAddJohn, btnClearAll, btnReadProvider;

    UserAdapter adapter;
    ArrayList<UserRecord> userList;

    AppDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        recyclerView = findViewById(R.id.recyclerViewUsers);
        btnAddJohn = findViewById(R.id.btnAddJohn);
        btnClearAll = findViewById(R.id.btnClearAll);
        btnReadProvider = findViewById(R.id.btnReadProvider);

        db = AppDatabase.getInstance(getApplicationContext());

        userList = new ArrayList<>();

        adapter = new UserAdapter(userList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadUsers();

        btnAddJohn.setOnClickListener(v -> {
            AppDatabase.databaseExecutor.execute(() -> {
                db.userDao().insert(new UserRecord("John"));

                runOnUiThread(() -> {
                    Toast.makeText(this, "John Added", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            });
        });


        btnReadProvider.setOnClickListener(v ->
                AppDatabase.databaseExecutor.execute(() -> {

                    int count = 0;

                    try (Cursor cursor = getContentResolver().query(
                            UserProvider.CONTENT_URI,
                            null, null, null, null)) {

                        if (cursor != null) {

                            int nameIndex =
                                    cursor.getColumnIndexOrThrow("name");

                            while (cursor.moveToNext()) {

                                String name = cursor.getString(nameIndex);

                                Log.d("RoomLab", "Provider User: " + name);

                                count++;
                            }
                        }
                    }

                    final int finalCount = count;

                    runOnUiThread(() ->
                            Toast.makeText(
                                    UsersActivity.this,
                                    "Provider returned " + finalCount + " rows",
                                    Toast.LENGTH_SHORT
                            ).show()
                    );
                }));


        btnClearAll.setOnClickListener(v -> {
            AppDatabase.databaseExecutor.execute(() -> {
                db.userDao().deleteAll();

                runOnUiThread(() -> {
                    Toast.makeText(this, "All Users Cleared", Toast.LENGTH_SHORT).show();
                    loadUsers();
                });
            });
        });
    }

    private void loadUsers() {
        AppDatabase.databaseExecutor.execute(() -> {
            List<UserRecord> users = db.userDao().getAll();

            runOnUiThread(() -> {
                adapter.updateUsers(users);
            });
        });
    }
}