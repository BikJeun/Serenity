package com.example.serenity;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ToDoListFragment extends Fragment {

    ArrayList<TodoListModel> models = new ArrayList<>();
    TodoListAdapter listAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todolist, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.hasFixedSize();
        listAdapter = new TodoListAdapter(getContext(), models);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listAdapter);

        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        TodoListModel today = new TodoListModel("TODAY","", 1);
        TodoListModel tmr = new TodoListModel("TOMORROW", "",1);
        TodoListModel upcoming = new TodoListModel("UPCOMING", "",1);

        database.getReference("TODAY").child(uid).push();
        database.getReference("TOMORROW").child(uid).push();
        database.getReference("UPCOMING").child(uid).push();

        models.add(today);
        models.add(tmr);
        models.add(upcoming);

    }
}