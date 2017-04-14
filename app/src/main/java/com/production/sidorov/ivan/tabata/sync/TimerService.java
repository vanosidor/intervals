package com.production.sidorov.ivan.tabata.sync;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.view.View;

import com.production.sidorov.ivan.tabata.NotificationUtils;
import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.utilitis.WorkoutTimeUtils;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by Иван on 09.03.2017.
 */

public class TimerService extends Service implements TimerWrapper.TimerCallbacks {
    private static final String TAG = TimerService.class.getSimpleName();

    private final String[] mProjection = {
            WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_REST_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM,
            WorkoutContract.WorkoutEntry.COLUMN_DATE,
    };

    public static final int INDEX_CURRENT_WORKOUT_TIME = 0;
    public static final int INDEX_CURRENT_REST_TIME = 1;
    public static final int INDEX_CURRENT_NUM_ROUNDS = 2;
    public static final int INDEX_CURRENT_DATE = 3;

    public static final String BROADCAST_START_TIMER_IN_SERVICE = "start_timer_in_service";
    public static final String BROADCAST_STOP_TIMER_IN_SERVICE = "stop_timer_in_service";

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

    long mDate;

    @Override
    public void onCreate() {
        Log.d(TAG, "Service onCreate");

        isTimerRunning = false;

        Observable<Long> startTimerObservable = createStartTimerObservable();

        startTimerObservable.subscribe(new Consumer<Long>() {
            @Override
            public void accept(Long aLong) throws Exception {
                Log.d(TAG, "accept: " + aLong);
            }
        });


    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service onStartCommand");

        mUriTemp = intent.getData();
        if (mUriTemp == null) throw new NullPointerException("uri is null");
        Log.d(TAG, "mUri: " + mUri);

        String action = intent.getAction();
        if (null != action) {
            if (action.equals(NotificationUtils.ACTION_CANCEL_WORKOUT_NOTIFICATION)) {
                isTimerRunning = false;
                stopTimer();
                stopSelf();
            }
        }
        return Service.START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind");

        return serviceBinder;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy");
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

    public long getMillisUntilFinished() {
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

        Log.d(TAG, "Service Start timer");

        boolean cursorHasValidData = false;

        mUri = mUriTemp;

        //get current workout data from content provider
        Cursor cursor = getContentResolver().query(mUri, mProjection, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {

            cursorHasValidData = true;
        }

        if (!cursorHasValidData) {

            return;
        }

        String workoutTime = cursor.getString(INDEX_CURRENT_WORKOUT_TIME);
        String restTime = cursor.getString(INDEX_CURRENT_REST_TIME);
        int rounds = cursor.getInt(INDEX_CURRENT_NUM_ROUNDS);
        mDate = cursor.getLong(INDEX_CURRENT_DATE);

        mWorkoutTimeInMillis = WorkoutTimeUtils.getTimeInMillis(workoutTime);
        mRestTimeInMillis = WorkoutTimeUtils.getTimeInMillis(restTime);
        mRounds = rounds;

        mTimerWrapper = new TimerWrapper(this, mWorkoutTimeInMillis, mRestTimeInMillis, mRounds, this);
        Log.d(TAG, "Create new TimerWrapper: " + mTimerWrapper.toString());

        mTimerWrapper.startTimer();

        isTimerRunning = true;

       /* Intent intent = new Intent(BROADCAST_START_TIMER_IN_SERVICE);
        intent.putExtra(id);
        sendBroadcast(intent);*/

        cursor.close();
    }

    public long getCurrentWorkoutTimeInMillis() {
        return mWorkoutTimeInMillis;
    }

    public long getCurrentRestTimeInMillis() {
        return mRestTimeInMillis;
    }

    public int getWorkoutState() {
        return mWorkoutState;
    }

    /**
     * Stop the timer
     */
    public void stopTimer() {
        Log.d(TAG, "Service stop timer");
        mTimerWrapper.stopTimer();

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
        Log.d(TAG, "Service foreground");
        startForeground(NotificationUtils.NOTIFICATION_ID, NotificationUtils.showWorkoutNotification(this, uri));
    }

    /**
     * Place the service to the background
     */
    public void background() {
        Log.d(TAG, "Service background");
        stopForeground(true);
    }

    /**
     * @return true if two Uri matches
     */

    public boolean isUriMatches() {
        boolean isUriMatches = false;
        if (mUriTemp != null && mUriTemp.equals(mUri)) isUriMatches = true;
        return isUriMatches;
    }

    public Observable<Long> createStartTimerObservable() {
        return Observable.create(new ObservableOnSubscribe<Long>() {
            @Override
            public void subscribe(final ObservableEmitter<Long> emitter) throws Exception {

                emitter.onNext(mDate);

                emitter.setCancellable(new Cancellable() {
                    @Override
                    public void cancel() throws Exception {
                        mDate = 0;
                    }
                });


            }
        });
    }


}
