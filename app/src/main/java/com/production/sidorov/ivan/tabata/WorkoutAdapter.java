package com.production.sidorov.ivan.tabata;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.support.v4.util.TimeUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;

import java.util.concurrent.TimeUnit;


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

        String name = mCursor.getString(WorkoutDBHelper.INDEX_WORKOUT_NAME);

        long dateInMillis = mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_DATE);
        long workoutTime = mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_TIME);

        long restTime = mCursor.getLong(WorkoutDBHelper.INDEX_REST_TIME);
        int numRounds = mCursor.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);

        holder.mWorkoutTextView.setText(name);
        holder.mWorkoutTime.setText(Long.toString(dateInMillis));
    }

    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    class WorkoutAdapterViewHolder extends RecyclerView.ViewHolder {

        final TextView mWorkoutTextView;
        final TextView mWorkoutTime;


        WorkoutAdapterViewHolder(View view) {
            super(view);
            mWorkoutTextView = (TextView) view.findViewById(R.id.tv_workout_data);
            mWorkoutTime = (TextView) view.findViewById(R.id.tv_time);
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
