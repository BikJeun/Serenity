package com.example.serenity;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.serenity.data.TodoListModel;
import com.example.serenity.uihelpers.OnSwipeTouchListener;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Handler;

public class TodoListAdapter extends RecyclerView.Adapter<TodoListAdapter.ViewHolder> {
    private Context context;
    private ArrayList<TodoListModel> models = new ArrayList<>();
    private TodoListModel rootModel;
    //ItemTouchHelper itemTouchHelper = new ItemTouchHelper(itemTouch);
    //ItemTouchHelper itemTouchHelper.attachToRecyclerView(recyclerView);


    public TodoListAdapter(Context context, ArrayList<TodoListModel> models) {
        this.context = context;
        this.models = models;

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.todolist_items, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {

        TodoListModel model = models.get(position);
        holder.textView.setText(model.getName());
        holder.message.setText(model.getMessage());

        holder.itemView.setTag(R.string.MODEL, model);
        holder.itemView.setTag(R.string.position, position);

        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) holder.rlContent.getLayoutParams();
        layoutParams.setMargins(((int) convertDpToPixel(20, context)) * model.getLevel(), layoutParams.topMargin, layoutParams.rightMargin, layoutParams.bottomMargin);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        assert user != null;
        final String uid = user.getUid();
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference ref = database.getReference(uid).child(model.getName());

        switch (model.getState()) {

            case CLOSED:
                holder.imgArrow.setImageResource(R.drawable.next);
                break;
            case OPENED:
                holder.imgArrow.setImageResource(R.drawable.down);
                break;
        }

        if (model.getLevel() == 2) {
            holder.imgArrow.setVisibility(View.INVISIBLE);
            holder.viewDashed.setVisibility(View.VISIBLE);
            holder.addbutton.setVisibility(View.GONE);
        } else {
            holder.imgArrow.setVisibility(View.VISIBLE);
            holder.viewDashed.setVisibility(View.INVISIBLE);
            holder.addbutton.setVisibility(View.VISIBLE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                int position = (int) v.getTag(R.string.position);
                rootModel = (TodoListModel) v.getTag(R.string.MODEL);

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        rootModel.getModels().clear();
                        for (DataSnapshot data : dataSnapshot.getChildren()) {
                            Log.d("creating data", "onClick: " + data);
                            String id = data.getKey();
                            String task = data.child("Task").getValue(String.class);
                            String msg = data.child("message").getValue(String.class);
                            TodoListModel todo = new TodoListModel(id, task, msg, 2);

                            rootModel.getModels().add(todo);
                        }
                        notifyDataSetChanged();
                    }
                    //}

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

                if (rootModel.getModels().isEmpty() && rootModel.getLevel() == 1) {
                    Toast.makeText(context, "EMPTY", Toast.LENGTH_SHORT).show();
                    return;
                }

                switch (rootModel.getState()) {

                    case CLOSED:
                        models.addAll(position + 1, rootModel.getModels());
                        notifyItemRangeInserted(position + 1, rootModel.getModels().size());
                        notifyItemRangeChanged(position + rootModel.getModels().size(), models.size() - (position + rootModel.getModels().size()));
                        notifyItemRangeChanged(position, models.size() - position);
                        rootModel.setState(TodoListModel.STATE.OPENED);
                        break;

                    case OPENED:
                        int start = position + 1;
                        int end = models.size();
                        for (int i = start; i < models.size(); i++) {
                            TodoListModel model1 = models.get(i);
                            if (model1.getLevel() <= rootModel.getLevel()) {
                                end = i;
                                break;
                            } else {
                                if (model1.getState() == TodoListModel.STATE.OPENED) {
                                    model1.setState(TodoListModel.STATE.CLOSED);
                                }
                            }
                        }

                        if (end != -1) {
                            models.subList(start, end).clear();
                            notifyItemRangeRemoved(start, end - start);
                            notifyItemRangeChanged(start, end - start);
                            notifyItemRangeChanged(position, models.size() - position);
                        }

                        rootModel.setState(TodoListModel.STATE.CLOSED);
                        break;
                }

                holder.itemView.setOnTouchListener(new OnSwipeTouchListener(context) {
                    public void onSwipeLeft() {
                        Toast.makeText(context, "delete",Toast.LENGTH_SHORT).show();
                    }

                    public void onSwipeRight() {
                        Toast.makeText(context, "timer", Toast.LENGTH_SHORT).show();
                    }
                });


            }
        });


        holder.addbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = holder.textView.getText().toString();
                DatabaseReference ref = database.getReference(uid).child(name);
                //Create BottomSheetDialog
                TodoListBottomSheet bottomSheet = new TodoListBottomSheet();
                bottomSheet.setTitle(ref);
                bottomSheet.show(((FragmentActivity) context).getSupportFragmentManager(), "addItemsSheet");
            }
        });
    }


    @Override
    public int getItemCount() {
        return models.size();
    }

    public void setData(ArrayList<TodoListModel> list) {
        models = list;
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

        RelativeLayout rlContent;
        TextView textView;
        TextView message;
        ImageView imgArrow;
        View viewDashed;
        ImageView addbutton;
        TextView empty;
        //View background;


        public ViewHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(R.id.tvName);
            message = (TextView) itemView.findViewById(R.id.tvDesignation);
            imgArrow = (ImageView) itemView.findViewById(R.id.imgArrow);
            rlContent = (RelativeLayout) itemView.findViewById(R.id.rlContent);
            viewDashed = itemView.findViewById(R.id.viewDashed);
            addbutton = (ImageView) itemView.findViewById(R.id.additems);
            empty = (TextView) itemView.findViewById(R.id.empty);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            assert user != null;
            final String uid = user.getUid();
            final FirebaseDatabase database = FirebaseDatabase.getInstance();
            final DatabaseReference ref = database.getReference(uid).child(models.get(getAdapterPosition()).getName());

            ref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    rootModel.getModels().clear();
                    for (DataSnapshot data : dataSnapshot.getChildren()) {
                        String id = data.getKey();
                        String task = data.child("Task").getValue(String.class);
                        String msg = data.child("message").getValue(String.class);
                        TodoListModel todo = new TodoListModel(id, task, msg, 2);

                        rootModel.getModels().add(todo);
                    }
                    notifyDataSetChanged();
                }
                //}

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
    }

    public static void removeTaskObject () {

    }



    public static float convertDpToPixel(float dp, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float px = dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return px;
    }

    /**
     * This method converts device specific pixels to density independent pixels.
     *
     * @param px      A value in px (pixels) unit. Which we need to convert into db
     * @param context Context to get resources and device specific display metrics
     * @return A float value to represent dp equivalent to px value
     */
    public static float convertPixelsToDp(float px, Context context) {
        Resources resources = context.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        float dp = px / ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT);
        return dp;
    }

    public TodoListModel getRootModel() {
        return rootModel;
    }
}