package com.production.sidorov.ivan.tabata.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Иван on 06.03.2017.
 */

public class WorkoutDBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "workout.db";

    public static final int DATABASE_VERSION = 2;

    public WorkoutDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public static final String[] WORKOUT_PROJECTION = {
            WorkoutContract.WorkoutEntry._ID,
            WorkoutContract.WorkoutEntry.COLUMN_NAME,
            WorkoutContract.WorkoutEntry.COLUMN_DATE,
            WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_REST_TIME,
            WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM,
    };

    public static final int INDEX_WORKOUT_ID = 0;
    public static final int INDEX_WORKOUT_NAME = 1;
    public static final int INDEX_WORKOUT_DATE= 2;
    public static final int INDEX_WORKOUT_TIME= 3;
    public static final int INDEX_REST_TIME= 4;
    public static final int INDEX_ROUNDS_NUM= 5;



    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE "+ WorkoutContract.WorkoutEntry.TABLE_NAME + "(" +
                WorkoutContract.WorkoutEntry._ID  + " INTEGER PRIMARY KEY AUTOINCREMENT, " +

                WorkoutContract.WorkoutEntry.COLUMN_NAME  + " STRING NOT NULL, " +

                WorkoutContract.WorkoutEntry.COLUMN_DATE  + " INTEGER NOT NULL, " +

                WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME + " INTEGER NOT NULL, " +

                WorkoutContract.WorkoutEntry.COLUMN_REST_TIME + " INTEGER NOT NULL, " +

                WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM + " INTEGER NOT NULL, " +

                " UNIQUE (" + WorkoutContract.WorkoutEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        sqLiteDatabase.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ WorkoutContract.WorkoutEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);
    }
}
