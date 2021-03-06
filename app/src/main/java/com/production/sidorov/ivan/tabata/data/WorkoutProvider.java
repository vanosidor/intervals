package com.production.sidorov.ivan.tabata.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Иван on 06.03.2017.
 */

public class WorkoutProvider extends ContentProvider {

    public static final int CODE_WORKOUT = 100;
    public static final int CODE_WORKOUT_WITH_DATE = 101;

    private WorkoutDBHelper mDBHelper;

    private static final UriMatcher sUriMatcher = buildUriMatcher();


    public static UriMatcher buildUriMatcher() {
        UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

        String authority = WorkoutContract.CONTENT_AUTHORITY;

        uriMatcher.addURI(authority, WorkoutContract.PATH_WORKOUT, CODE_WORKOUT);
        uriMatcher.addURI(authority, WorkoutContract.PATH_WORKOUT + "/#", CODE_WORKOUT_WITH_DATE);

        return uriMatcher;
    }

    @Override
    public boolean onCreate() {
        mDBHelper = new WorkoutDBHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        SQLiteDatabase db = mDBHelper.getReadableDatabase();
        Cursor cursor;

        switch (sUriMatcher.match(uri)) {
            case CODE_WORKOUT: {
                cursor = db.query(WorkoutContract.WorkoutEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            }

            case CODE_WORKOUT_WITH_DATE: {

                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                cursor = db.query(WorkoutContract.WorkoutEntry.TABLE_NAME,
                        projection,
                        WorkoutContract.WorkoutEntry.COLUMN_DATE + " = ? ",
                        selectionArguments,
                        null,
                        null,
                        sortOrder);
                break;
            }

            default:
                throw new UnsupportedOperationException("unknown Uri: " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);

        return cursor;
    }

    @Override
    public int bulkInsert(@NonNull Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mDBHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {

            case CODE_WORKOUT:
                db.beginTransaction();
                int rowsInserted = 0;
                try {
                    for (ContentValues value : values) {
                        long weatherDate =
                                value.getAsLong(WorkoutContract.WorkoutEntry.COLUMN_DATE);
                       /* if (!SunshineDateUtils.isDateNormalized(weatherDate)) {
                            throw new IllegalArgumentException("Date must be normalized to insert");
                        }*/

                        long _id = db.insert(WorkoutContract.WorkoutEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            rowsInserted++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                if (rowsInserted > 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }

                return rowsInserted;

            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {

        Uri returnUri;

        final SQLiteDatabase db = mDBHelper.getWritableDatabase();
        switch (sUriMatcher.match(uri)) {
            case CODE_WORKOUT:{
                long id = db.insert(WorkoutContract.WorkoutEntry.TABLE_NAME,null,contentValues);
                if (id>0){
                    returnUri = ContentUris.withAppendedId(WorkoutContract.WorkoutEntry.CONTENT_URI,id);
                }
                else throw new android.database.SQLException("Failed to insert row into " + uri);

                break;
            }
            default:throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri,null);

        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int numRowsDeleted;


        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        switch (sUriMatcher.match(uri)) {
            case CODE_WORKOUT: {
                numRowsDeleted = db.delete(WorkoutContract.WorkoutEntry.TABLE_NAME, selection, selectionArgs);
                break;
            }
            case CODE_WORKOUT_WITH_DATE: {
                /*String idString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{idString};*/

                String normalizedUtcDateString = uri.getLastPathSegment();
                String[] selectionArguments = new String[]{normalizedUtcDateString};

                numRowsDeleted = db.delete(WorkoutContract.WorkoutEntry.TABLE_NAME
                        ,WorkoutContract.WorkoutEntry.COLUMN_DATE + " = ? "
                        ,selectionArguments);
                break;

            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);

        }
        if(numRowsDeleted != 0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return numRowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String s, String[] strings) {

        //get writable database
        SQLiteDatabase db = mDBHelper.getWritableDatabase();

        //Keep track of if an update occurs
        int tasksUpdated;

        // match code
        int match = sUriMatcher.match(uri);

        switch (match) {
            case CODE_WORKOUT_WITH_DATE:
                //update a single task by getting the id
                String normalizedUtcDateString = uri.getPathSegments().get(1);
                //using selections
                tasksUpdated = db.update(WorkoutContract.WorkoutEntry.TABLE_NAME, contentValues, WorkoutContract.WorkoutEntry.COLUMN_DATE + " = ? ", new String[]{normalizedUtcDateString});
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (tasksUpdated != 0) {
            //set notifications if a task was updated
            getContext().getContentResolver().notifyChange(uri, null);
        }

        // return number of tasks updated
        return tasksUpdated;
    }
}
