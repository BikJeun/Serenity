package com.example.serenity;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.preference.PreferenceFragmentCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

import static android.app.Activity.RESULT_OK;

public class SettingsActivity extends Fragment {

    //Firebase
    DatabaseReference reference;
    FirebaseUser user;
    String uid;
    StorageReference storageReference;

    //views from xml
    ImageView profilePic;
    TextView editPic, nameLabel, emailLabel, edit, reset, delete, logout;

    private static Context context = null;

    public static Intent makeIntent(Context context) {
        return new Intent(context, SettingsActivity.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.settings_activity, container, false);

        user = FirebaseAuth.getInstance().getCurrentUser();
        uid = user.getUid();
        reference = FirebaseDatabase.getInstance().getReference(uid).child("Users");
        storageReference = FirebaseStorage.getInstance().getReference();

        initializeUI(v);

        return v;
    }

    private void initializeUI(View v) {
        editPic = v.findViewById(R.id.edit_pic);
        profilePic = v.findViewById(R.id.profile_pic);
        nameLabel = v.findViewById(R.id.name);
        emailLabel = v.findViewById(R.id.email);
        edit  = v.findViewById(R.id.editAccount);
        reset = v.findViewById(R.id.Reset_Password);
        delete = v.findViewById(R.id.DeleteAccount);
        logout = v.findViewById(R.id.signOut);

        showProfile();

        editPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeDP(v);
            }
        });

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeAccount(v);
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resetPassword(v);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteAcct(v);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getContext(), MainActivity.class));
            }
        });

    }

    private void changeAccount(View v) {
        SettingsEditAccountDialog dialog = new SettingsEditAccountDialog(getContext());
        SettingsEditAccountDialog.Builder mBuilder = new SettingsEditAccountDialog.Builder(getContext());
        dialog.show();

        Toast.makeText(getContext(), "account", Toast.LENGTH_SHORT).show();
    }

    private void deleteAcct(View v) {

        SettingsDialog dialog = new SettingsDialog(getContext());
        SettingsDialog.Builder mBuilder = new SettingsDialog.Builder(getContext());
        dialog.show();

        Button yes = dialog.getView().findViewById(R.id.deleteyes);
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase.getInstance().getReference(uid).removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        FirebaseAuth.getInstance().getCurrentUser().delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                startActivity(new Intent(getContext(), MainActivity.class));
                                Toast.makeText(getContext(), "Account Deleted", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(getContext(), "delete FAILED", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    private void resetPassword(View v) {
        //Toast.makeText(getContext(), "reset", Toast.LENGTH_SHORT).show();
        startActivityForResult(new Intent(v.getContext(), ResetPassword.class),100);
        //startActivity(new Intent(v.getContext(), ResetPassword.class));



    }

    private void changeDP(View v) {
        //Toast.makeText(getContext(), "DP", Toast.LENGTH_SHORT).show();

        //Open Gallery
        Intent openGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(openGalleryIntent, 101);
    }

    private void showProfile() {
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String userName = dataSnapshot.child("username").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);

                nameLabel.setText(userName);
                emailLabel.setText(email);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        StorageReference profileRef = storageReference.child("users/" + uid + "/Profile Pic");
        profileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.get().load(uri).into(profilePic);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 100) {
            if(resultCode == RESULT_OK) {
            }
        } else if(requestCode == 101) {
            if(resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                //profilePic.setImageURI(imageUri);

                uploadImageToFirebase(imageUri);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadImageToFirebase(Uri uri) {
        final StorageReference fileRef = storageReference.child("users/" + uid + "/Profile Pic");
        fileRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.d("Profile Pic Upload", "onSuccess: ");
                fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        Picasso.get().load(uri).into(profilePic);
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("Profile Pic Upload", "onFailure: " + e.getMessage());
            }
        });
    }
}

