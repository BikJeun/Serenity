package com.example.serenity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import com.example.serenity.data.CalenderEventModel;
import com.example.serenity.uihelpers.CalendarDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.hugoandrade.calendarviewlib.CalendarView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
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

    public static Intent makeIntent(Context context) {
        return new Intent(context, CalendarViewWithNotesActivitySDK21_v2.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        context = getActivity();
        setHasOptionsMenu(true);
        View v = inflater.inflate(R.layout.activity_calendar_view_with_notes_sdk_21, container, false);

        //mShortMonths = new DateFormatSymbols().getShortMonths();

        initializeUI(v);

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CREATE_EVENT_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int action = CreateEventActivity.extractActionFromIntent(data);
                CalenderEventModel event = CreateEventActivity.extractEventFromIntent(data);

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

}
