package com.huzaifa_khan.snapquestfyp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashboardActivity extends AppCompatActivity {

    ImageView imageView;
    TextView tvTimer;
    Button btnUpload;

    FirebaseAuth mAuth;
    DatabaseReference ref;

    Uri imageUri;
    private static final int PICK_IMAGE = 1;

    EditText etCaption;
    DatabaseReference postsRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_dashboard);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



        etCaption = findViewById(R.id.etCaption);

        postsRef = FirebaseDatabase.getInstance().getReference("posts");

        mAuth = FirebaseAuth.getInstance();

        ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(mAuth.getCurrentUser().getUid());

        imageView = findViewById(R.id.imageViewDashboard);
        tvTimer = findViewById(R.id.tvTimer);
        btnUpload = findViewById(R.id.btnUpload);

        btnUpload.setOnClickListener(v -> openGallery());

        loadImage();
    }



    private void openGallery(){
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode,resultCode,data);

        if(requestCode==PICK_IMAGE && resultCode==RESULT_OK && data!=null){
            imageUri = data.getData();
            uploadToCloudinary(imageUri);
        }
    }

    private void uploadToCloudinary(Uri uri){
        try{
            InputStream inputStream = getContentResolver().openInputStream(uri);
            byte[] bytes = inputStream.readAllBytes();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file","image.jpg",
                            RequestBody.create(bytes, MediaType.parse("image/*")))
                    .addFormDataPart("upload_preset","sanpquest123")
                    .build();

            Request request = new Request.Builder()
                    .url("https://api.cloudinary.com/v1_1/djybxbkdm/image/upload")
                    .post(requestBody)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e){}

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String res = response.body().string();
                    try{
                        JSONObject json = new JSONObject(res);
                        String imageUrl = json.getString("secure_url");

                        runOnUiThread(() -> saveToFirebase(imageUrl));

                    }catch(Exception e){}
                }
            });

        }catch(Exception e){}
    }

    private void saveToFirebase(String imageUrl){

        String userId = mAuth.getCurrentUser().getUid();
        String caption = etCaption.getText().toString().trim();

        long currentTime = System.currentTimeMillis();

        String todayDate = new java.text.SimpleDateFormat("yyyy-MM-dd")
                .format(new java.util.Date());


        ref.child("lastUploadDate").get().addOnSuccessListener(snapshot -> {

            String lastDate = snapshot.getValue(String.class);

            if(todayDate.equals(lastDate)){
                runOnUiThread(() ->
                        tvTimer.setText("Already uploaded today ❌")
                );
                return;
            }


            String postId = postsRef.push().getKey();

            long expiryTime = currentTime + (24*60*60*1000);

            HashMap<String,Object> postMap = new HashMap<>();
            postMap.put("postId", postId);
            postMap.put("userId", userId);
            postMap.put("imageUrl", imageUrl);
            postMap.put("caption", caption.isEmpty() ? "" : caption);
            postMap.put("timestamp", currentTime);
            postMap.put("date", todayDate);
            postMap.put("likesCount", 0);


            postsRef.child(postId).setValue(postMap);


            HashMap<String,Object> userMap = new HashMap<>();
            userMap.put("imageUrl", imageUrl);
            userMap.put("expiryTime", expiryTime);
            userMap.put("lastUploadDate", todayDate);

            ref.updateChildren(userMap);


            runOnUiThread(() -> {
                Glide.with(this).load(imageUrl).into(imageView);
                startCountdown(expiryTime);
                etCaption.setText("");
            });

        });
    }

    private void loadImage(){
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if(snapshot.exists()){

                    String url = snapshot.child("imageUrl").getValue(String.class);
                    Long expiry = snapshot.child("expiryTime").getValue(Long.class);

                    if(url != null){
                        Glide.with(DashboardActivity.this)
                                .load(url)
                                .into(imageView);
                    } else {
                        imageView.setImageDrawable(null);
                    }

                    if(expiry != null){
                        startCountdown(expiry);
                    } else {
                        tvTimer.setText("No active post");
                    }

                } else {
                    tvTimer.setText("Upload Today Post");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error){}
        });
    }

    private void startCountdown(long expiryTime){

        long timeLeft = expiryTime - System.currentTimeMillis();

        new CountDownTimer(timeLeft,1000){
            public void onTick(long millisUntilFinished){
                long hours = millisUntilFinished/(1000*60*60);
                long minutes = (millisUntilFinished/(1000*60))%60;
                long seconds = (millisUntilFinished/1000)%60;
                tvTimer.setText(hours+":"+minutes+":"+seconds);
            }

            public void onFinish(){
                ref.removeValue();
                imageView.setImageDrawable(null);
                tvTimer.setText("Expired");
            }
        }.start();
    }
}