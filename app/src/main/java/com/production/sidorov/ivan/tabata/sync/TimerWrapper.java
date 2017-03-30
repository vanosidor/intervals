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
    public static final String BROADCAST_FINISH_ALL= "finish_all";

    public static final String INTENT_WORKOUT_EXTRA = "workout_remain_time";
    public static final String INTENT_REST_EXTRA = "reset_remain_time";
    public static final String INTENT_CURRENT_ROUND_NUM_EXTRA = "current_round_num";

    private static final String TAG = TimerWrapper.class.getSimpleName();

    private Context mContext;

    private long mFullTimeInMillis;

    private Ticker mWorkoutTicker;
    private Ticker mRestTicker;

    private long mWorkoutTime;
    private long mRestTime;
    private int mNumOfRounds;

    private int roundCounter;

    public static final int STATE_WORKOUT = 0;
    public static final int STATE_REST = 1;

     interface TimerCallbacks {
        void timerFinished();
        void timerTick(long millisUntilFinished);
        void timerStateChange(int timerState);
    }

    private TimerCallbacks mTimerCallbacksHandler;

    TimerWrapper(final Context context, long workoutTime, long restTime, int numOfRounds,TimerCallbacks timerCallbacksHandler) {

        mTimerCallbacksHandler = timerCallbacksHandler;
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

                mTimerCallbacksHandler.timerTick(millisRemain);

                mFullTimeInMillis -= 1000;
            }

            @Override
            public void onFinish() {

                Log.d(TAG, "onFinish: Workout timer");
                Intent intent = new Intent(BROADCAST_WORKOUT_FINISH);
                mContext.sendBroadcast(intent);

                mRestTicker.start();
                mTimerCallbacksHandler.timerStateChange(STATE_REST);
            }
        };

        mRestTicker = new Ticker(mRestTime,TimerService.TICKER_INTERVAL) {

            @Override
            public void onTick(long millisRemain) {

                Log.d(TAG, "onTick: "+ millisRemain/1000);
                Intent intent = new Intent(BROADCAST_REST_TICK);
                intent.putExtra(INTENT_REST_EXTRA, millisRemain);
                mContext.sendBroadcast(intent);

                mTimerCallbacksHandler.timerTick(millisRemain);

                mFullTimeInMillis -= 1000;
            }

            @Override
            public void onFinish() {
                Log.d(TAG, "onFinish: Rest timer");
                roundCounter++;

                Intent intent = new Intent(BROADCAST_REST_FINISH);
                intent.putExtra(INTENT_CURRENT_ROUND_NUM_EXTRA,roundCounter);
                mContext.sendBroadcast(intent);

                if (roundCounter < mNumOfRounds) {
                    mWorkoutTicker.start();
                    mTimerCallbacksHandler.timerStateChange(STATE_WORKOUT);
                }
                else{
                    mTimerCallbacksHandler.timerFinished();
                    Intent intentFinishAll = new Intent(BROADCAST_FINISH_ALL);
                    mContext.sendBroadcast(intentFinishAll);
                }
            }
        };
    }
    /*
     *start timer
      */
    void startTimer(){
        mWorkoutTicker.start();
        mTimerCallbacksHandler.timerStateChange(STATE_WORKOUT);
    }

    /*
     *stop timer
     */
    void stopTimer(){
        roundCounter = 0;
        mWorkoutTicker.cancel();
        mRestTicker.cancel();
    }
}
