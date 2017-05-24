package com.production.sidorov.ivan.tabata.dialog;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.databinding.DataBindingUtil;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.codetroopers.betterpickers.hmspicker.HmsPickerBuilder;
import com.codetroopers.betterpickers.hmspicker.HmsPickerDialogFragment;
import com.jakewharton.rxbinding2.widget.RxTextView;
import com.production.sidorov.ivan.tabata.R;
import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;
import com.production.sidorov.ivan.tabata.databinding.AddWorkoutFragmentBinding;

import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;


/**
 * Created by Иван on 21.03.2017.
 */

public class AddWorkoutDialog extends Fragment implements View.OnClickListener, HmsPickerDialogFragment.HmsPickerDialogHandlerV2, SeekBar.OnSeekBarChangeListener{

    AddWorkoutFragmentBinding mBinding;

    public static final String TAG = AddWorkoutDialog.class.getSimpleName();
    private static final int INPUT_WORKOUT_TIME_REFERENCE = 0 ;
    private static final int INPUT_REST_TIME_REFERENCE = 1 ;

    //Date value for edit workout(it's ID)
    long mDate;

    //Edit mode or Create mode value
    private boolean isEdit;

    //Subscriptions for InputWorkout and InputRest fields
    Disposable setWorkoutTimeSubscription;
    Disposable setRestTimeSubscription;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        //Inflate xml layout
        mBinding = DataBindingUtil.inflate(inflater,R.layout.add_workout_fragment,container,false);

        //Get root view
        View rootView = mBinding.getRoot();

        //Get arguments
        Bundle arguments = getArguments();

        mBinding.okButton.setText(R.string.add_dialog_ok);
        mBinding.cancelButton.setText(R.string.add_dialog_cancel);

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

                mBinding.inputWorkoutTimeTextView.setText(workoutTime);
                mBinding.inputRestTimeTextView.setText(restTime);
                mBinding.workoutTitleEditText.setText(name);
                mBinding.numRoundsTextView.setText(String.valueOf(rounds));
            }

        }
        else {
            mBinding.inputWorkoutTimeTextView.setText(R.string.time_default);
            mBinding.inputRestTimeTextView.setText(R.string.time_default);
            mBinding.numRoundsTextView.setText("1");
        }

        mBinding.workoutTitleTextView.setText(R.string.workout_title);
        //mWorkoutTimeTitleTextView.setText(R.string.workout_time_title);
        mBinding.workoutTimeTitleTextView.setText(R.string.workout_time_title);

        mBinding.restTimeTitleTextView.setText(R.string.rest_time_title);
        mBinding.roundsTitleTextView.setText(R.string.rounds_title);


        //Set onClick Listeners
        //mOkButton.setOnClickListener(this);
        mBinding.okButton.setOnClickListener(this);

        mBinding.cancelButton.setOnClickListener(this);
        mBinding.inputWorkoutTimeTextView.setOnClickListener(this);

        //set subscription for input workout time changes
        setWorkoutTimeSubscription = RxTextView.textChanges(mBinding.inputWorkoutTimeTextView).subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(CharSequence charSequence) throws Exception {
                if(charSequence.equals("00:00")) {
                    mBinding.inputWorkoutTimeTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                    mBinding.workoutTimeHintLeft.setVisibility(View.VISIBLE);
                    mBinding.workoutTimeHintLeft.setText(R.string.set_time_hint);
                }
                else {
                    mBinding.inputWorkoutTimeTextView.setTextColor(getResources().getColor(R.color.primary_text));
                    mBinding.workoutTimeHintLeft.setVisibility(View.GONE);
                }
                //Toast.makeText(getContext(), "ADSDSFD", Toast.LENGTH_SHORT).show();
            }
        });

        //set subscription for input rest time changes
        setRestTimeSubscription = RxTextView.textChanges(mBinding.inputRestTimeTextView).subscribe(new Consumer<CharSequence>() {
            @Override
            public void accept(CharSequence charSequence) throws Exception {
                if(charSequence.equals("00:00")) {
                    mBinding.inputRestTimeTextView.setTextColor(getResources().getColor(R.color.colorAccent));
                    mBinding.workoutTimeHintRight.setVisibility(View.VISIBLE);
                    mBinding.workoutTimeHintRight.setText(R.string.set_time_hint);
                }
                else {
                    mBinding.inputRestTimeTextView.setTextColor(getResources().getColor(R.color.primary_text));
                    mBinding.workoutTimeHintRight.setVisibility(View.GONE);
                }
                //Toast.makeText(getContext(), "ADSDSFD", Toast.LENGTH_SHORT).show();
            }
        });



        mBinding.inputRestTimeTextView.setOnClickListener(this);

        //Set onSeekBarChangeListener
        mBinding.roundsSeekBar.setOnSeekBarChangeListener(this);

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

    //Unsubscribe
    @Override
    public void onPause() {
        super.onPause();
        setWorkoutTimeSubscription.dispose();
        setRestTimeSubscription.dispose();
    }

    //Dialog Set callback
    @Override
    public void onDialogHmsSet(int reference, boolean isNegative, int hours, int minutes, int seconds) {
        if(reference==INPUT_WORKOUT_TIME_REFERENCE)
        {
            if(hours>0) mBinding.inputWorkoutTimeTextView.setText(getString(R.string.format_time_with_hours,hours,minutes,seconds));
            else mBinding.inputWorkoutTimeTextView.setText(getString(R.string.format_time,minutes,seconds));
        }
        else
        {
            if(hours>0) mBinding.inputRestTimeTextView.setText(getString(R.string.format_time_with_hours,hours,minutes,seconds));
            else mBinding.inputRestTimeTextView.setText(getString(R.string.format_time,minutes,seconds));
        }
    }


    //SeekBarChangeListener callback
    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        if(mBinding.roundsSeekBar.getProgress() == 0 )
        {
            seekBar.setProgress(1);
        }
        mBinding.numRoundsTextView.setText(String.valueOf(seekBar.getProgress()));
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

                if(mBinding.workoutTitleEditText.getText().toString().trim().length() == 0
                        || mBinding.inputWorkoutTimeTextView.getText().toString().equals(getResources().getString(R.string.time_default))
                        || mBinding.inputRestTimeTextView.getText().toString().equals(getResources().getString(R.string.time_default))) {
                    Toast.makeText(getActivity(),getResources().getString(R.string.toast_enter_data),Toast.LENGTH_SHORT).show();
                    return;
                }


                String title = String.valueOf(mBinding.workoutTitleEditText.getText());
                String workoutTime = String.valueOf(mBinding.inputWorkoutTimeTextView.getText());
                String restTime = String.valueOf(mBinding.inputWorkoutTimeTextView.getText());
                int rounds = mBinding.roundsSeekBar.getProgress();

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
