package com.production.sidorov.ivan.tabata.utilitis;

/**
 * Created by Иван on 23.03.2017.
 */

public class TimeUtils {

    //get time from String like "00:00"
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
