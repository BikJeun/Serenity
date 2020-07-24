package com.example.serenity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;


public class SettingsEditAccountDialog {
    private Context context;
    private SettingsDialog.OnSettingsDialogListener mListener;
    private android.app.AlertDialog alert;

    //Firebase
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    String uid = user.getUid();
    DatabaseReference reference = FirebaseDatabase.getInstance().getReference(uid).child("Users");

    EditText password, email,username;

    public SettingsEditAccountDialog(Context context) {
        this.context = context;
        buildPlan();
    }

    private void buildPlan() {
        final View dialogView = View.inflate(context, R.layout.settings_edit_account, null);

        password = dialogView.findViewById(R.id.checkingPassword);
        email = dialogView.findViewById(R.id.newEmail);
        username = dialogView.findViewById(R.id.newUserName);

        email.setVisibility(View.INVISIBLE);
        username.setVisibility(View.GONE);
        password.setVisibility(View.VISIBLE);

        final Button cancel = dialogView.findViewById(R.id.AccCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alert.dismiss();
            }
        });

        final Button confirm = dialogView.findViewById(R.id.AccConfirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkPasswordAuth(password, cancel, confirm,dialogView);
            }
        });

        // Initialize and build the AlertBuilderDialog
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(context)
                .setView(dialogView);
        alert = builder.create();
        if (alert.getWindow() != null)
            alert.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }
    public void show() {
        alert.show();
    }


    void setOnSettingsEditAccountDialog(SettingsDialog.OnSettingsDialogListener listener) {
        mListener = listener;
    }

    interface setOnSettingsEditAccountDialog {
    }
    static class Builder {

        private final Context mContext;
        private SettingsEditAccountDialog mListener;

        static SettingsEditAccountDialog.Builder instance(Context context) {
            return new SettingsEditAccountDialog.Builder(context);
        }

        Builder(Context context) {
            mContext = context;
        }

        SettingsEditAccountDialog.Builder setOnColorSelectedListener(SettingsDialog.OnSettingsDialogListener listener) {
            mListener = (SettingsEditAccountDialog) listener;
            return this;
        }

        SettingsEditAccountDialog create() {
            SettingsEditAccountDialog dialog = new SettingsEditAccountDialog(mContext);
            dialog.setOnSettingsEditAccountDialog((SettingsDialog.OnSettingsDialogListener) mListener);
            return dialog;
        }

        /*public Builder setSelectedColor(int color) {
            mColor = color;
            return this;
        }*/
    }

    private void checkPasswordAuth(EditText pwd, Button cancel, Button confirm, final View dialogView) {
        final String[] updatedEmail = new String[1];
        String pass = pwd.getText().toString();

        if(pass.isEmpty()) {
            pwd.setError("Password Required");
            pwd.requestFocus();
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), pass);
        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()) {
                    password.setVisibility(View.GONE);
                    username.setVisibility(View.VISIBLE);
                    username.setHint(R.string.new_username);
                    email.setVisibility(View.VISIBLE);
                    email.setHint(R.string.new_email);
                } else if(task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                    password.setError("Invalid Password");
                    password.requestFocus();
                } else {
                    Toast.makeText(context,"" + task.getException().getMessage(),Toast.LENGTH_LONG).show();
                }
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updatedEmail[0] = getNewEmail(dialogView);

                user.verifyBeforeUpdateEmail(updatedEmail[0]);
                reference.child("username").removeValue();
                reference.child("username").setValue(getUsername(dialogView));
                context.startActivity(new Intent(context, MainActivity.class));
                Toast.makeText(context, "Please Verify Updated Email" , Toast.LENGTH_LONG).show();
                Log.d("email", "onClick: " + user.getEmail());

            }
        });
    }

    private String getUsername(View dialogView) {
        EditText username = dialogView.findViewById(R.id.newUserName);
        String name = username.getText().toString();

        if(name.isEmpty()) {
            username.setError("Desired Username Required");
            username.requestFocus();
        }
        return name;
    }

    private String getNewEmail(View view) {
        EditText newEmail = view.findViewById(R.id.newEmail);
        String email = newEmail.getText().toString();

        if(email.isEmpty()) {
            newEmail.setError("Email Required");
            newEmail.requestFocus();
        }

        if(!isValid(email)) {
            newEmail.setError("Please enter valid email");
            newEmail.requestFocus();
        }
        return email;
    }

    private boolean isValid(String email) {
        String regex = "^[\\w-_.+]*[\\w-_.]@([\\w]+\\.)+[\\w]+[\\w]$";
        return email.matches(regex);
    }
}
