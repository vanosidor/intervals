package com.production.sidorov.ivan.tabata.sync;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.production.sidorov.ivan.tabata.NotificationUtils;
import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.utilitis.WorkoutTimeUtils;

/**
 * Created by Иван on 09.03.2017.
 */

public class TimerService extends Service implements TimerWrapper.TimerCallbacks {
    private static final String TAG = TimerService.class.getSimpleName();

    private final String[] mProjection = {
            WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_REST_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM,
    };

    public static final int INDEX_CURRENT_WORKOUT_TIME = 0;
    public static final int INDEX_CURRENT_REST_TIME = 1;
    public static final int INDEX_CURRENT_NUM_ROUNDS= 2;

    private Uri mUri;
    private Uri mUriTemp;

    private int mWorkoutState;

    private long mMillisUntilFinished;

    private boolean isTimerRunning;
    public static final int TICKER_INTERVAL = 1000;

    long mWorkoutTimeInMillis;
    long mRestTimeInMillis;
    int mRounds;

    private final IBinder serviceBinder = new RunServiceBinder();
    private TimerWrapper mTimerWrapper;

    @Override
    public void onCreate() {
        Log.v(TAG, "Service created");

        isTimerRunning = false;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Starting service");

        mUriTemp = intent.getData();
        if(mUriTemp == null) throw new NullPointerException("uri is null");
        Log.d(TAG,"mUri:"+mUri);

        String action = intent.getAction();
        if(null != action){
            if (action.equals(NotificationUtils.ACTION_CANCEL_WORKOUT_NOTIFICATION)){
                stopTimer();
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.v(TAG, "Binding service");

        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        Log.v(TAG, "Destroying service");
        super.onDestroy();
    }

    /*
    * Timer callbacks
    * */
    @Override
    public void timerFinished() {
        isTimerRunning = false;
    }

    @Override
    public void timerTick(long millisUntilFinished) {
        mMillisUntilFinished = millisUntilFinished;
    }

    @Override
    public void timerStateChange(int timerState) {
        mWorkoutState = timerState;
    }

    public long getMillisUntilFinished(){
        return mMillisUntilFinished;
    }

    public class RunServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }
    /**
     * Start the timer
     */
    public void startTimer() {

        Log.v(TAG, "Start timer");

        boolean cursorHasValidData = false;

        mUri = mUriTemp;

        //get current workout data from content provider
        Cursor cursor = getContentResolver().query(mUri, mProjection,null,null,null);

        if (cursor != null && cursor.moveToFirst()) {

            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {

            return;
        }

        String workoutTime = cursor.getString(INDEX_CURRENT_WORKOUT_TIME);
        String restTime = cursor.getString(INDEX_CURRENT_REST_TIME);
        int rounds = cursor.getInt(INDEX_CURRENT_NUM_ROUNDS);

        mWorkoutTimeInMillis = WorkoutTimeUtils.getTimeInMillis(workoutTime);
        mRestTimeInMillis = WorkoutTimeUtils.getTimeInMillis(restTime);
        mRounds = rounds;

        mTimerWrapper = new TimerWrapper(this,mWorkoutTimeInMillis,mRestTimeInMillis,mRounds,this);
        Log.d(TAG,"Create new TimerWrapper:"+mTimerWrapper.toString());

        mTimerWrapper.startTimer();

        isTimerRunning = true;

        cursor.close();
    }

    public long getCurrentWorkoutTimeInMillis(){
        return mWorkoutTimeInMillis;
    }

    public long getCurrentRestTimeInMillis(){
        return mRestTimeInMillis;
    }

    public int getWorkoutState(){
        return mWorkoutState;
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
        Log.v(TAG, "Stop timer");
        mTimerWrapper.stopTimer();
        isTimerRunning = false;
    }

    /**
     * @return whether the timer is running
     */
    public boolean isTimerRunning() {
        return isTimerRunning;
    }

    /**
     * Place the service into the foreground
     */
    public void foreground(Uri uri) {
        Log.v(TAG,"Service foreground");
        startForeground(NotificationUtils.NOTIFICATION_ID, NotificationUtils.showWorkoutNotification(this,uri));
    }

    /**
     *  Place the service to the background
     */
    public void background() {
        Log.v(TAG,"Service background");
        stopForeground(true);
    }

    /**
    * @return true if two Uri matches
     */

    public boolean isUriMatches(){
        boolean isUriMatches = false;
        if (mUriTemp != null && mUriTemp.equals(mUri)) isUriMatches = true;
        return isUriMatches;
    }

}
