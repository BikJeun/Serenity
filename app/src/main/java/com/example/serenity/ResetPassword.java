package com.example.serenity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class ResetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_reset_password);

        final EditText email, password, password2;
        email = findViewById(R.id.emailConfirm);

        Button confirm, cancel;
        confirm = findViewById(R.id.Resconfirm);
        cancel = findViewById(R.id.Rescancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();

                if(userEmail.isEmpty() || !userEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    email.setError("Please enter your registered email address");
                    email.requestFocus();
                } else {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.sendPasswordResetEmail(userEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Password reset email sent!", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            } else {
                                Toast.makeText(getApplicationContext(), task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                    });
                }
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setResult(RESULT_OK, new Intent());
                finish();
                overridePendingTransition(R.anim.stay, R.anim.slide_out_down);
                //getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, new SettingsActivity()).commit();
                //startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
            }
        });

    }
}
