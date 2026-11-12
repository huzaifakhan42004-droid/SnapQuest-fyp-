package com.huzaifa_khan.snapquestfyp;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class FeedAdapter extends RecyclerView.Adapter<FeedAdapter.ViewHolder>{

    Context context;
    List<Post> list;
    DatabaseReference postsRef;
    String currentUserId;

    public FeedAdapter(Context context,List<Post> list){
        this.context = context;
        this.list = list;
        postsRef = FirebaseDatabase.getInstance().getReference("posts");
        currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_post,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder,int position){

        Post post = list.get(position);

        holder.tvCaption.setText(post.caption);
        holder.tvLikes.setText(post.likesCount + " likes");

        Glide.with(context).load(post.imageUrl).into(holder.postImage);

        DatabaseReference userRef = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(post.userId);

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                String name = snapshot.child("name").getValue(String.class);
                holder.tvUsername.setText(name);
            }

            @Override public void onCancelled(DatabaseError error){}
        });

        if(post.timestamp != 0){
            holder.tvDate.setText(formatDate(post.timestamp));
        }

        // LIKE BUTTON
        holder.btnLike.setOnClickListener(v -> {

            DatabaseReference likeRef = postsRef.child(post.postId)
                    .child("likes")
                    .child(currentUserId);

            likeRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {

                    if(snapshot.exists()){
                        // UNLIKE
                        likeRef.removeValue();
                        postsRef.child(post.postId)
                                .child("likesCount")
                                .setValue(post.likesCount - 1);
                    }else{
                        // LIKE
                        likeRef.setValue(true);
                        postsRef.child(post.postId)
                                .child("likesCount")
                                .setValue(post.likesCount + 1);
                    }
                }

                @Override public void onCancelled(DatabaseError error){}
            });
        });

        // COMMENT BUTTON
        holder.btnComment.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentActivity.class);
            intent.putExtra("postId", post.postId);
            context.startActivity(intent);
        });
    }

    private String formatDate(long timestamp){
        java.text.SimpleDateFormat sdf =
                new java.text.SimpleDateFormat("dd MMM yyyy hh:mm a");

        return sdf.format(new java.util.Date(timestamp));
    }

    @Override
    public int getItemCount(){
        return list.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView postImage;
        TextView tvCaption,tvLikes,btnLike,btnComment, tvUsername, tvDate;

        public ViewHolder(View itemView){
            super(itemView);

            postImage = itemView.findViewById(R.id.postImage);
            tvUsername = itemView.findViewById(R.id.tvUsername);
            tvCaption = itemView.findViewById(R.id.tvCaption);
            tvLikes = itemView.findViewById(R.id.tvLikes);
            btnLike = itemView.findViewById(R.id.btnLike);
            tvDate = itemView.findViewById(R.id.tvDate);
            btnComment = itemView.findViewById(R.id.btnComment);
        }
    }

    // COMMENT DIALOG
    private void showCommentDialog(String postId){

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Add Comment");

        EditText input = new EditText(context);
        builder.setView(input);

        builder.setPositiveButton("Post",(dialog,which)->{

            String text = input.getText().toString();

            DatabaseReference commentRef = postsRef.child(postId)
                    .child("comments")
                    .push();

            HashMap<String,Object> map = new HashMap<>();
            map.put("userId",currentUserId);
            map.put("text",text);
            map.put("timestamp",System.currentTimeMillis());

            commentRef.setValue(map);
        });

        builder.setNegativeButton("Cancel",null);
        builder.show();
    }

}
