package com.production.sidorov.ivan.tabata.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Иван on 06.03.2017.
 */

public class WorkoutContract {


    public static final String CONTENT_AUTHORITY = "com.production.sidorov.ivan.tabata";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_WORKOUT = "workout";

    public static final class WorkoutEntry implements BaseColumns{

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_WORKOUT).build();

        public static final String TABLE_NAME = "workout";

        public static final String COLUMN_NAME = "name";

        public static final String COLUMN_DATE= "date";

        public static final String COLUMN_WORKOUT_TIME = "workout_time";

        public static final String COLUMN_REST_TIME= "rest_time";

        public static final String COLUMN_ROUNDS_NUM = "rounds";

        public static Uri buildWorkoutUriWithDate(long date){
            return CONTENT_URI.buildUpon().appendPath(Long.toString(date)).build();
        }
    }




}
