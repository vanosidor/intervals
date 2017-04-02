package com.production.sidorov.ivan.tabata;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;


/**
 * Created by Иван on 06.03.2017.
 */


class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutAdapterViewHolder> {

    private Context mContext;
    private Cursor mCursor;

    public WorkoutAdapter(@NonNull Context context, WorkoutAdapterOnClickHandler handler) {
        mContext = context;
        mWorkoutAdapterClickedHandler = handler;
    }

    public interface WorkoutAdapterOnClickHandler {
        void OnListItemClicked(long workoutDate);
    }

    private WorkoutAdapterOnClickHandler mWorkoutAdapterClickedHandler;

    @Override
    public WorkoutAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.workout_list_item, parent, false);
        v.setFocusable(true);
        return new WorkoutAdapterViewHolder(v);
    }

    @Override
    public void onBindViewHolder(WorkoutAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        int id = mCursor.getInt(WorkoutDBHelper.INDEX_WORKOUT_ID);

        String name = mCursor.getString(WorkoutDBHelper.INDEX_WORKOUT_NAME);

        long workoutDate = mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_DATE);

        String workoutTime = mCursor.getString(WorkoutDBHelper.INDEX_WORKOUT_TIME);
        String restTime = mCursor.getString(WorkoutDBHelper.INDEX_REST_TIME);
        int  numRounds = mCursor.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);

        holder.itemView.setTag(workoutDate);

        holder.mWorkoutTitleTextView.setText(name);
        holder.mWorkoutTimeTextView.setText(workoutTime);
        holder.mRestTimeTextView.setText(restTime);
        holder.mRoundsTextView.setText(String.valueOf(numRounds));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    class WorkoutAdapterViewHolder extends RecyclerView.ViewHolder {

        final TextView mWorkoutTitleTextView;
        final TextView mWorkoutTimeTextView;
        final TextView mRestTimeTextView;
        final TextView mRoundsTextView;

        WorkoutAdapterViewHolder(View view) {
            super(view);

            mWorkoutTitleTextView = (TextView) view.findViewById(R.id.titleTextView);
            mWorkoutTimeTextView = (TextView) view.findViewById(R.id.workoutTimeTextView);
            mRestTimeTextView = (TextView) view.findViewById(R.id.restTimeTextView);
            mRoundsTextView = (TextView) view.findViewById(R.id.roundsTimeTextView);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int adapterPosition = getAdapterPosition();
                    mCursor.moveToPosition(adapterPosition);
                    long workoutDate = mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_DATE);
                    mWorkoutAdapterClickedHandler.OnListItemClicked(workoutDate);
                }
            });
        }
    }

    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }

}
