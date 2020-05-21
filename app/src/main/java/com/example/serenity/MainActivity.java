package com.example.serenity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;


public class MainActivity extends Activity {
    Button login, createAccount;
    EditText usernameInput, passInput;
    FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button) findViewById(R.id.login);
        usernameInput = (EditText) findViewById(R.id.emailText);
        passInput = (EditText) findViewById(R.id.pwdText);


        createAccount = (Button) findViewById(R.id.createAccount);
        auth = FirebaseAuth.getInstance();
        final FirebaseUser user = auth.getCurrentUser();


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.getCurrentUser().reload();
                String email = usernameInput.getText().toString();
                String pwd = passInput.getText().toString();

                if (email.isEmpty()) {
                    usernameInput.setError("Please enter email");
                    usernameInput.requestFocus();
                    return;
                } else if (!isValid(email)) {
                    usernameInput.setError("Please enter valid email");
                    usernameInput.requestFocus();
                    return;
                }

                assert user != null;
                if (!user.isEmailVerified()) {
                    Toast.makeText(MainActivity.this, "Verify Email", Toast.LENGTH_SHORT).show();
                    return;
                }


                if (pwd.isEmpty()) {
                    passInput.setError("Please enter password");
                    passInput.requestFocus();
                    return;
                }

                loginUser(email, pwd);
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() { //GOES TO CREATE ACCOUNT CLASS
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAccount.class));
            }
        });
    }

    private boolean isValid(String email) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }

    private void loginUser(String email, String pwd) {
        auth = FirebaseAuth.getInstance();
        auth.signInWithEmailAndPassword(email, pwd).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(MainActivity.this, Calender.class));
                } else {
                    Toast.makeText(MainActivity.this, "Error! " + Objects.requireNonNull(task.getException()).getMessage(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }


}
