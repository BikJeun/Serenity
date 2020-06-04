package com.example.serenity;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

public class TodoListBottomSheet extends BottomSheetDialogFragment {
    DatabaseReference title;

    public void setTitle(DatabaseReference title) {
        this.title = title;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.todolist_additems_bottomsheet, container, false);

        final EditText input = (EditText) view.findViewById(R.id.setTask);
        final EditText message = (EditText) view.findViewById(R.id.setMessage);
        Button addButton = view.findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseDatabase database = FirebaseDatabase.getInstance();
                String key = title.push().getKey();
                Log.d("creating data", "onClick: " + key);

                TodoListModel todo = new TodoListModel(key, input.getText().toString(), message.getText().toString(), 2);

                HashMap<String, Object> childUpdates = new HashMap<>();
                childUpdates.put(key, todo.toFireBaseObject());
                title.updateChildren(childUpdates, new DatabaseReference.CompletionListener() {
                    @Override
                    public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                        if (databaseError == null) {
                            dismiss();
                        }
                    }
                });
            }
        });
        return view;
    }
}
