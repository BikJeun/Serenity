package com.example.serenity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;

import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity  { //this the login screen
    Button login,createAccount;
    EditText usernameInput,passInput;

    TextView tx1;
    int counter = 3; //im limiting to 3 attempts to login

    @Override
    protected void onCreate(Bundle savedInstanceState) { //this is a method which starts when tat screen appears
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        login = (Button)findViewById(R.id.login);
        usernameInput = (EditText)findViewById(R.id.editText);
        passInput = (EditText)findViewById(R.id.editText2);

        tx1 = (TextView)findViewById(R.id.attempts); //"Attempts Left"
        tx1.setVisibility(View.VISIBLE);

        createAccount = (Button)findViewById(R.id.createAccount);

        //INCOMPLETE TO BE LINKED TO MAIN MENU
        // NEED TO ADD SIGN IN FUNCTIONS FROM FIREBASE
        login.setOnClickListener(new View.OnClickListener() { //when clicking the login button
            @Override
            public void onClick(View v) {
                if(usernameInput.getText().toString().equals("admin") &&
                        passInput.getText().toString().equals("admin")) {
                    Toast.makeText(getApplicationContext(),
                            "Redirecting...",Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(getApplicationContext(),
                               "Wrong Input",Toast.LENGTH_SHORT).show();

                            tx1.setVisibility(View.VISIBLE);
                    tx1.setBackgroundColor(Color.RED);
                    counter--;
                    tx1.setText(Integer.toString(counter));

                    if (counter == 0) {
                        login.setEnabled(false);
                    }
                }
            }
        });

        createAccount.setOnClickListener(new View.OnClickListener() { //GOES TO CREATE ACCOUNT CLASS
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, CreateAccount.class));
            }
        });
    }
}
