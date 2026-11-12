package com.huzaifa_khan.snapquestfyp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.graphics.Color;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    EditText etEmail, etPass, etRePass;
    Button btnSignup;
    FirebaseAuth mAuth;

    EditText etName;
    ImageView ivProfile;

    Uri imageUri;
    private static final int PICK_IMAGE = 2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        etName = findViewById(R.id.etName);
        ivProfile = findViewById(R.id.ivProfile);

        ivProfile.setOnClickListener(v -> openGallery());

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        etRePass = findViewById(R.id.etRePass);
        btnSignup = findViewById(R.id.btnSignup);

        btnSignup.setOnClickListener(v -> {

            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String pass = etPass.getText().toString().trim();
            String repass = etRePass.getText().toString().trim();

            if(name.isEmpty()){
                Toast.makeText(this,"Enter name",Toast.LENGTH_SHORT).show();
                return;
            }

            if(imageUri == null){
                Toast.makeText(this,"Select profile image",Toast.LENGTH_SHORT).show();
                return;
            }

            if(!pass.equals(repass)){
                Toast.makeText(this,"Passwords do not match",Toast.LENGTH_SHORT).show();
                return;
            }

            mAuth.createUserWithEmailAndPassword(email,pass)
                    .addOnCompleteListener(task -> {
                        if(task.isSuccessful()){

                            uploadProfileToCloudinary(name);

                        } else {
                            Toast.makeText(this,"Signup Failed",Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        TextView tvLoginRedirect = findViewById(R.id.tvLoginRedirect);

        String fullText = "Already have an account? Login";

        SpannableString spannableString = new SpannableString(fullText);

        int startIndex = fullText.indexOf("Login");
        int endIndex = startIndex + "Login".length();


        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View widget) {
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
            }
        };

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

// Change color of Login
        spannableString.setSpan(
                new ForegroundColorSpan(Color.parseColor("#6750A4")),
                startIndex,
                endIndex,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        );

        tvLoginRedirect.setText(spannableString);
        tvLoginRedirect.setMovementMethod(LinkMovementMethod.getInstance());
        tvLoginRedirect.setHighlightColor(Color.TRANSPARENT);
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
            ivProfile.setImageURI(imageUri);
        }
    }

    private void uploadProfileToCloudinary(String name){

        try{
            InputStream inputStream = getContentResolver().openInputStream(imageUri);
            byte[] bytes = inputStream.readAllBytes();

            OkHttpClient client = new OkHttpClient();

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("file","profile.jpg",
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

                        runOnUiThread(() -> saveUserToDatabase(name,imageUrl));

                    }catch(Exception e){}
                }
            });

        }catch(Exception e){}
    }

    private void saveUserToDatabase(String name, String imageUrl){

        String uid = mAuth.getCurrentUser().getUid();

        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("users")
                .child(uid);
        //user container
        HashMap<String,Object> map = new HashMap<>();
        map.put("name",name);
        map.put("profileImage",imageUrl);
        map.put("email",mAuth.getCurrentUser().getEmail());

        ref.setValue(map).addOnCompleteListener(task -> {

            Toast.makeText(this,"Signup Success",Toast.LENGTH_SHORT).show();

            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
}