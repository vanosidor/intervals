package com.production.sidorov.ivan.tabata.utilitis;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;

import com.production.sidorov.ivan.tabata.data.WorkoutContract;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by Иван on 07.03.2017.
 */

public class FakeDataUtils {
    private static int [] workoutIDs = {200,300,500,711,900,962};

    /**
     * Creates a single ContentValues object with random workout data for the provided date
     * @param date a normalized date
     * @return ContentValues object filled with random workout data
     */
    private static ContentValues createTestWeatherContentValues(long date) {
        ContentValues testWorkoutValues = new ContentValues();
        testWorkoutValues.put(WorkoutContract.WorkoutEntry.COLUMN_DATE, date);
        testWorkoutValues.put(WorkoutContract.WorkoutEntry.COLUMN_NAME, "TABATA");
        testWorkoutValues.put(WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME, "12:10");
        testWorkoutValues.put(WorkoutContract.WorkoutEntry.COLUMN_REST_TIME, "13:00");
        testWorkoutValues.put(WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM, 3);

        return testWorkoutValues;
    }

    /**
     * Creates random weather data for 7 days starting today
     * @param context
     */
    public static void insertFakeData(Context context) {

        long date = System.currentTimeMillis();
        List<ContentValues> fakeValues = new ArrayList<ContentValues>();

        for(int i=0; i<10; i++) {
            fakeValues.add(FakeDataUtils.createTestWeatherContentValues(date + (long)(Math.random()*25000)));
        }
        // Bulk Insert our new weather data into Sunshine's Database
        context.getContentResolver().bulkInsert(
                WorkoutContract.WorkoutEntry.CONTENT_URI,
                fakeValues.toArray(new ContentValues[7]));
    }
}
