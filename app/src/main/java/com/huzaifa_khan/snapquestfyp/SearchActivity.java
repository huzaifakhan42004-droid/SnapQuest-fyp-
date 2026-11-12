package com.huzaifa_khan.snapquestfyp;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    EditText etSearch;
    RecyclerView recyclerUsers;

    List<User> list;
    UserAdapter adapter;

    DatabaseReference usersRef;

    TextView tvHint;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_search);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etSearch = findViewById(R.id.etSearch);
        recyclerUsers = findViewById(R.id.recyclerUsers);
        tvHint = findViewById(R.id.tvHint);

        recyclerUsers.setLayoutManager(new LinearLayoutManager(this));

        list = new ArrayList<>();
        adapter = new UserAdapter(this,list);
        recyclerUsers.setAdapter(adapter);

        usersRef = FirebaseDatabase.getInstance().getReference("users");

        //SEARCH LISTENER
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int start,int count,int after){}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count){
                searchUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s){}
        });
    }

    private void searchUsers(String text){

        //if search empty → hide results
        if(text.isEmpty()){
            list.clear();
            adapter.notifyDataSetChanged();
            tvHint.setVisibility(View.VISIBLE);
            return;
        } else {
            tvHint.setVisibility(View.GONE);
        }

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                list.clear();

                for(DataSnapshot data : snapshot.getChildren()){

                    User user = data.getValue(User.class);

                    if(user != null && user.name != null &&
                            user.name.toLowerCase().contains(text.toLowerCase())){

                        user.uid = data.getKey();
                        list.add(user);
                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override public void onCancelled(DatabaseError error){}
        });
    }
}