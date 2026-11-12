package com.huzaifa_khan.snapquestfyp;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    EditText etEmail, etPass;
    Button btnLogin;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        mAuth = FirebaseAuth.getInstance();

        etEmail = findViewById(R.id.etEmail);
        etPass = findViewById(R.id.etPass);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(v -> {

            mAuth.signInWithEmailAndPassword(
                    etEmail.getText().toString(),
                    etPass.getText().toString()
            ).addOnCompleteListener(task -> {

                if(task.isSuccessful()){

                    scheduleReminder();

                    startActivity(new Intent(this, TemporaryActivity.class));
                    finish();
                } else {
                    Toast.makeText(this,"Login Failed",Toast.LENGTH_SHORT).show();
                }
            });
        });

        TextView tvLoginRedirect = findViewById(R.id.tvLoginRedirectt);

        String fullText = "Don't you have an account? Sign UP";


        int startIndex = fullText.indexOf("Sign UP");
        int endIndex = startIndex + "Sign UP".length();


        if(startIndex >= 0){
            //txt to obj
            SpannableString spannableString = new SpannableString(fullText);

            // Clickable span
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(View widget) {
                    startActivity(new Intent(MainActivity.this, SignupActivity.class));
                }
            };

            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Color
            spannableString.setSpan(
                    new ForegroundColorSpan(Color.parseColor("#6750A4")),
                    startIndex,
                    endIndex,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            );

            tvLoginRedirect.setText(spannableString);
            tvLoginRedirect.setMovementMethod(LinkMovementMethod.getInstance());
            tvLoginRedirect.setHighlightColor(Color.TRANSPARENT);

        } else {
            // fallback
            tvLoginRedirect.setText(fullText);
        }


    }
    
    private void scheduleReminder(){

        long delay = calculateInitialDelay(14); // 2 PM

        PeriodicWorkRequest request =
                new PeriodicWorkRequest.Builder(ReminderWorker.class,24,TimeUnit.HOURS)
                        .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                        .build();

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                "dailyReminder",
                ExistingPeriodicWorkPolicy.KEEP,
                request
        );
    }

    private long calculateInitialDelay(int hour){

        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();

        target.set(Calendar.HOUR_OF_DAY, hour);
        target.set(Calendar.MINUTE, 0);
        target.set(Calendar.SECOND, 0);

        if(target.before(now)){
            target.add(Calendar.DAY_OF_MONTH,1);
        }

        return target.getTimeInMillis() - now.getTimeInMillis();
    }
}