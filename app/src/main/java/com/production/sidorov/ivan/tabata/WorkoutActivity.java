package com.production.sidorov.ivan.tabata;

import android.animation.ObjectAnimator;
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
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;
import com.production.sidorov.ivan.tabata.sync.TimerService;
import com.production.sidorov.ivan.tabata.sync.TimerWrapper;
import com.production.sidorov.ivan.tabata.utilitis.WorkoutTimeUtils;

/**
 * Created by Иван on 06.03.2017.
 */

public class WorkoutActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {


    private Uri mUri;

    public static final String TAG = WorkoutActivity.class.getSimpleName();

    public static final int ID_LOADER_CURRENT_WORKOUT = 44;

    private TimerService mTimerService;
    private boolean mServiceIsBound;

    String mWorkoutTime;
    String mRestTime;
    private int mRounds;

    //UI
    private TextView mWorkoutNameTextView;
    private TextView mTimeTextView;
    private TextView mWorkoutTypeTextView;
    private HoloCircularProgressBar mProgressBar;
    private TextView mRoundsTitleTextView;
    private TextView mCurrentRoundTextView;
    private Button mStartButton;
    private Button mStopButton;

    private ObjectAnimator mProgressBarAnimator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "OnCreate");
        setContentView(R.layout.activity_workout);

        mStartButton = (Button) findViewById(R.id.startButton);
        mStopButton = (Button) findViewById(R.id.stopButton);

        mProgressBar = (HoloCircularProgressBar) findViewById(R.id.holoCircularProgressBar);

        mWorkoutNameTextView = (TextView) findViewById(R.id.workoutNameTextView);
        mTimeTextView = (TextView) findViewById(R.id.timeTextView);
        mWorkoutTypeTextView = (TextView) findViewById(R.id.workoutTypeTextView);
        mRoundsTitleTextView = (TextView) findViewById(R.id.roundsTitleTextView);
        mCurrentRoundTextView = (TextView) findViewById(R.id.currentRoundTextView);

        mTimeTextView.setText(R.string.time_default);
        mRoundsTitleTextView.setText(R.string.round_title);
        mStartButton.setText(R.string.timer_start);
        mStopButton.setText(R.string.timer_cancel);

        mStopButton.setEnabled(false);

        mUri = getIntent().getData();

        if (mUri == null) throw new NullPointerException("Uri is null");

        getSupportLoaderManager().initLoader(ID_LOADER_CURRENT_WORKOUT, null, this);
    }

    /*
 * On WorkoutActivity started
 * */
    @Override
    protected void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");

        /*Start and bind service */
        Intent i = new Intent(this, TimerService.class);
        i.setData(mUri);
        startService(i);
        bindService(i, mConnection, 0);
    }
    /*
     * On Workout Activity stopped
     **/
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "OnStop");
        //updateUIStopRun();
        if (mServiceIsBound) {

            /* If a timer is active, foreground the service, otherwise kill the service*/
            if (mTimerService.isTimerRunning()) {
                mTimerService.foreground(mUri);
            } else {
                stopService(new Intent(this, TimerService.class));
                Log.v(TAG, "Stop service");
            }
            /*Unbind the service */
            unbindService(mConnection);
            mServiceIsBound = false;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
        Log.d(TAG, "onPause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_WORKOUT_TICK));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_REST_TICK));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_WORKOUT_FINISH));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_REST_FINISH));
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_FINISH_ALL));
        Log.d(TAG, "onResume");
    }


    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*If uri clicked matches with current uri update UI */
            if (mTimerService.isUriMatches()) updateUI(intent);
        }
    };

    /*
    * Cursor Loader callbacks
    **/
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        /*Load the information about current workout*/
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

        Log.d(TAG, "Loader Finished");

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
        mWorkoutTime = data.getString(WorkoutDBHelper.INDEX_WORKOUT_TIME);
        mRestTime = data.getString(WorkoutDBHelper.INDEX_REST_TIME);
        mRounds = data.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);
        mWorkoutNameTextView.setText(name);
        mCurrentRoundTextView.setText(getString(R.string.format_rounds, mRounds));
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    /**
     * Callback for service binding, passed to bindService()
     */
    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "On service connected");

            TimerService.RunServiceBinder binder = (TimerService.RunServiceBinder) service;
            mTimerService = binder.getService();

            Log.d(TAG,mTimerService.toString());

            if (null == mTimerService) return;

            mServiceIsBound = true;
            /*
            * If uri not matches for running workout and clicked workout service stay foreground,else restore UI state
            * */
            if (mTimerService.isUriMatches()) {

                mTimerService.background();
                //Restore Activity state
                restoreUI(mTimerService);
            }
            /*
            *if timer not running and binding service set stop UI(when close notification window and reopen app)
            * */
            if(!mTimerService.isTimerRunning()){
                updateUIStopRun();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

                Log.d(TAG, "Service disconnect");

            mServiceIsBound = false;
        }
    };

   /*
   * On Button start clicked
   * */
    public void startTimer(View view) {
        Log.d(TAG, "Start button clicked: " + mUri);

        if (mServiceIsBound) {
            //if click start when another timer is running
            if (mTimerService.isTimerRunning()) {
                mTimerService.stopTimer();
                mTimerService.background();
            }
            mTimerService.startTimer();
            updateUIStartRun();
        }
    }

    /*
    *On Button stop clicked
    **/
    public void stopTimer(View view){
        Log.d(TAG, "Stop button clicked: " + mUri);
        if (mServiceIsBound && mTimerService.isTimerRunning()) {
            mTimerService.stopTimer();
            updateUIStopRun();
        }
    }

    /**
     * Updates the UI when a run starts
     */
    private void updateUIStartRun() {
        Log.d(TAG, "Update UIStartRun");
        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);
        mWorkoutTypeTextView.setText(R.string.workout_text);
        mCurrentRoundTextView.setText(getString(R.string.format_rounds, mRounds));

        mProgressBar.setProgress(0.0f);
        animateProgressBar(mProgressBar,1.0f, (int) WorkoutTimeUtils.getTimeInMillis(mWorkoutTime));
    }

    /**
     * Updates the UI when a run stops
     */
    private void updateUIStopRun() {
        Log.d(TAG, "Update UIStopRun");

        mStartButton.setEnabled(true);
        mStopButton.setEnabled(false);
        mWorkoutTypeTextView.setText("");

        if (mProgressBarAnimator != null) {
            mProgressBarAnimator.cancel();
        }

        mProgressBar.setProgress(0.0f);

        mCurrentRoundTextView.setText(getString(R.string.format_rounds, mRounds));
    }

    /*
   **Update UI when BroadcastReceiver received events from Timer
   **/
    private void updateUI(Intent intent) {
        if (intent == null) return;
        String action = intent.getAction();
        switch (action) {
            case TimerWrapper.BROADCAST_WORKOUT_TICK: {
                long millisUntilFinished = intent.getLongExtra(TimerWrapper.INTENT_WORKOUT_EXTRA, 0);
                mTimeTextView.setText(WorkoutTimeUtils.timeInMillisToString(millisUntilFinished));
                break;
            }
            case TimerWrapper.BROADCAST_REST_TICK: {
                long millisUntilFinished = intent.getLongExtra(TimerWrapper.INTENT_REST_EXTRA, 0);
                mTimeTextView.setText(WorkoutTimeUtils.timeInMillisToString(millisUntilFinished));
                break;
            }
            case TimerWrapper.BROADCAST_WORKOUT_FINISH: {

                mProgressBar.setProgress(0.0f);
                //Animate ProgressBar rest when workout finished
                animateProgressBar(mProgressBar, 1.0f, (int) WorkoutTimeUtils.getTimeInMillis(mRestTime));
                mWorkoutTypeTextView.setText(R.string.rest_text);
                break;
            }
            case TimerWrapper.BROADCAST_REST_FINISH: {
                if (intent.hasExtra(TimerWrapper.INTENT_CURRENT_ROUND_NUM_EXTRA)) {
                    int roundNumCurrent = intent.getIntExtra(TimerWrapper.INTENT_CURRENT_ROUND_NUM_EXTRA, 0);
                    if (roundNumCurrent < mRounds) {
                        mCurrentRoundTextView.setText(getString(R.string.format_rounds_started, ++roundNumCurrent, mRounds));
                        mProgressBar.setProgress(0.0f);
                        //Animate ProgressBar workout when rest finished if not end yet
                        animateProgressBar(mProgressBar,1.0f, (int) WorkoutTimeUtils.getTimeInMillis(mWorkoutTime));
                    }
                }
                mWorkoutTypeTextView.setText(R.string.workout_text);
                break;
            }
            /*WorkoutTimer is finished work */
            case TimerWrapper.BROADCAST_FINISH_ALL: {
                Log.d(TAG, "workout finished work");

                mStartButton.setEnabled(true);
                mStopButton.setEnabled(false);
                mWorkoutTypeTextView.setText("");
                break;
            }
        }
    }

    /*
    * Restore UI and ProgressBarAnimation when return to the WorkoutActivity
    */
    private void restoreUI(TimerService timerService) {
        Log.d(TAG, "restoreUI state");

        mStartButton.setEnabled(false);
        mStopButton.setEnabled(true);

        /*get Workout Data about current Workout from TimerService*/
        long millisUntilFinished = timerService.getMillisUntilFinished();
        long workoutTime = timerService.getCurrentWorkoutTimeInMillis();
        long restTime = timerService.getCurrentRestTimeInMillis();

        /*get state:WORKOUT or REST*/
        int state = timerService.getWorkoutState();
        float progress;

        /*animate ProgressBar and setWorkoutType */
        if (state == TimerWrapper.STATE_WORKOUT) {
            progress = (float) (workoutTime-millisUntilFinished )/ workoutTime;
            mWorkoutTypeTextView.setText(R.string.workout_text);
        } else {
            progress = (float) (restTime - millisUntilFinished) / restTime;
            mWorkoutTypeTextView.setText(R.string.rest_text);
        }

        mProgressBar.setProgress(progress);

        animateProgressBar(mProgressBar, 1.0f, (int) millisUntilFinished);
    }

    /*
    *Animation Progress Bar
    **/
    private void animateProgressBar(final HoloCircularProgressBar progressBar,
                                    final float progress, final int duration) {
        mProgressBarAnimator = ObjectAnimator.ofFloat(progressBar, "progress", progress);
        mProgressBarAnimator.setDuration(duration);
        mProgressBarAnimator.setInterpolator(new LinearInterpolator());
        mProgressBarAnimator.start();
    }

}
