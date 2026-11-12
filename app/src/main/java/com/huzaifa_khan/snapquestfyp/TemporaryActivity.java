package com.huzaifa_khan.snapquestfyp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

public class TemporaryActivity extends AppCompatActivity {

    LinearLayout dashboard, newFeed, trendingPost, searchUser, myProfile, btnLogout;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_temporary);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        dashboard = findViewById(R.id.btnUploadPhoto);
        newFeed = findViewById(R.id.btnFeed);
        trendingPost = findViewById(R.id.btnTrending);
        myProfile = findViewById(R.id.btnProfile);
        searchUser = findViewById(R.id.btnSearchUser);
        btnLogout = findViewById(R.id.btnLogout);

        dashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemporaryActivity.this, DashboardActivity.class));
            }
        });

        newFeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemporaryActivity.this, FeedActivity.class));
            }
        });

        trendingPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemporaryActivity.this, TrendingActivity.class));
            }
        });

        myProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemporaryActivity.this, MyProfileActivity.class));
            }
        });

        searchUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(TemporaryActivity.this, SearchActivity.class));
            }
        });

        btnLogout.setOnClickListener(v -> {

            new androidx.appcompat.app.AlertDialog.Builder(
                    TemporaryActivity.this,
                    R.style.CustomAlertDialog)

                    .setIcon(android.R.drawable.ic_lock_power_off)

                    .setTitle("Logout Confirmation")
                    .setMessage("Are you sure you want to logout from your account?")

                    .setPositiveButton("Logout", (dialog, which) -> {
                        mAuth.signOut();
                        Intent intent = new Intent(TemporaryActivity.this, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })

                    .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                //don't close
                    .setCancelable(false)
                    .show();
        });
    }
}