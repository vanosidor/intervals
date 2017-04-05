package com.production.sidorov.ivan.tabata;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;

import com.production.sidorov.ivan.tabata.dialog.AddWorkoutDialog;
import com.tubb.smrv.SwipeMenuRecyclerView;

public class MainActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>,
        AddWorkoutDialog.OnFragmentButtonsClickListener{

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String WORKOUT_DATA = "workout_data";

    private com.tubb.smrv.SwipeMenuRecyclerView mRecyclerView;
    private WorkoutAdapter mWorkoutAdapter;
    private FloatingActionButton mFloatingActionButtonAdd;

    private int mPosition = RecyclerView.NO_POSITION;

    public static final int ID_WORKOUT_LOADER = 33;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (SwipeMenuRecyclerView) findViewById(R.id.rv_workout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mWorkoutAdapter = new WorkoutAdapter(this,this);

        mRecyclerView.setAdapter(mWorkoutAdapter);

        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(3));

        mFloatingActionButtonAdd = (FloatingActionButton)findViewById(R.id.fabAddWorkout);


        getSupportLoaderManager().initLoader(ID_WORKOUT_LOADER, null, this);

        /* remove all workouts */
        // getContentResolver().delete(WorkoutContract.WorkoutEntry.CONTENT_URI,null,null);

        /*insert fake data*/
        // FakeDataUtils.insertFakeData(this);

    }


    //onClick listItem handler
    @Override
    public void onListItemClicked(long workoutDate) {
        Intent intent = new Intent(this,WorkoutActivity.class);
        Uri uriForDateClicked = WorkoutContract.WorkoutEntry.buildWorkoutUriWithDate(workoutDate);
        intent.setData(uriForDateClicked);
        startActivity(intent);
    }

    //OnEdit list item clicked
    @Override
    public void onEditItemClicked(long workoutDate) {
        showWorkoutAddDialog(workoutDate);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        switch (id){
            case ID_WORKOUT_LOADER: {
                Uri workoutQueryUri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                String sortOrder = WorkoutContract.WorkoutEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(this,workoutQueryUri, WorkoutDBHelper.WORKOUT_PROJECTION,null,null,sortOrder);
            }
            default: throw new RuntimeException("Loader Not Implemented");
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mWorkoutAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mWorkoutAdapter.swapCursor(null);
    }

    //onClick Floating Action Button
    public void addWorkout(View view) {
        showWorkoutAddDialog(0);
    }

    public void showWorkoutAddDialog(long date){

        AddWorkoutDialog addWorkoutDialog = new AddWorkoutDialog();

        Bundle b = new Bundle();
        if(date!= 0) {
            b.putLong("Date",date);
            addWorkoutDialog.setArguments(b);
        }
        else addWorkoutDialog.setArguments(null);

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragmentContainerFrameLayout, addWorkoutDialog);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

        mFloatingActionButtonAdd.hide();
        mRecyclerView.setVisibility(View.GONE);
    }

    //On back pressed
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        mRecyclerView.setVisibility(View.VISIBLE);
        mFloatingActionButtonAdd.show();
        }


    //Button cancel clicked in AddWorkoutFragment callback
    @Override
    public void onButtonCancelClicked() {
       mFloatingActionButtonAdd.show();
       mRecyclerView.setVisibility(View.VISIBLE);

    }

    //Button Ok clicked in AddWorkoutFragment callback
    @Override
    public void onButtonOkClicked() {
        mFloatingActionButtonAdd.show();
        mRecyclerView.setVisibility(View.VISIBLE);
    }


    //is it really need?
    private class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration{

        private final int mVerticalSpaceHeight;

        VerticalSpaceItemDecoration(int mVerticalSpaceHeight) {
            this.mVerticalSpaceHeight = mVerticalSpaceHeight;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                                   RecyclerView.State state) {
            outRect.bottom = mVerticalSpaceHeight;
        }

    }

}
