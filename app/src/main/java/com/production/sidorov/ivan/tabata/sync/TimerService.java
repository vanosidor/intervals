package com.production.sidorov.ivan.tabata.sync;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.production.sidorov.ivan.tabata.NotificationUtils;
import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.utilitis.TimeUtils;

/**
 * Created by Иван on 09.03.2017.
 */

public class TimerService extends Service implements TimerWrapper.OnTimerFinishedHandler {
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

    // Is the service tracking time?
    private boolean isTimerRunning;
    public static final int TICKER_INTERVAL = 1000;

    //test data for testing work
    long mWorkoutTimeInMillis;
    long mRestTimeInMillis;
    int mRounds;

    // Service binder
    private final IBinder serviceBinder = new RunServiceBinder();
    private TimerWrapper mTimerWrapper;

    @Override
    public void timerFinished() {
        isTimerRunning = false;

    }

    private class QueryToDatabase extends AsyncTask <Uri,Void,Cursor>{

        @Override
        protected Cursor doInBackground(Uri... uris) {
            return  getContentResolver().query(uris[0], mProjection,null,null,null);
        }

        @Override
        protected void onPostExecute(Cursor data) {
            boolean cursorHasValidData = false;
            if (data != null && data.moveToFirst()) {

                cursorHasValidData = true;
            }

            if (!cursorHasValidData) {

                return;
            }

            String workoutTime = data.getString(INDEX_CURRENT_WORKOUT_TIME);
            String restTime = data.getString(INDEX_CURRENT_REST_TIME);
            int rounds = data.getInt(INDEX_CURRENT_NUM_ROUNDS);

            mWorkoutTimeInMillis = TimeUtils.getTimeInMillis(workoutTime);
            mRestTimeInMillis = TimeUtils.getTimeInMillis(restTime);
            mRounds = rounds;

            data.close();
        }
    }


    public class RunServiceBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    @Override
    public void onCreate() {
        Log.v(TAG, "Service created");
        isTimerRunning = false;

        //mTimerWrapper = new TimerWrapper(this,workoutTimeInMillis,restTimeInMillis,rounds,this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.v(TAG, "Starting service");


        mUriTemp = intent.getData();
        if(mUriTemp == null) throw new NullPointerException("uri is null");



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

    /**
     * Starts the timer
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

        mWorkoutTimeInMillis = TimeUtils.getTimeInMillis(workoutTime);
        mRestTimeInMillis = TimeUtils.getTimeInMillis(restTime);
        mRounds = rounds;

        mTimerWrapper = new TimerWrapper(this,mWorkoutTimeInMillis,mRestTimeInMillis,mRounds,this);

        mTimerWrapper.startTimer();

        isTimerRunning = true;

        cursor.close();
    }

    /**
     * Stops the timer
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
     *  the service to the background
     */
    public void background() {
        Log.v(TAG,"Service background");
        stopForeground(true);
    }

    /**
     * Need to filter intent reopening workout activity
     * Case true show timer, false - not show
     */

    public boolean isUriMatches(){
        boolean isUriMatches = false;
        if (mUriTemp != null && mUriTemp.equals(mUri)) isUriMatches = true;
        return isUriMatches;
    }


}
