package com.example.serenity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * note to grace: this class is complete and can be documented and fully designed
 * firebase function have been added
 */

public class CreateAccount extends Activity { //THIS THE CREATE ACCOUNT PAGE
    Button create;
    EditText password1, password2, email;

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
        auth = FirebaseAuth.getInstance();

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              String emailIn = email.getText().toString();
              String pass1 = password1.getText().toString();
              String pass2 = password2.getText().toString();

              if(emailIn.isEmpty()) {
                  email.setError("Please enter email");
                  email.requestFocus();
              } else if(pass1.isEmpty()) {
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
                              Toast.makeText(getApplicationContext(), "SignUp Unsuccessful, Please Try Again :(", Toast.LENGTH_LONG).show();
                          } else {
                              Toast.makeText(getApplicationContext(), "SignUp Successful :)", Toast.LENGTH_LONG).show();
                              FirebaseUser user = auth.getCurrentUser();

                              user.sendEmailVerification()
                                      .addOnCompleteListener(new OnCompleteListener<Void>() { //THIS IS THE OPTIONAL PART....DO YOU WANT IT?
                                          @Override
                                          public void onComplete(@NonNull Task<Void> task) {
                                              if (task.isSuccessful()) {
                                                  Toast.makeText(CreateAccount.this, "Verify your Email", Toast.LENGTH_LONG).show();
                                              }
                                          }
                                      });
                              startActivity(new Intent(CreateAccount.this, MainActivity.class));
                          }
                      }
                  });
              }
            }
        });


    }
}
