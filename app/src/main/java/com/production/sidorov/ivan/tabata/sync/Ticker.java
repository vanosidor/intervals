package com.production.sidorov.ivan.tabata.sync;

import android.os.Handler;
import android.os.Message;

import java.lang.ref.WeakReference;

/**
 * Created by пользователь on 11.03.2017.
 */

public abstract class Ticker {

    private static final String TAG = Ticker.class.getSimpleName();

    private long startTime;
    private long pauseTime;
    private long timeInPause;

    private long remainTime;
    private long delay;

    private long millisInFuture;

    private long countdownInterval;

    private boolean canceled = false;
    private boolean paused = false;

    private static final int MSG = 1;

    public abstract void onTick(long millisRemain);
    public abstract void onFinish();

    private final Handler mUpdateTickerHandler = new TickerHandler(this);

    Ticker(long millisInFuture,long countdownInterval) {
        this.millisInFuture = millisInFuture;
        this.countdownInterval = countdownInterval;
    }

    void cancel()
    {
        mUpdateTickerHandler.removeMessages(MSG);
        canceled = true;
        paused=false;
        startTime = 0;
        timeInPause = 0;
        remainTime = 0;
    }

    /*
    * Start ticker function. May be call @synchronized!
    */
    final void start()
    {
        startTime = System.currentTimeMillis();
        remainTime = millisInFuture;
        mUpdateTickerHandler.sendEmptyMessage(MSG);
        canceled = false;
        paused = false;
    }

    public synchronized void pause()
    {
        paused =true;

        mUpdateTickerHandler.sendEmptyMessage(MSG);
        pauseTime = System.currentTimeMillis();

        long elapsedTime = pauseTime - startTime - timeInPause;

        delay = millisInFuture-elapsedTime-millisInFuture;

    }

    public synchronized void resume()
    {
        timeInPause = timeInPause +System.currentTimeMillis()-pauseTime;

        paused = false;

        mUpdateTickerHandler.sendEmptyMessageDelayed(MSG,delay);
    }


    private static class TickerHandler extends Handler{

        private final WeakReference<Ticker> ticker;

        TickerHandler(Ticker ticker) {
            this.ticker = new WeakReference<>(ticker);
        }
        @Override
        public void handleMessage(Message msg) {
            if (MSG == msg.what) {
                ticker.get().updateTicker(this);
            }
        }
    }

    private void updateTicker(Handler handler){
        if(!paused)
        {
            if (remainTime>=0) {
                onTick(remainTime);

                if (!canceled) {
                    handler.sendEmptyMessageDelayed(MSG, countdownInterval);
                }
                remainTime-=countdownInterval;
            }
            else
            {
                onFinish();
            }
        }
    }
}
