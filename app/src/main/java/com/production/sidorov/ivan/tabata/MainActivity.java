package com.production.sidorov.ivan.tabata;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
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
import android.view.View;
import android.widget.Toast;

import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;

import com.production.sidorov.ivan.tabata.dialog.AddWorkoutDialog;

public class MainActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>,
        AddWorkoutDialog.OnFragmentButtonsClickListener{

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String WORKOUT_DATA = "workout_data";

    public static final int ADD_NEW_WORKOUT_MODE = 0;
    public static final int EDIT_WORKOUT_MODE = 1;

    private RecyclerView mRecyclerView;
    private WorkoutAdapter mWorkoutAdapter;
    private FloatingActionButton mFloatingActionButtonAdd;

    private int mPosition = RecyclerView.NO_POSITION;

    public static final int ID_WORKOUT_LOADER = 33;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_workout);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);

        mRecyclerView.setLayoutManager(layoutManager);

        mRecyclerView.setHasFixedSize(true);

        mWorkoutAdapter = new WorkoutAdapter(this,this);

        mRecyclerView.setAdapter(mWorkoutAdapter);

        mFloatingActionButtonAdd = (FloatingActionButton)findViewById(R.id.fabAddWorkout);


        getSupportLoaderManager().initLoader(ID_WORKOUT_LOADER, null, this);

        /* remove all workouts */
        // getContentResolver().delete(WorkoutContract.WorkoutEntry.CONTENT_URI,null,null);

        /*insert fake data*/
        // FakeDataUtils.insertFakeData(this);

        //swipe to delete
        initSwipe();
        
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            // Called when a user swipes left or right on a ViewHolder
            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int swipeDir) {
                long date = (long) viewHolder.itemView.getTag();

                if (swipeDir == ItemTouchHelper.LEFT){


                    String stringDate = Long.toString(date);
                    Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringDate).build();

                    getContentResolver().delete(uri, null, null);

                    //Restart the loader to re-query for all tasks after a deletion
                    getSupportLoaderManager().restartLoader(ID_WORKOUT_LOADER, null, MainActivity.this);

                } else {
                    showWorkoutAddDialog(date);
                }
            }
        }).attachToRecyclerView(mRecyclerView);

    }

    private void initSwipe() {
        ItemTouchHelper.SimpleCallback simpleItemTouchCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                long date = (long) viewHolder.itemView.getTag();

                if (direction == ItemTouchHelper.LEFT){


                    String stringDate = Long.toString(date);
                    Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringDate).build();

                    getContentResolver().delete(uri, null, null);

                    //Restart the loader to re-query for all tasks after a deletion
                    getSupportLoaderManager().restartLoader(ID_WORKOUT_LOADER, null, MainActivity.this);

                } else {
                    showWorkoutAddDialog(date);
                }
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                Paint paint = new Paint();
                Bitmap icon;
                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    float height = (float) itemView.getBottom() - (float) itemView.getTop();
                    float width = height / 3;

                    if(dX > 0){
                        paint.setColor(Color.parseColor("#388E3C"));
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_border_color_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getLeft() + width ,(float) itemView.getTop() + width,(float) itemView.getLeft()+ 2*width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest, paint);
                    } else {
                        paint.setColor(Color.parseColor("#D32F2F"));
                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background, paint);
                        icon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_delete_white_24dp);
                        RectF icon_dest = new RectF((float) itemView.getRight() - 2*width ,(float) itemView.getTop() + width,(float) itemView.getRight() - width,(float)itemView.getBottom() - width);
                        c.drawBitmap(icon,null,icon_dest, paint);
                    }
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }
        };

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleItemTouchCallback);
        itemTouchHelper.attachToRecyclerView(mRecyclerView);
    }

    @Override
    public void OnListItemClicked(long workoutDate) {
        Intent intent = new Intent(this,WorkoutActivity.class);
        Uri uriForDateClicked = WorkoutContract.WorkoutEntry.buildWorkoutUriWithDate(workoutDate);
        intent.setData(uriForDateClicked);
        startActivity(intent);
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
}
