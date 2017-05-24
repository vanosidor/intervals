package com.production.sidorov.ivan.tabata;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.Image;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

    public static final String TAG = WorkoutAdapter.class.getSimpleName();

    private Context mContext;
    private Cursor mCursor;

    public WorkoutAdapter(@NonNull Context context, WorkoutAdapterOnClickHandler handler) {
        mContext = context;
        mWorkoutAdapterClickedHandler = handler;
    }

    //Callback interface for main activity event handlers
    public interface WorkoutAdapterOnClickHandler {
        //method callback for click item
        void onListItemClicked(long workoutDate,int adapterPosition);
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

        holder.setsTitleTextView.setText(mContext.getResources().getQuantityString(R.plurals.plurals_rounds,numRounds,numRounds));
       // holder.playImageView.setVisibility(View.VISIBLE);
    }


    //get Item count
    @Override
    public int getItemCount() {
        if (null == mCursor) {
            return 0;
        }
        return mCursor.getCount();
    }

    @Override
    public void onViewAttachedToWindow(WorkoutAdapterViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    public void showArrowVisible(int position,WorkoutAdapterViewHolder vh){
        if (position==vh.getAdapterPosition())
        {
            vh.playImageView.setVisibility(View.VISIBLE);
        }
    }

    //Workout ViewHolder implementation
    class WorkoutAdapterViewHolder extends RecyclerView.ViewHolder implements  View.OnClickListener {

        final TextView workoutNameTextView;
        final TextView workoutTimeTextView;
        final TextView restTimeTextView;
       // final TextView roundsTextView;

        final TextView setsTitleTextView;
        final TextView workoutTitleTextView;
        final TextView restTitleTextView;

        final ImageView playImageView;

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

            setsTitleTextView = (TextView)view.findViewById(R.id.setsTitleTextView);

            workoutTitleTextView= (TextView)view.findViewById(R.id.workoutListTitleTextView);
            restTitleTextView = (TextView)view.findViewById(R.id.restListTitleTextView);

            playImageView = (ImageView)view.findViewById(R.id.playImageView);

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
                    final Uri uri = WorkoutContract.WorkoutEntry.CONTENT_URI.buildUpon().appendPath(stringDate).build();

                    //create dialog
                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(mContext);
                    dialogBuilder.setMessage(R.string.dialog_delete_message);

                    //button Ok click
                    dialogBuilder.setPositiveButton(R.string.dialog_delete_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            //delete workout item
                            mContext.getContentResolver().delete(uri,null,null);

                            dialog.dismiss();
                        }
                    });

                    //button Cancel click
                    dialogBuilder.setNegativeButton(R.string.dialog_delete_cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

                    //show dialog
                    dialogBuilder.show();

                    break;
                }
                //Content View clicked
                case R.id.smContentView:{

                    mWorkoutAdapterClickedHandler.onListItemClicked(workoutDate,getAdapterPosition());

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
