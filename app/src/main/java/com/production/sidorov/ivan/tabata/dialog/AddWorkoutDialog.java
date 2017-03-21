package com.production.sidorov.ivan.tabata.dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.R;

/**
 * Created by Иван on 21.03.2017.
 */

public class AddWorkoutDialog extends Fragment {

    private Button okButton;
    private Button cancelButton;

    private SeekBar roundsSeekBar;
    private TextInputLayout workoutTitleTextInputLayout;
    private EditText workoutTitleEditText;

    private TextView workoutTitleTextView;
    private TextView workoutTimeTitleTextView;
    private TextView restTimeTitleTextView;
    private TextView inputWorkoutTimeTextView;
    private TextView inputRestTimeTextView;
    private TextView roundsTitleTextView;
    private TextView numRoundsTextView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.add_workout_fragment,container,false);

        okButton = (Button)rootView.findViewById(R.id.okButton);
        cancelButton = (Button)rootView.findViewById(R.id.cancelButton);

        roundsSeekBar = (SeekBar)rootView.findViewById(R.id.roundsSeekBar);
        workoutTitleTextInputLayout = (TextInputLayout)rootView.findViewById(R.id.intervalTitleTextInputLayout);
        workoutTitleEditText = (EditText)rootView.findViewById(R.id.intervalTitleEditText);

        workoutTitleTextView = (TextView)rootView.findViewById(R.id.workoutTitleTextView);
        workoutTimeTitleTextView = (TextView)rootView.findViewById(R.id.workoutTimeTitleTextView);
        restTimeTitleTextView= (TextView)rootView.findViewById(R.id.restTimeTitleTextView);
        roundsTitleTextView = (TextView)rootView.findViewById(R.id.roundsTitleTextView);

        workoutTitleTextView.setText(R.string.workout_title);
        workoutTimeTitleTextView.setText(R.string.workout_time_title);
        restTimeTitleTextView.setText(R.string.rest_time_title);
        roundsTitleTextView.setText(R.string.rounds_title);

        inputWorkoutTimeTextView = (TextView)rootView.findViewById(R.id.inputWorkoutTimeTextView);
        inputRestTimeTextView = (TextView)rootView.findViewById(R.id.inputRestTimeTextView);
        numRoundsTextView = (TextView)rootView.findViewById(R.id.numRoundsTextView);

        return rootView;
    }
}
