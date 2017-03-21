package com.production.sidorov.ivan.tabata.sync;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by пользователь on 11.03.2017.
 */

public class TimerWrapper {

    public static final String BROADCAST_WORKOUT_TICK = "workout_tick";
    public static final String BROADCAST_REST_TICK = "rest_tick";
    public static final String BROADCAST_WORKOUT_FINISH= "workout_finish";
    public static final String BROADCAST_REST_FINISH= "rest_finish";

    public static final String INTENT_WORKOUT_EXTRA = "workout_remain_time";
    public static final String INTENT_REST_EXTRA = "reset_remain_time";

    private static final String TAG = TimerWrapper.class.getSimpleName();

    private Context mContext;

    private Ticker mWorkoutTicker;
    private Ticker mRestTicker;

    private long mWorkoutTime;
    private long mRestTime;
    private int mNumOfRounds;

    private int roundCounter;

    private long mFullTimeInMillis;

    public interface OnTimerFinishedHandler{
        void timerFinished();
    }

    private OnTimerFinishedHandler mTimerFinishedHandler;

    TimerWrapper(final Context context, long workoutTime, long restTime, int numOfRounds,OnTimerFinishedHandler timerFinishedHandler) {
        mTimerFinishedHandler = timerFinishedHandler;
        mContext = context;
        mWorkoutTime = workoutTime;
        mRestTime = restTime;
        mNumOfRounds = numOfRounds;

        mFullTimeInMillis = (workoutTime + restTime)*numOfRounds;

        mWorkoutTicker = new Ticker(mWorkoutTime,TimerService.TICKER_INTERVAL) {
            @Override
            public void onTick(long millisRemain) {

                Log.d(TAG, "onTick: "+ millisRemain/1000);

                Intent intent = new Intent(BROADCAST_WORKOUT_TICK);
                intent.putExtra(INTENT_WORKOUT_EXTRA, millisRemain);
                mContext.sendBroadcast(intent);

                mFullTimeInMillis -= 1000;
            }

            @Override
            public void onFinish() {

                Log.d(TAG, "onFinish: Workout timer");
                Intent intent = new Intent(BROADCAST_WORKOUT_FINISH);
                mContext.sendBroadcast(intent);

                mRestTicker.start();
            }
        };

        mRestTicker = new Ticker(mRestTime,TimerService.TICKER_INTERVAL) {
            @Override
            public void onTick(long millisRemain) {

                Log.d(TAG, "onTick: "+ millisRemain/1000);
                Intent intent = new Intent(BROADCAST_REST_TICK);
                intent.putExtra(INTENT_REST_EXTRA, millisRemain);
                mContext.sendBroadcast(intent);

                mFullTimeInMillis -= 1000;
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish: Rest timer");
                Intent intent = new Intent(BROADCAST_REST_FINISH);
                mContext.sendBroadcast(intent);

                roundCounter++;

                if (roundCounter < mNumOfRounds) {
                    mWorkoutTicker.start();
                }
                else{
                    mTimerFinishedHandler.timerFinished();
                }
            }
        };
    }

    void startTimer(){
        mWorkoutTicker.start();
    }

    void stopTimer(){
        roundCounter = 0;
        mWorkoutTicker.cancel();
        mRestTicker.cancel();
    }
}
