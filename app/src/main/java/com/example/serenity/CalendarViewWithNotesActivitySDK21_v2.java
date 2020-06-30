package com.example.serenity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.serenity.data.CalenderEventModel;
import com.example.serenity.uihelpers.CalendarDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.hugoandrade.calendarviewlib.CalendarView;

import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;

//@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CalendarViewWithNotesActivitySDK21_v2 extends Fragment {

    private final static int CREATE_EVENT_REQUEST_CODE = 100;

    private String[] mShortMonths;
    private CalendarView mCalendarView;
    private CalendarDialog mCalendarDialog;

    private List<CalenderEventModel> mEventList = new ArrayList<>();

    private static Context context = null;

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    final String uid = user.getUid();
    final FirebaseDatabase db = FirebaseDatabase.getInstance();
    final DatabaseReference ref = db.getReference("Calendar Events").child(uid);

    public static Intent makeIntent(Context context) {
        return new Intent(context, CalendarViewWithNotesActivitySDK21_v2.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.activity_calendar_view_with_notes_sdk_21, container, false);

        //getFirebaseData();

        //mShortMonths = new DateFormatSymbols().getShortMonths();

        initializeUI(v);
        //getFirebaseData();

        return v;
    }

    private void getFirebaseData() {

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                mEventList.clear();
                for(DataSnapshot ds : dataSnapshot.getChildren()) {
                    String id = ds.getKey();
                    String title = ds.child("title").getValue(String.class);
                    Long dateInLong = ds.child("date").getValue(Long.class);
                    int color = ds.child("color").getValue(Integer.class);
                    boolean complete = ds.child("completed").getValue(boolean.class);

                    Calendar date = Calendar.getInstance();
                    date.setTime(new Date(dateInLong));

                    CalenderEventModel model = new CalenderEventModel(id,title,date,color,complete);
                    mEventList.add(model);
                    Log.d("checking on firebase", "" + mEventList.size());
                    mCalendarView.addCalendarObject(parseCalendarObject(model));
                    mCalendarDialog.setEventList(mEventList);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("create", "onCreate: ");

        getFirebaseData();
        Log.d("checking", "" + mEventList.size());
        mShortMonths = new DateFormatSymbols().getShortMonths();

        //initializeUI();
    }

    private void initializeUI(View v) {

        //setContentView(R.layout.activity_calendar_view_with_notes_sdk_21);

        Toolbar toolbar = v.findViewById(R.id.toolbar);
        ((AppCompatActivity)getActivity()).setSupportActionBar(toolbar);

        mCalendarView = v.findViewById(R.id.calendarView);
        mCalendarView.setOnMonthChangedListener(new CalendarView.OnMonthChangedListener() {
            @Override
            public void onMonthChanged(int month, int year) {
                if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mShortMonths[month]);
                    ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(Integer.toString(year));
                }
            }
        });
        mCalendarView.setOnItemClickedListener(new CalendarView.OnItemClickListener() {
            @Override
            public void onItemClicked(List<CalendarView.CalendarObject> calendarObjects,
                                      Calendar previousDate,
                                      Calendar selectedDate) {
                if (calendarObjects.size() != 0) {
                    mCalendarDialog.setSelectedDate(selectedDate);
                    mCalendarDialog.show();
                }
                else {
                    if (diffYMD(previousDate, selectedDate) == 0)
                        createEvent(selectedDate);
                }
            }
        });

        for (CalenderEventModel e : mEventList) {
            mCalendarView.addCalendarObject(parseCalendarObject(e));
        }

        if (((AppCompatActivity)getActivity()).getSupportActionBar() != null) {
            int month = mCalendarView.getCurrentDate().get(Calendar.MONTH);
            int year = mCalendarView.getCurrentDate().get(Calendar.YEAR);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle(mShortMonths[month]);
            ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(Integer.toString(year));
        }

        FloatingActionButton fab = v.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createEvent(mCalendarView.getSelectedDate());
            }
        });

        mCalendarDialog = CalendarDialog.Builder.instance(getActivity())
                .setEventList(mEventList)
                .setOnItemClickListener(new CalendarDialog.OnCalendarDialogListener() {
                    @Override
                    public void onEventClick(CalenderEventModel event) {
                        onEventSelected(event);
                    }

                    @Override
                    public void onCreateEvent(Calendar calendar) {
                        createEvent(calendar);
                    }
                })
                .create();
    }

    private void onEventSelected(CalenderEventModel event) {
        Activity context = getActivity();
        Intent intent = CreateEventActivity.makeIntent(context, event);

        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        getActivity().overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    private void createEvent(Calendar selectedDate) {
        Activity context = getActivity();
        Intent intent = CreateEventActivity.makeIntent(context, selectedDate);

        startActivityForResult(intent, CREATE_EVENT_REQUEST_CODE);
        getActivity().overridePendingTransition( R.anim.slide_in_up, R.anim.stay );
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        getActivity().getMenuInflater().inflate(R.menu.menu_toolbar_calendar_view, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_today: {
                mCalendarView.setSelectedDate(Calendar.getInstance());
                return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int action = CreateEventActivity.extractActionFromIntent(data);
                CalenderEventModel event = CreateEventActivity.extractEventFromIntent(data);
                Log.d("Checking on results", "onActivityResult: " + event.getTitle());
                Log.d("checking on results", "" + mEventList.size());
                //ref.addChildEventListener(childEventListener);

                switch (action) {
                    case CreateEventActivity.ACTION_CREATE: {
                        mEventList.add(event);
                        mCalendarView.addCalendarObject(parseCalendarObject(event));
                        mCalendarDialog.setEventList(mEventList);
                        break;
                    }
                    case CreateEventActivity.ACTION_EDIT: {
                        CalenderEventModel oldEvent = null;
                        for (CalenderEventModel e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mEventList.add(event);

                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));
                            mCalendarView.addCalendarObject(parseCalendarObject(event));
                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                    case CreateEventActivity.ACTION_DELETE: {
                        CalenderEventModel oldEvent = null;
                        for (CalenderEventModel e : mEventList) {
                            if (Objects.equals(event.getID(), e.getID())) {
                                oldEvent = e;
                                break;
                            }
                        }
                        if (oldEvent != null) {
                            mEventList.remove(oldEvent);
                            mCalendarView.removeCalendarObjectByID(parseCalendarObject(oldEvent));
                            mCalendarDialog.setEventList(mEventList);
                        }
                        break;
                    }
                }
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public static int diffYMD(Calendar date1, Calendar date2) {
        if (date1.get(Calendar.YEAR) == date2.get(Calendar.YEAR) &&
                date1.get(Calendar.MONTH) == date2.get(Calendar.MONTH) &&
                date1.get(Calendar.DAY_OF_MONTH) == date2.get(Calendar.DAY_OF_MONTH))
            return 0;

        return date1.before(date2) ? -1 : 1;
    }

    private static CalendarView.CalendarObject parseCalendarObject(CalenderEventModel event) {
        return new CalendarView.CalendarObject(
                event.getID(),
                event.getDate(),
                event.getColor(),
                event.isCompleted() ? Color.TRANSPARENT : Color.RED);
    }

    ChildEventListener childEventListener = new ChildEventListener() {
        @Override
        public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d("childAdded", "onChildAdded:" + dataSnapshot.getKey());

            String id = dataSnapshot.getKey();
            String title = dataSnapshot.child("title").getValue(String.class);
            Long dateInLong = dataSnapshot.child("date").getValue(Long.class);
            int color = dataSnapshot.child("color").getValue(Integer.class);
            boolean complete = dataSnapshot.child("completed").getValue(boolean.class);

            Calendar date = Calendar.getInstance();
            date.setTime(new Date(dateInLong));

            CalenderEventModel model = new CalenderEventModel(id,title,date,color,complete);
            mEventList.add(model);

            mCalendarView.addCalendarObject(parseCalendarObject(model));
            mCalendarDialog.setEventList(mEventList);

        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {
            Log.d("childChanged", "onChildChanged:" + dataSnapshot.getKey());

            String id = dataSnapshot.getKey();
            String title = dataSnapshot.child("title").getValue(String.class);
            Long dateInLong = dataSnapshot.child("date").getValue(Long.class);
            int color = dataSnapshot.child("color").getValue(Integer.class);
            boolean complete = dataSnapshot.child("completed").getValue(boolean.class);

            Calendar date = Calendar.getInstance();
            date.setTime(new Date(dateInLong));

            CalenderEventModel model = new CalenderEventModel(id,title,date,color,complete);
            mEventList.add(model);

            mCalendarView.addCalendarObject(parseCalendarObject(model));
            mCalendarDialog.setEventList(mEventList);

        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

        }

        @Override
        public void onCancelled(@NonNull DatabaseError databaseError) {
            Log.w("calendar error", "postComments:onCancelled", databaseError.toException());
            Toast.makeText(getContext(), "Failed to load comments.",
                    Toast.LENGTH_SHORT).show();
        }
    };


}
