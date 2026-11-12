package com.huzaifa_khan.snapquestfyp;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyProfileActivity extends AppCompatActivity {

    ImageView ivProfile;
    TextView tvName, tvEmail;
    RecyclerView recyclerProfile;

    List<Post> list;
    GalleryAdapter adapter;

    DatabaseReference userRef, postsRef;
    String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_my_profile);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        ivProfile = findViewById(R.id.ivProfile);
        tvName = findViewById(R.id.tvName);
        tvEmail = findViewById(R.id.tvEmail);
        recyclerProfile = findViewById(R.id.recyclerProfile);

        recyclerProfile.setLayoutManager(new GridLayoutManager(this,3));

        list = new ArrayList<>();
        adapter = new GalleryAdapter(this,list);
        recyclerProfile.setAdapter(adapter);

        uid = getIntent().getStringExtra("userId");

        //khud ka profile open ho (fallback)
        if(uid == null){
            uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        userRef = FirebaseDatabase.getInstance().getReference("users").child(uid);
        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        loadUserData();
        loadUserPosts();
    }

    private void loadUserData(){

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                if(snapshot.exists()){
                    String name = snapshot.child("name").getValue(String.class);
                    String image = snapshot.child("profileImage").getValue(String.class);
                    String email = snapshot.child("email").getValue(String.class);

                    tvName.setText(name != null ? name : "No Name");
                    tvEmail.setText(email != null ? email : "");

                    if(image != null){
                        Glide.with(MyProfileActivity.this)
                                .load(image)
                                .into(ivProfile);
                    }
                }
            }

            @Override public void onCancelled(DatabaseError error){}
        });
    }

    private void loadUserPosts(){

        postsRef.orderByChild("userId")
                .equalTo(uid)
                .addValueEventListener(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        list.clear();

                        for(DataSnapshot data : snapshot.getChildren()){
                            Post post = data.getValue(Post.class);
                            list.add(post);
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onCancelled(DatabaseError error){}
                });
    }
}