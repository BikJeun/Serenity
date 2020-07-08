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

public class ResetPassword extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_reset_password);

        final EditText email, password, password2;
        email = findViewById(R.id.emailConfirm);
        password = findViewById(R.id.newPassword);
        password2 = findViewById(R.id.newPassword2);

        Button confirm, cancel;
        confirm = findViewById(R.id.confirm);
        cancel = findViewById(R.id.cancel);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String userEmail = email.getText().toString();
                String pwd = password.getText().toString();
                String pwd2 = password2.getText().toString();

                if(userEmail.isEmpty() || !userEmail.equals(FirebaseAuth.getInstance().getCurrentUser().getEmail())) {
                    email.setError("Please enter your registered email address");
                    email.requestFocus();
                }else if(pwd.isEmpty()) {
                    password.setError("Please enter new password");
                    password.requestFocus();
                } else if(pwd2.isEmpty()) {
                    password2.setError("Please enter new password");
                    password2.requestFocus();
                } else if(!pwd.equals(pwd2)) {
                    password2.setError("Password not the same");
                    password.requestFocus();
                    password2.requestFocus();
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
                startActivity(new Intent(getApplicationContext(),SettingsActivity.class));
            }
        });

    }
}
