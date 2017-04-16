package com.production.sidorov.ivan.tabata;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.IBinder;
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
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;

import com.production.sidorov.ivan.tabata.dialog.AddWorkoutDialog;
import com.production.sidorov.ivan.tabata.sync.TimerService;
import com.production.sidorov.ivan.tabata.sync.TimerWrapper;
import com.tubb.smrv.SwipeMenuRecyclerView;

public class MainActivity extends AppCompatActivity implements WorkoutAdapter.WorkoutAdapterOnClickHandler,
        LoaderManager.LoaderCallbacks<Cursor>,
        AddWorkoutDialog.OnFragmentButtonsClickListener {

    public static final String TAG = MainActivity.class.getSimpleName();
    public static final String WORKOUT_DATA = "workout_data";

    private com.tubb.smrv.SwipeMenuRecyclerView mRecyclerView;
    private WorkoutAdapter mWorkoutAdapter;
    private FloatingActionButton mFloatingActionButtonAdd;

    private int mPosition = RecyclerView.NO_POSITION;

    public static final int ID_WORKOUT_LOADER = 33;

    private int mAdapterPosition;

    TimerService mTimerService;

    LinearLayoutManager mLayoutManager;

    long mWorkoutDateTag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "Create");
        setContentView(R.layout.activity_main);

        mRecyclerView = (SwipeMenuRecyclerView) findViewById(R.id.rv_workout);

        mLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);

        mRecyclerView.setLayoutManager(mLayoutManager);

        mRecyclerView.setHasFixedSize(true);

        mWorkoutAdapter = new WorkoutAdapter(this, this);

        mRecyclerView.setAdapter(mWorkoutAdapter);


        mRecyclerView.addItemDecoration(new VerticalSpaceItemDecoration(3));

        mFloatingActionButtonAdd = (FloatingActionButton) findViewById(R.id.fabAddWorkout);


        getSupportLoaderManager().initLoader(ID_WORKOUT_LOADER, null, this);

        /*set green arrow*/
        // mRecyclerView.getLayoutManager().findViewByPosition(1).findViewById(R.id.playImageView).setVisibility(View.VISIBLE);

        /* remove all workouts */
        // getContentResolver().delete(WorkoutContract.WorkoutEntry.CONTENT_URI,null,null);

        /*insert fake data*/
        // FakeDataUtils.insertFakeData(this);
    }


    @Override
    protected void onStart() {
        Log.d(TAG, "OnStart");
        super.onStart();

        Intent i = new Intent(this, TimerService.class);
        bindService(i, mConnection, 0);
        Log.d(TAG, "Bind service");
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(TimerWrapper.BROADCAST_FINISH_ALL));
    }

    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            /*If uri clicked matches with current uri update UI */
            Toast.makeText(context, "Finish workout", Toast.LENGTH_SHORT).show();
            mRecyclerView.findViewWithTag(mWorkoutDateTag).findViewById(R.id.playImageView).setVisibility(View.INVISIBLE);
        }
    };

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        super.unregisterReceiver(receiver);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "OnStop");
        super.onStop();
        unbindService(mConnection);
        Log.d(TAG, "Unbind service");
    }


    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected");

            TimerService.RunServiceBinder binder = (TimerService.RunServiceBinder) service;
            mTimerService = binder.getService();

            Log.d(TAG, "This is timer service: " + mTimerService.toString());

            if (null == mTimerService) return;

            //remove previous visible arrow if exists
            if (mWorkoutDateTag != 0) {
                View contentView = mRecyclerView.findViewWithTag(mWorkoutDateTag);
                if(contentView!=null) contentView.findViewById(R.id.playImageView).setVisibility(View.INVISIBLE);
            }
            //show visible arrow
            if (mTimerService.isTimerRunning()) {
                mWorkoutDateTag = mTimerService.getDate();
                //set green arrow for current workout
                View contentView = mRecyclerView.findViewWithTag(mWorkoutDateTag);
                if(contentView!=null)contentView.findViewById(R.id.playImageView).setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "onServiceDisconnected");
        }
    };


    //onClick listItem handler
    @Override
    public void onListItemClicked(long workoutDate, int adapterPosition) {

        mAdapterPosition = adapterPosition;

        Intent intent = new Intent(this, WorkoutActivity.class);
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

        switch (id) {
            case ID_WORKOUT_LOADER: {
                Uri workoutQueryUri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                String sortOrder = WorkoutContract.WorkoutEntry.COLUMN_DATE + " ASC";
                return new CursorLoader(this, workoutQueryUri, WorkoutDBHelper.WORKOUT_PROJECTION, null, null, sortOrder);
            }
            default:
                throw new RuntimeException("Loader Not Implemented");
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

    public void showWorkoutAddDialog(long date) {

        AddWorkoutDialog addWorkoutDialog = new AddWorkoutDialog();

        Bundle b = new Bundle();
        if (date != 0) {
            b.putLong("Date", date);
            addWorkoutDialog.setArguments(b);
        } else addWorkoutDialog.setArguments(null);

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
    private class VerticalSpaceItemDecoration extends RecyclerView.ItemDecoration {

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
