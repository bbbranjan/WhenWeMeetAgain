package com.example.suyashl.WhenWeMeetAgain;

/**
 * Created by SuyashL on 24/1/15.
 */


import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import com.example.suyashl.WhenWeMeetAgain.FriendsDbAdapter;


public class ReminderEditActivity extends Activity {

    //
    // Dialog Constants
    //
    private static final int DATE_PICKER_DIALOG = 0;
    private static final int TIME_PICKER_DIALOG = 1;

    //
    // Date Format
    //
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd kk:mm:ss";

    private EditText mReminderText;
    private Button mConfirmButton;
    private Button mAddPersonButton;
    private Long mRowId;
    private RemindersDbAdapter mDbHelper;
    private FriendsDbAdapter mDbHelper2;
    private FriendsDbAdapter mDb2;
    private Calendar mCalendar;
    private Spinner mPerson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mDbHelper = new RemindersDbAdapter(this);
        mDbHelper2 = new FriendsDbAdapter(this);

        setContentView(R.layout.activity_edit);

        mCalendar = Calendar.getInstance();
        mReminderText = (EditText) findViewById(R.id.MainReminder);
        mPerson = (Spinner) findViewById(R.id.spinner);
        mConfirmButton = (Button) findViewById(R.id.button);
        mAddPersonButton = (Button) findViewById(R.id.button2);

        mRowId = savedInstanceState != null ? savedInstanceState.getLong(RemindersDbAdapter.KEY_ROWID)
                : null;

        mAddPersonButton.setOnClickListener(button2Listener);

        registerButtonListenersAndSetDefaultText();

        loadspinnerdata();
    }

    private void loadspinnerdata() {
        // Spinner Drop down elements
        mDbHelper2.open();
        List<String> labels = mDbHelper2.getAllLabels();

        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, labels);

        // Drop down layout style - list view with radio button
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // attaching data adapter to spinner
        mPerson.setAdapter(dataAdapter);
    }

    private Button.OnClickListener button2Listener
            = new Button.OnClickListener(){

        @Override
        public void onClick(View arg0) {
            addnewFriend();
            finish();
        }};

    private void addnewFriend() {
        Intent i = new Intent(this, BluetoothScanner.class);
        startActivityForResult(i, 0);
    }

    private void setRowIdFromIntent() {
        if (mRowId == null) {
            Bundle extras = getIntent().getExtras();
            mRowId = extras != null ? extras.getLong(RemindersDbAdapter.KEY_ROWID)
                    : null;

        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDbHelper.close();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mDbHelper.open();
        setRowIdFromIntent();
        populateFields();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch(id) {
            case DATE_PICKER_DIALOG:
                return showDatePicker();
            case TIME_PICKER_DIALOG:
                return showTimePicker();
        }
        return super.onCreateDialog(id);
    }

    private DatePickerDialog showDatePicker() {


        DatePickerDialog datePicker = new DatePickerDialog(ReminderEditActivity.this, new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                mCalendar.set(Calendar.YEAR, year);
                mCalendar.set(Calendar.MONTH, monthOfYear);
                mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            }
        }, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH), mCalendar.get(Calendar.DAY_OF_MONTH));
        return datePicker;
    }

    private TimePickerDialog showTimePicker() {

        TimePickerDialog timePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {

            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                mCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                mCalendar.set(Calendar.MINUTE, minute);
            }
        }, mCalendar.get(Calendar.HOUR_OF_DAY), mCalendar.get(Calendar.MINUTE), true);

        return timePicker;
    }

    private void registerButtonListenersAndSetDefaultText() {
        mConfirmButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                saveState();
                setResult(RESULT_OK);
                Toast.makeText(ReminderEditActivity.this, "Reminder Saved.", Toast.LENGTH_SHORT).show();
                finish();
            }

        });
    }

    private void populateFields()  {



        // Only populate the text boxes and change the calendar date
        // if the row is not null from the database.
        if (mRowId != null) {
            Cursor reminder = mDbHelper.fetchReminder(mRowId);
            startManagingCursor(reminder);
            mReminderText.setText(reminder.getString(
                    reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_REMINDER)));
            mPerson.setSelection(((ArrayAdapter<String>)mPerson.getAdapter()).getPosition((reminder.getString(
                    reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_NAME)))));


            // Get the date from the database and format it for our use.
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
            Date date = null;
            try {
                String dateString = reminder.getString(reminder.getColumnIndexOrThrow(RemindersDbAdapter.KEY_DATE_TIME));
                date = dateTimeFormat.parse(dateString);
                mCalendar.setTime(date);
            } catch (ParseException e) {
                Log.e("ReminderEditActivity", e.getMessage(), e);
            }
        } else {
            // This is a new task - add defaults from preferences if set.
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            String defaultTitleKey = "Default Reminder.";
            String defaultTimeKey = "Default Person.";

            String defaultTitle = prefs.getString(defaultTitleKey, null);
            String defaultTime = prefs.getString(defaultTimeKey, null);

            if(defaultTitle != null)
                mReminderText.setText(defaultTitle);

            if(defaultTime != null)
                mCalendar.add(Calendar.MINUTE, Integer.parseInt(defaultTime));

        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(RemindersDbAdapter.KEY_ROWID, mRowId);
    }



    private void saveState() {
        mDb2 = new FriendsDbAdapter(this);

        String title = mReminderText.getText().toString();
        String person_name = mPerson.getSelectedItem().toString();
        String bluetooth = mDb2.SearchTable(person_name);

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DATE_TIME_FORMAT);
        String reminderDateTime = dateTimeFormat.format(mCalendar.getTime());

        if (mRowId == null) {

            long id = mDbHelper.createReminder(title, person_name, bluetooth, reminderDateTime);
            if (id > 0) {
                mRowId = id;
            }
        } else {
            mDbHelper.updateReminder(mRowId, title, person_name, bluetooth, reminderDateTime);
        }

    }


}

