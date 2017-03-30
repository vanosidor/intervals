package com.production.sidorov.ivan.tabata.utilitis;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 23.03.2017.
 */

public class WorkoutTimeUtils {

    //convert time in milliseconds to String value like "00:00" or "01:00:22"
    public static String timeInMillisToString(long timeInMillis){

        DecimalFormat df = new DecimalFormat("00");

        long hours = TimeUnit.MILLISECONDS.toHours(timeInMillis);
        long temp = timeInMillis;
        temp-=TimeUnit.HOURS.toMillis(hours);

        long minutes = TimeUnit.MILLISECONDS.toMinutes(temp);
        // long temp = millis;
        temp -= TimeUnit.MINUTES.toMillis(minutes);

        long seconds = TimeUnit.MILLISECONDS.toSeconds(temp);

        if(hours==0) {
            return df.format(minutes) + ":" + df.format(seconds);
        }else{
            return df.format(hours)+":" + df.format(minutes)+ ":" + df.format(seconds);
        }
    }

    //get long time from String like "00:00" or "01:55:11"
    public static long getTimeInMillis(String time) {

        String hoursString;
        String minutesString;
        String secondsString;

        int hours;
        int minutes;
        int seconds;

        //with hours
        if (time.length()>5){

            hoursString = time.substring(0,2);
            minutesString = time.substring(3,5);
            secondsString = time.substring(6,8);

            hours = Integer.parseInt(hoursString);
            minutes = Integer.parseInt(minutesString);
            seconds = Integer.parseInt(secondsString);

            return (hours*3600 + minutes*60 + seconds)*1000;

        }
        // without hours
        else {
             minutesString = time.substring(0, 2);
             secondsString = time.substring(3, 5);

             minutes = Integer.parseInt(minutesString);
             seconds = Integer.parseInt(secondsString);

            return (minutes*60+seconds)*1000;
        }
    }
}
