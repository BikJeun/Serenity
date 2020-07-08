package com.example.serenity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.serenity.data.UserProfile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class CreateAccount extends Activity {
    Button create;
    EditText password1, password2, email, username;

    FirebaseAuth auth;

    public boolean checkPassword() { //ENSURING INPUT PASSWORD SAME
        return password1.getText().toString().equals(password2.getText().toString());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        create = (Button)findViewById(R.id.Create);
        password1 = (EditText)findViewById(R.id.createPassword);
        password2 = (EditText)findViewById(R.id.confirmPassword);
        email = (EditText)findViewById(R.id.email);
        username = findViewById(R.id.User);
        auth = FirebaseAuth.getInstance();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameIN = username.getText().toString();
                String emailIn = email.getText().toString();
                String pass1 = password1.getText().toString();
                String pass2 = password2.getText().toString();

                if(usernameIN.isEmpty()) {
                    username.setError("Please enter username");
                    username.requestFocus();
                }

                if (emailIn.isEmpty()) {
                    email.setError("Please enter email");
                    email.requestFocus();
                } else if (!isValid(emailIn)) {
                    email.setError("Please enter valid email");
                    email.requestFocus();
                }

                if(pass1.isEmpty()) {
                    password1.setError("Please enter password");
                    password1.requestFocus();
                } else if(pass2.isEmpty()) {
                    password2.setError("Please enter password");
                    password2.requestFocus();
                } else if(!checkPassword()) {
                    Toast.makeText(getApplicationContext(), "Input password are different", Toast.LENGTH_SHORT).show();
                } else {
                    auth.createUserWithEmailAndPassword(emailIn, pass2).addOnCompleteListener(CreateAccount.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(!task.isSuccessful()) {
                                Toast.makeText(getApplicationContext(), "Account already exist", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(getApplicationContext(), "SignUp Successful :)", Toast.LENGTH_LONG).show();
                                FirebaseUser user = auth.getCurrentUser();

                                saveProfile(user.getUid());

                                user.sendEmailVerification().addOnSuccessListener((new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(CreateAccount.this, "Verification Email Sent", Toast.LENGTH_SHORT).show();
                                    }
                                }));
                                startActivity(new Intent(CreateAccount.this, MainActivity.class));
                            }
                        }
                    });
                }
            }

            private void saveProfile(String uid) {
                UserProfile newProfile = new UserProfile(uid, username.getText().toString(), email.getText().toString(), password2.getText().toString());
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference(uid);
                ref.child("Users").setValue(newProfile.toFirebaseObject());
            }
        });

    }

    private boolean isValid(String email) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }


}
