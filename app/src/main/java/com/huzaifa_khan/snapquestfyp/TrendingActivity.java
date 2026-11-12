package com.huzaifa_khan.snapquestfyp;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

public class TrendingActivity extends AppCompatActivity {

    RecyclerView recyclerTrending;
    List<Post> list;
    FeedAdapter adapter;

    DatabaseReference postsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_trending);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recyclerTrending = findViewById(R.id.recyclerTrending);
        recyclerTrending.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new FeedAdapter(this, list);
        recyclerTrending.setAdapter(adapter);

        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        loadTrendingPosts();

    }

    private void loadTrendingPosts(){

        long startOfDay = getStartOfDay();

        postsRef.orderByChild("likesCount")
                .addListenerForSingleValueEvent(new ValueEventListener() {

                    @Override
                    public void onDataChange(DataSnapshot snapshot) {

                        list.clear();

                        for(DataSnapshot data : snapshot.getChildren()){

                            Post post = data.getValue(Post.class);

                            if(post != null && post.timestamp >= startOfDay){
                                list.add(post);
                            }
                        }

                        //SORT (highest likes first)
                        Collections.sort(list, (a, b) -> Math.toIntExact(b.likesCount - a.likesCount));

                        //TOP 10 ONLY
                        if(list.size() > 10){
                            list = new ArrayList<>(list.subList(0,10));
                        }

                        adapter.notifyDataSetChanged();
                    }

                    @Override public void onCancelled(DatabaseError error){}
                });
    }

    private long getStartOfDay(){

        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY,0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

        return cal.getTimeInMillis();
    }
}