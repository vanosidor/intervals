package com.production.sidorov.ivan.tabata.sync;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.production.sidorov.ivan.tabata.R;

/**
 * Created by пользователь on 11.03.2017.
 */

public class TimerWrapper implements SharedPreferences.OnSharedPreferenceChangeListener {

   // public static final String BROADCAST_START_TIMER = "start_timer";
    public static final String BROADCAST_WORKOUT_TICK = "workout_tick";
    public static final String BROADCAST_REST_TICK = "rest_tick";
    public static final String BROADCAST_WORKOUT_FINISH= "workout_finish";
    public static final String BROADCAST_REST_FINISH= "rest_finish";
    public static final String BROADCAST_FINISH_ALL= "finish_all";
    public static final String BROADCAST_STOP_TIMER= "stop_timer";

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

    /*
    * OnShared preferences Changed Listener
    * */
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (key.equals(mContext.getString(R.string.pref_play_round_sound_key))) {
            mIsSoundOn = getAudioPreference();
        }
    }

    interface TimerCallbacks {
        void timerFinished();
        void timerTick(long millisUntilFinished);
        void timerStateChange(int timerState);
    }

    private TimerCallbacks mTimerCallbacksHandler;

    private SharedPreferences mSharedPreferences;
    private boolean mIsSoundOn;
    private MediaPlayer mediaPlayerWorkout;
    private MediaPlayer mediaPlayerRest;

    TimerWrapper(final Context context, long workoutTime, long restTime, int numOfRounds,TimerCallbacks timerCallbacksHandler) {

        mTimerCallbacksHandler = timerCallbacksHandler;
        mContext = context;
        mWorkoutTime = workoutTime;
        mRestTime = restTime;
        mNumOfRounds = numOfRounds;

        mFullTimeInMillis = (workoutTime + restTime)*numOfRounds;

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        mIsSoundOn = getAudioPreference();

        mediaPlayerWorkout = MediaPlayer.create(mContext, R.raw.boxing_bell_ring);
        mediaPlayerRest = MediaPlayer.create(mContext, R.raw.bell_ring);

        mWorkoutTicker = new Ticker(mWorkoutTime,TimerService.TICKER_INTERVAL) {
            @Override
            public void onTick(long millisRemain) {

               // Log.d(TAG, "onTick: "+ millisRemain/1000);

                Intent intent = new Intent(BROADCAST_WORKOUT_TICK);

                intent.putExtra(INTENT_WORKOUT_EXTRA, millisRemain);
                mContext.sendBroadcast(intent);

                mTimerCallbacksHandler.timerTick(millisRemain);

                mFullTimeInMillis -= 1000;
            }

            @Override
            public void onFinish() {

                //start playing sound when round ends
                if(mIsSoundOn)mediaPlayerWorkout.start();

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

               // Log.d(TAG, "onTick: "+ millisRemain/1000);
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

                //start playing sound when round ends
                if(mIsSoundOn)mediaPlayerRest.start();

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
        //register on shared preferences change listener
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

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


        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);

        if (mediaPlayerWorkout != null) {
            mediaPlayerWorkout.reset();
            mediaPlayerWorkout.release();
        }
        if (mediaPlayerRest != null) {
            mediaPlayerRest.reset();
            mediaPlayerRest.release();
        }

        //may be remove???WTF!!!why i did it???
        //need to be send main activity to remove green arrow when timer stopped or canceled
         Intent intentStopTimer = new Intent(BROADCAST_STOP_TIMER);
         mContext.sendBroadcast(intentStopTimer);
    }

    private boolean getAudioPreference(){

        return mSharedPreferences.getBoolean(mContext.getString(R.string.pref_play_round_sound_key),mContext.getResources().getBoolean(R.bool.pref_play_sounds_default));
    }
}
