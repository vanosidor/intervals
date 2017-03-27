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


    private Uri mUri;

    public static final String TAG = WorkoutActivity.class.getSimpleName();

    public static final int ID_LOADER_CURRENT_WORKOUT = 44;

    private TimerService mTimerService;
    private boolean mServiceIsBound;




    //UI
    private TextView mWorkoutNameTextView;
    private TextView mTimeTextView;
    private TextView mWorkoutTypeTextView;
    private HoloCircularProgressBar mProgressBar;
    private TextView mRoundsTitleTextView;
    private TextView mCurrentRoundTextView;
    private Button mStartButton;
    private Button mStopButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.v(TAG, "OnCreate");
        setContentView(R.layout.activity_workout);

        mStartButton = (Button) findViewById(R.id.startButton);
        mStopButton = (Button)findViewById(R.id.stopButton);

        mWorkoutNameTextView = (TextView) findViewById(R.id.workoutNameTextView);
        mTimeTextView = (TextView) findViewById(R.id.timeTextView);
        mWorkoutTypeTextView = (TextView) findViewById(R.id.workoutTypeTextView);
        mRoundsTitleTextView = (TextView)findViewById(R.id.roundsTitleTextView);
        mCurrentRoundTextView = (TextView)findViewById(R.id.currentRoundTextView);

        mRoundsTitleTextView.setText(R.string.round_title);
        mStartButton.setText(R.string.timer_start);
        mStopButton.setText(R.string.timer_cancel);


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
                mTimeTextView.setText(Long.toString(millisUntilFinished / 1000) + " seconds");
                //Log.i(TAG, "Countdown seconds remaining: " + millisUntilFinished / 1000);
                break;
            }
            case TimerWrapper.BROADCAST_REST_TICK: {
                long millisUntilFinished = intent.getLongExtra(TimerWrapper.INTENT_REST_EXTRA, 0);
                mTimeTextView.setText(Long.toString(millisUntilFinished / 1000) + " seconds");
            }
            case TimerWrapper.BROADCAST_WORKOUT_FINISH: {
                mWorkoutTypeTextView.setText("workout finished");
                break;
            }
            case TimerWrapper.BROADCAST_REST_FINISH: {
                mWorkoutTypeTextView.setText("rest finished");
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
        String workoutTime = data.getString(WorkoutDBHelper.INDEX_WORKOUT_TIME);
        String restTime = data.getString(WorkoutDBHelper.INDEX_REST_TIME);
        int rounds = data.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);

        mWorkoutNameTextView.setText(name);
        mTimeTextView.setText(workoutTime);
        mCurrentRoundTextView.setText(getString(R.string.format_rounds,rounds));
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
        mStartButton.setText("Stop");
    }

    /**
     * Updates the UI when a run stops
     */
    private void updateUIStopRun() {
        // mUpdateTimeHandler.removeMessages(MSG_UPDATE_TIME);
        mStartButton.setText("Start");
    }


}
