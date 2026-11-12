package com.huzaifa_khan.snapquestfyp;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CommentActivity extends AppCompatActivity {

    RecyclerView recyclerComments;
    EditText etComment;
    ImageView btnSend;

    List<CommentModel> list;
    CommentAdapter adapter;

    DatabaseReference commentRef;
    String postId, currentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_comment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerComments = findViewById(R.id.recyclerComments);
        etComment = findViewById(R.id.etComment);
        btnSend = findViewById(R.id.btnSend);

        recyclerComments.setLayoutManager(new LinearLayoutManager(this));

        postId = getIntent().getStringExtra("postId");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        commentRef = FirebaseDatabase.getInstance()
                .getReference("posts")
                .child(postId)
                .child("comments");

        list = new ArrayList<>();
        adapter = new CommentAdapter(this,list);
        recyclerComments.setAdapter(adapter);

        loadComments();

        btnSend.setOnClickListener(v -> sendComment());
    }

    private void loadComments(){
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                list.clear();

                for(DataSnapshot data : snapshot.getChildren()){
                    CommentModel c = data.getValue(CommentModel.class);
                    list.add(c);
                }

                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError error){}
        });
    }

    private void sendComment(){

        String text = etComment.getText().toString().trim();

        if(text.isEmpty()) return;

        String id = commentRef.push().getKey();

        HashMap<String,Object> map = new HashMap<>();
        map.put("commentId",id);
        map.put("userId",currentUserId);
        map.put("text",text);
        map.put("timestamp",System.currentTimeMillis());

        commentRef.child(id).setValue(map);

        etComment.setText("");
    }
}