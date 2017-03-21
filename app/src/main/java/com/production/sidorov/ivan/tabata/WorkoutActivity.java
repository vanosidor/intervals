package com.production.sidorov.ivan.tabata;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;
import com.production.sidorov.ivan.tabata.sync.TimerService;
import com.production.sidorov.ivan.tabata.sync.TimerWrapper;

/**
 * Created by Иван on 06.03.2017.
 */

public class WorkoutActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private TextView mWorkoutDetails;
    private Uri mUri;

    public static final String TAG = WorkoutActivity.class.getSimpleName();

    public static final int ID_LOADER_CURRENT_WORKOUT = 44;

    private TimerService mTimerService;
    private boolean mServiceIsBound;


    private Button timerButton;
    private TextView timerTextView;
    private TextView timerTextView1;
    private TextView timerTextView2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.v(TAG, "OnCreate");
        setContentView(R.layout.activity_workout);

        mWorkoutDetails = (TextView) findViewById(R.id.tv_workout_data_details);

        timerButton = (Button) findViewById(R.id.btn_test);
        timerTextView = (TextView) findViewById(R.id.tv_timer);
        timerTextView1 = (TextView) findViewById(R.id.tv_timer1);
        timerTextView2 = (TextView) findViewById(R.id.tv_timer2);

        mUri = getIntent().getData();

        if (mUri == null) throw new NullPointerException("Uri is null");

        getSupportLoaderManager().initLoader(ID_LOADER_CURRENT_WORKOUT, null, this);
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(mTimerService.isUriMatches()) updateGUI(intent);
        }
    };

    private void updateGUI(Intent intent) {
        if (intent == null ) return;
        String action = intent.getAction();
        switch (action) {
            case TimerWrapper.BROADCAST_WORKOUT_TICK: {
                long millisUntilFinished = intent.getLongExtra(TimerWrapper.INTENT_WORKOUT_EXTRA, 0);
                timerTextView1.setText(Long.toString(millisUntilFinished / 1000) + " seconds");
                //Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                break;
            }
            case TimerWrapper.BROADCAST_REST_TICK: {
                long millisUntilFinished = intent.getLongExtra(TimerWrapper.INTENT_REST_EXTRA, 0);
                timerTextView1.setText(Long.toString(millisUntilFinished / 1000) + " seconds");
            }
            case TimerWrapper.BROADCAST_WORKOUT_FINISH: {
                timerTextView2.setText("workout finished");
                break;
            }
            case TimerWrapper.BROADCAST_REST_FINISH: {
                timerTextView2.setText("rest finished");
                break;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.v(TAG, "onStart");
        Intent i = new Intent(this, TimerService.class);
        i.setData(mUri);
        startService(i);
        bindService(i, mConnection, 0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.v(TAG, "OnStop");
        updateUIStopRun();
        if (mServiceIsBound) {

            // If a timer is active, foreground the service, otherwise kill the service
            if (mTimerService.isTimerRunning()) {
                mTimerService.foreground(mUri);
            } else {
                stopService(new Intent(this, TimerService.class));
                Log.v(TAG, "Stop service");
            }
            // Unbind the service
            unbindService(mConnection);
            mServiceIsBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.v(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_WORKOUT_TICK));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_REST_TICK));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_WORKOUT_FINISH));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_REST_FINISH));
        Log.v(TAG, "onResume");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.v(TAG, "onDestroy");
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case ID_LOADER_CURRENT_WORKOUT: {
                return new CursorLoader(this, mUri, WorkoutDBHelper.WORKOUT_PROJECTION, null, null, null);
            }
            default:
                throw new RuntimeException("Loader Not Implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        boolean cursorHasValidData = false;
        if (data != null && data.moveToFirst()) {
            /* We have valid data, continue on to bind the data to the UI */
            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {
            /* No data to display, simply return and do nothing */
            return;
        }

        String name = data.getString(WorkoutDBHelper.INDEX_WORKOUT_NAME);

        mWorkoutDetails.setText(name);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    //On Button start clicked
    public void startTimer(View view) {
        Log.v(TAG, "Starting and binding service" + mUri);

        if (mServiceIsBound && !mTimerService.isTimerRunning()) {
            mTimerService.startTimer();
            updateUIStartRun();
        } else if (mServiceIsBound && mTimerService.isTimerRunning()) {
            mTimerService.stopTimer();
            updateUIStopRun();
        }
    }

    /**
     * Callback for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.v(TAG, "Service bound");

            TimerService.RunServiceBinder binder = (TimerService.RunServiceBinder) service;
            mTimerService = binder.getService();
            mServiceIsBound = true;
            // if uri matches for running workout and clicked workout service stay foreground
            if(mTimerService.isUriMatches()) {
                mTimerService.background();
            }
            // Update the UI if the service is already running the timer
            if (mTimerService.isTimerRunning()) {
                updateUIStartRun();
            }
         }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            if (Log.isLoggable(TAG, Log.VERBOSE)) {
                Log.v(TAG, "Service disconnect");
            }
            mServiceIsBound = false;
        }
    };

    /**
     * Updates the UI when a run starts
     */
    private void updateUIStartRun() {
        //mUpdateTimeHandler.sendEmptyMessage(MSG_UPDATE_TIME);
        timerButton.setText("Stop");
    }

    /**
     * Updates the UI when a run stops
     */
    private void updateUIStopRun() {
        // mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        timerButton.setText("Start");
    }


}
