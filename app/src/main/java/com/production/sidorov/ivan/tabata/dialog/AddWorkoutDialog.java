package com.production.sidorov.ivan.tabata.dialog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.production.sidorov.ivan.tabata.R;
import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;

/**
 * Created by Иван on 21.03.2017.
 */

public class AddWorkoutDialog extends Fragment implements View.OnClickListener, HmsPickerDialogFragment.HmsPickerDialogHandlerV2, SeekBar.OnSeekBarChangeListener{

    public static final String TAG = AddWorkoutDialog.class.getSimpleName();
    private static final int INPUT_WORKOUT_TIME_REFERENCE = 0 ;
    private static final int INPUT_REST_TIME_REFERENCE = 1 ;

    private Button mOkButton;
    private Button mCancelButton;

    private SeekBar mRoundsSeekBar;
    private TextInputLayout mWorkoutTitleTextInputLayout;
    private EditText mWorkoutTitleEditText;

    private TextView mWorkoutTitleTextView;
    private TextView mWorkoutTimeTitleTextView;
    private TextView mRestTimeTitleTextView;
    private TextView mInputWorkoutTimeTextView;
    private TextView mInputRestTimeTextView;
    private TextView mRoundsTitleTextView;
    private TextView mNumRoundsTextView;

    long mDate;
    private boolean isEdit;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.add_workout_fragment,container,false);

        Bundle arguments = getArguments();

        mOkButton = (Button)rootView.findViewById(R.id.okButton);
        mCancelButton = (Button)rootView.findViewById(R.id.cancelButton);

        mOkButton.setText(R.string.btn_ok);
        mCancelButton.setText(R.string.btn_cancel);

        mRoundsSeekBar = (SeekBar)rootView.findViewById(R.id.roundsSeekBar);
        mWorkoutTitleTextInputLayout = (TextInputLayout)rootView.findViewById(R.id.intervalTitleTextInputLayout);
        mWorkoutTitleEditText = (EditText)rootView.findViewById(R.id.intervalTitleEditText);

        mWorkoutTitleTextView = (TextView)rootView.findViewById(R.id.workoutTitleTextView);
        mWorkoutTimeTitleTextView = (TextView)rootView.findViewById(R.id.workoutTimeTitleTextView);
        mRestTimeTitleTextView = (TextView)rootView.findViewById(R.id.restTimeTitleTextView);
        mRoundsTitleTextView = (TextView)rootView.findViewById(R.id.roundsTitleTextView);

        mInputWorkoutTimeTextView = (TextView)rootView.findViewById(R.id.inputWorkoutTimeTextView);
        mInputRestTimeTextView = (TextView)rootView.findViewById(R.id.inputRestTimeTextView);
        mNumRoundsTextView = (TextView)rootView.findViewById(R.id.numRoundsTextView);

        //Set text for static views
        if(arguments!=null)
        {
            isEdit = true;

            mDate = arguments.getLong("Date",0);

            String stringDate = Long.toString(mDate);
            Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI;
            uri = uri.buildUpon().appendPath(stringDate).build();

            Cursor c = getActivity().getContentResolver().query(uri, WorkoutDBHelper.WORKOUT_PROJECTION,null,null,null);

            if (c != null && c.moveToFirst()) {

                String name = c.getString(WorkoutDBHelper.INDEX_WORKOUT_NAME);
                String workoutTime = c.getString(WorkoutDBHelper.INDEX_WORKOUT_TIME);
                String restTime = c.getString(WorkoutDBHelper.INDEX_REST_TIME);
                int rounds = c.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);

                mInputWorkoutTimeTextView.setText(workoutTime);
                mInputRestTimeTextView.setText(restTime);
                mWorkoutTitleEditText.setText(name);
                mNumRoundsTextView.setText(String.valueOf(rounds));
            }

        }
        else {
            mInputWorkoutTimeTextView.setText(R.string.time_default);
            mInputRestTimeTextView.setText(R.string.time_default);
            mNumRoundsTextView.setText("1");
        }

        mWorkoutTitleTextView.setText(R.string.workout_title);
        mWorkoutTimeTitleTextView.setText(R.string.workout_time_title);
        mRestTimeTitleTextView.setText(R.string.rest_time_title);
        mRoundsTitleTextView.setText(R.string.rounds_title);


        //Set onClick Listeners
        mOkButton.setOnClickListener(this);
        mCancelButton.setOnClickListener(this);
        mInputWorkoutTimeTextView.setOnClickListener(this);
        mInputRestTimeTextView.setOnClickListener(this);

        //Set onSeekBarChangeListener
        mRoundsSeekBar.setOnSeekBarChangeListener(this);

        return rootView;
    }


    //onAttach to activity
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFragmentButtonsClickListener)context;
        }
        catch (ClassCastException ex){
            throw new ClassCastException(context.toString()+" must implement OnFragmentButtonClickListener");
        }
    }

    //Dialog Set callback
    @Override
    public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
        if(reference==INPUT_WORKOUT_TIME_REFERENCE)
        {
            if(hours>0) mInputWorkoutTimeTextView.setText(getString(R.string.format_time_with_hours,hours,minutes,seconds));
            else mInputWorkoutTimeTextView.setText(getString(R.string.format_time,minutes,seconds));
        }
        else
        {
            if(hours>0) mInputRestTimeTextView.setText(getString(R.string.format_time_with_hours,hours,minutes,seconds));
            else mInputRestTimeTextView.setText(getString(R.string.format_time,minutes,seconds));
        }
    }


    //SeekBarChangeListener callback
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(mRoundsSeekBar.getProgress() == 0 )
        {
            seekBar.setProgress(1);
        }
        mNumRoundsTextView.setText(String.valueOf(seekBar.getProgress()));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }


    //Interface for "OK" and "Cancel" buttons callback
    public interface OnFragmentButtonsClickListener{
        void onButtonCancelClicked();
        void onButtonOkClicked();
    }

    private OnFragmentButtonsClickListener mListener;


    //onClick views method callback
    @Override
    public void onClick(View view) {
        FragmentManager fragmentManager = getFragmentManager();
        switch (view.getId()){

            // Button Cancel clicked
            case R.id.cancelButton:{
               // getFragmentManager().beginTransaction().remove(getFragmentManager().findFragmentById(R.id.fragment_container)).commit();
                fragmentManager.beginTransaction().remove(this).commit();
                mListener.onButtonCancelClicked();
                break;
            }

            //Button Ok clicked
            case R.id.okButton:{

                long date;

                if(mWorkoutTitleEditText.getText().toString().trim().length() == 0
                        || mInputWorkoutTimeTextView.getText().toString().equals(getResources().getString(R.string.time_default))
                        || mInputRestTimeTextView.getText().toString().equals(getResources().getString(R.string.time_default))) {
                    Toast.makeText(getActivity(),"Please, enter workout data!",Toast.LENGTH_SHORT).show();
                    return;
                }


                String title = String.valueOf(mWorkoutTitleEditText.getText());
                String workoutTime = String.valueOf(mInputWorkoutTimeTextView.getText());
                String restTime = String.valueOf(mInputWorkoutTimeTextView.getText());
                int rounds = mRoundsSeekBar.getProgress();

                ContentValues contentValues = new ContentValues();

                contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_NAME,title);
                contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_WORKOUT_TIME, workoutTime);
                contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_REST_TIME, restTime);
                contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_ROUNDS_NUM, rounds);

                //if edit workout
                if (isEdit){
                    date = mDate;

                    String stringDate = Long.toString(date);
                    Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringDate).build();

                    contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_DATE,date);
                    getContext().getContentResolver().update(uri,contentValues,null,null);
                }

                //if create new workout
                else{

                    date = System.currentTimeMillis();
                    contentValues.put(WorkoutContract.WorkoutEntry.COLUMN_DATE,date);
                    getContext().getContentResolver().insert(WorkoutContract.WorkoutEntry.CONTENT_URI,contentValues);
                }

                //hide dialog
                fragmentManager.beginTransaction().remove(this).commit();
                mListener.onButtonOkClicked();
                break;
            }

            // Set Workout time Dialog
            case R.id.inputWorkoutTimeTextView:{
                HmsPickerBuilder hpb = new HmsPickerBuilder()
                        .setFragmentManager(getActivity()
                        .getSupportFragmentManager())
                        .setTargetFragment(this)
                        .setStyleResId(R.style.MyCustomBetterPickerTheme)
                        .setReference(INPUT_WORKOUT_TIME_REFERENCE);
                hpb.show();
                break;
            }

            //Set Rest time Dialog
            case R.id.inputRestTimeTextView:{
                HmsPickerBuilder hpb = new HmsPickerBuilder()
                        .setFragmentManager(getActivity()
                        .getSupportFragmentManager())
                        .setTargetFragment(this)
                        .setStyleResId(R.style.MyCustomBetterPickerTheme)
                        .setReference(INPUT_REST_TIME_REFERENCE);
                hpb.show();
                break;
            }
            default:Log.d(TAG, "onClick:other button clicked in fragment ");
        }
    }

}
