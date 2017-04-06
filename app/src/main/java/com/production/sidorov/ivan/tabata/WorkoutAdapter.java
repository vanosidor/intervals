package com.production.sidorov.ivan.tabata;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.production.sidorov.ivan.tabata.data.WorkoutContract;
import com.production.sidorov.ivan.tabata.data.WorkoutDBHelper;
import com.tubb.smrv.SwipeHorizontalMenuLayout;


/**
 * Created by Иван on 06.03.2017.
 */


class WorkoutAdapter extends RecyclerView.Adapter<WorkoutAdapter.WorkoutAdapterViewHolder>  {

    private Context mContext;
    private Cursor mCursor;

    public WorkoutAdapter(@NonNull Context context, WorkoutAdapterOnClickHandler handler) {
        mContext = context;
        mWorkoutAdapterClickedHandler = handler;
    }

    //Callback interface for main activity event handlers
    public interface WorkoutAdapterOnClickHandler {
        //method callback for click item
        void onListItemClicked(long workoutDate);
        //method callback for click edit
        void onEditItemClicked(long workoutDate);
    }

    private WorkoutAdapterOnClickHandler mWorkoutAdapterClickedHandler;


    //On Create ViewHolder
    @Override
    public WorkoutAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(mContext).inflate(R.layout.workout_list_item_swipe, parent, false);
        v.setFocusable(true);
        return new WorkoutAdapterViewHolder(v);
    }

    //On Bind View Holder
    @Override
    public void onBindViewHolder(WorkoutAdapterViewHolder holder, int position) {
        mCursor.moveToPosition(position);

        String name = mCursor.getString(WorkoutDBHelper.INDEX_WORKOUT_NAME);

        long workoutDate = mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_DATE);

        String workoutTime = mCursor.getString(WorkoutDBHelper.INDEX_WORKOUT_TIME);
        String restTime = mCursor.getString(WorkoutDBHelper.INDEX_REST_TIME);
        int  numRounds = mCursor.getInt(WorkoutDBHelper.INDEX_ROUNDS_NUM);

        holder.itemView.setTag(workoutDate);

        holder.workoutNameTextView.setText(name);
        holder.workoutTimeTextView.setText(workoutTime);
        holder.restTimeTextView.setText(restTime);
        holder.roundsTextView.setText(String.valueOf(numRounds));
    }

    //get Item count
    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    //Workout ViewHolder implementation
    class WorkoutAdapterViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener{

        final TextView workoutNameTextView;
        final TextView workoutTimeTextView;
        final TextView restTimeTextView;
        final TextView roundsTextView;

        final TextView setsTitleTextView;
        final TextView workoutTitleTextView;
        final TextView restTitleTextView;


        final View contentView;

        //swipe view views
        final SwipeHorizontalMenuLayout swipeHorizontalMenuLayout;

        final ImageView deleteImageView;
        final ImageButton editImageButton;

        WorkoutAdapterViewHolder(View view) {
            super(view);

            workoutNameTextView = (TextView) view.findViewById(R.id.workoutNameTextView);
            workoutTimeTextView = (TextView) view.findViewById(R.id.workoutTimeTextView);
            restTimeTextView = (TextView) view.findViewById(R.id.restTimeTextView);
            roundsTextView = (TextView) view.findViewById(R.id.roundsTimeTextView);

            setsTitleTextView = (TextView)view.findViewById(R.id.setsTitleTextView);
            workoutTitleTextView= (TextView)view.findViewById(R.id.workoutListTitleTextView);
            restTitleTextView = (TextView)view.findViewById(R.id.restListTitleTextView);

            setsTitleTextView.setText(R.string.sets_title);
            workoutTitleTextView.setText(R.string.workout_list_title);
            restTitleTextView.setText(R.string.rest_list_title);

            contentView = view.findViewById(R.id.smContentView);

            //adding views for swipe view layout
            swipeHorizontalMenuLayout = (SwipeHorizontalMenuLayout)view.findViewById(R.id.sml);

            deleteImageView = (ImageView) itemView.findViewById(R.id.deleteImageView);
            editImageButton = (ImageButton) itemView.findViewById(R.id.editImageButton);

            contentView.setOnClickListener(this);
            deleteImageView.setOnClickListener(this);
            editImageButton.setOnClickListener(this);


        }

        //On click handler
        @Override
        public void onClick(View view) {
            long workoutDate = getWorkoutDate(this);

            switch (view.getId()){

                // Delete clicked
                case R.id.deleteImageView:{

                    swipeHorizontalMenuLayout.smoothCloseMenu(0);

                    String stringDate = Long.toString(workoutDate);
                    Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI;
                    uri = uri.buildUpon().appendPath(stringDate).build();

                    mContext.getContentResolver().delete(uri,null,null);
                    break;
                }
                //Content View clicked
                case R.id.smContentView:{
                    mWorkoutAdapterClickedHandler.onListItemClicked(workoutDate);
                    break;
                }

                //Edit View Clicked
                case R.id.editImageButton:{
                    mWorkoutAdapterClickedHandler.onEditItemClicked(workoutDate);
                    swipeHorizontalMenuLayout.smoothCloseMenu(0);
                    break;
                }
            }
        }

    }


    //get workout date from cursor
    private long getWorkoutDate(RecyclerView.ViewHolder viewHolder){

        int adapterPosition = viewHolder.getAdapterPosition();
        if (mCursor.moveToPosition(adapterPosition)){
            return mCursor.getLong(WorkoutDBHelper.INDEX_WORKOUT_DATE);
        }
        else return 0L;

    }

    //Swap cursor
    void swapCursor(Cursor newCursor) {
        mCursor = newCursor;
        notifyDataSetChanged();
    }


}
