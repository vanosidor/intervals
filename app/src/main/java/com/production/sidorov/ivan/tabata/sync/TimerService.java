package com.production.sidorov.ivan.tabata.sync;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.production.sidorov.ivan.tabata.NotificationUtils;

/**
 * Created by Иван on 09.03.2017.
 */

public class TimerService extends Service implements TimerWrapper.OnTimerFinishedHandler{
    private static final String TAG = TimerService.class.getSimpleName();

    private Uri mUri;
    private Uri mUriTemp;

    // Is the service tracking time?
    private boolean isTimerRunning;
    public static final int TICKER_INTERVAL = 1000;

    //test data for testing work
    long workoutTimeInMillis = 5000;
    long restTimeInMillis = 5000;
    int rounds = 2;

    // Service binder
    private final IBinder serviceBinder = new RunServiceBinder();
    private TimerWrapper mTimerWrapper;

    @Override
    public void timerFinished() {
        isTimerRunning = false;

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
        mTimerWrapper = new TimerWrapper(this,workoutTimeInMillis,restTimeInMillis,rounds,this);
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

        mTimerWrapper.startTimer();
        isTimerRunning = true;

        mUri = mUriTemp;
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
