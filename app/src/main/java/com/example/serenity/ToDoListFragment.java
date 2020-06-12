package com.example.serenity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LongDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serenity.data.TodoListModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;

public class ToDoListFragment extends Fragment {

    ArrayList<TodoListModel> models = new ArrayList<>();
    TodoListAdapter listAdapter;
    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final String uid = user.getUid();
    private Paint p = new Paint();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_todolist, container, false);
        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.list);
        recyclerView.hasFixedSize();
        listAdapter = new TodoListAdapter(getContext(), models);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(listAdapter);

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouch);
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final String uid = user.getUid();
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        TodoListModel today = new TodoListModel("","TODAY", "", 1);
        TodoListModel tmr = new TodoListModel("","TOMORROW", "", 1);
        TodoListModel upcoming = new TodoListModel("","UPCOMING", "", 1);

        database.getReference("TODAY").child(uid).push();
        database.getReference("TOMORROW").child(uid).push();
        database.getReference("UPCOMING").child(uid).push();

        models.add(today);
        models.add(tmr);
        models.add(upcoming);

    }

    ItemTouchHelper.SimpleCallback itemTouch = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            switch (direction) {
                case ItemTouchHelper.LEFT:
                    int position = viewHolder.getAdapterPosition();
                    TodoListModel parent = listAdapter.getRootModel();
                    final TodoListModel deletedModel = models.get(position);
                    final int deletedPos = position;
                    final boolean[] confirm = new boolean[1];

                    Snackbar snackbar = Snackbar.make(getActivity().findViewById(R.id.rlContent), "removed from Recyclerview!", Snackbar.LENGTH_LONG);
                    snackbar.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirm[0] = false;
                        }
                    });
                    snackbar.show();
                    if (confirm[0] == false) {
                        return;
                    }
                    deleteData(parent, uid, deletedModel);

                    listAdapter.notifyItemRemoved(position);
                    break;

                case ItemTouchHelper.RIGHT:
                    startActivity(new Intent(getActivity(), LockApp.class));
                    int position2 = viewHolder.getAdapterPosition();
                    TodoListModel parent2 = listAdapter.getRootModel();
                    final TodoListModel deletedModel2 = models.get(position2);
                    final int deletedPos2 = position2;
                    final boolean[] confirm2 = new boolean[1];

                    Snackbar snackbar2 = Snackbar.make(getActivity().findViewById(R.id.rlContent), "removed from Recyclerview!", Snackbar.LENGTH_LONG);
                    snackbar2.setAction("UNDO", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            confirm2[0] = false;
                        }
                    });
                    snackbar2.show();
                    if (confirm2[0] == false) {
                        return;
                    }
                    deleteData(parent2, uid, deletedModel2);

                    listAdapter.notifyItemRemoved(position2);
                    break;
            }

        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            Bitmap icon;
            if (actionState == ItemTouchHelper.ACTION_STATE_SWIPE) {

                View itemView = viewHolder.itemView;
                float height = (float) itemView.getBottom() - (float) itemView.getTop();
                float width = height / 3;

                if (dX > 0) {
                    p.setColor(Color.parseColor("#388E3C"));
                    RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX, (float) itemView.getBottom());
                    c.drawRect(background, p);
                   // icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_forever_black_24dp);
                    Drawable icons = getResources().getDrawable(R.drawable.ic_done_black_24dp);
                    icon = drawableToBitmap(icons);
                    RectF icon_dest = new RectF((float) itemView.getLeft() + width, (float) itemView.getTop() + width, (float) itemView.getLeft() + 2 * width, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, p);
                    Paint paint = new Paint();
                    paint.setColor(Color.BLACK);
                    paint.setTextSize(50);
                    paint.setTextAlign(Paint.Align.CENTER);
                    String begin = itemView.getContext().getResources().getString(R.string.begin);
                    c.drawText(begin,  itemView.getLeft() + 2 * width - 30, itemView.getBottom() - width + 35, paint);
                } else {
                    p.setColor(Color.parseColor("#D32F2F"));
                    RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
                    c.drawRect(background, p);
                    // icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_forever_black_24dp);
                    Drawable icons = getResources().getDrawable(R.drawable.ic_delete_forever_black_24dp);
                    icon = drawableToBitmap(icons);
                    RectF icon_dest = new RectF((float) itemView.getRight() - 2 * width, (float) itemView.getTop() + width, (float) itemView.getRight() - width, (float) itemView.getBottom() - width);
                    c.drawBitmap(icon, null, icon_dest, p);
                }
            }
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        }

        private void deleteData(TodoListModel parent, String uid, TodoListModel child) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference(parent.getName()).child(uid);
            ref.child(child.getId()).removeValue();

        }
    };

    public static Bitmap drawableToBitmap (Drawable drawable) {

        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

}