package com.alon.spyapp.utils.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.alon.spyapp.ActivityDetailsActivity;
import com.alon.spyapp.R;
import com.alon.spyapp.utils.Converter;
import com.alon.spyapp.utils.models.ActivityUtil;
import com.google.gson.Gson;

import java.util.ArrayList;

public class HistoryRecyclerViewAdapter extends RecyclerView.Adapter<HistoryRecyclerViewAdapter.MyViewHolder> {

    private ArrayList<ActivityUtil> mDataset;
    private Converter converter;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class MyViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private ActivityUtil activityUtil;
        private TextView history_TXT_number, history_TXT_activity, history_TXT_duration;
        private LinearLayout history_LYT_image;
        private Context context;

        public MyViewHolder(View v) {
            super(v);
            history_TXT_number = v.findViewById(R.id.history_TXT_number);
            history_TXT_activity = v.findViewById(R.id.history_TXT_activity);
            history_TXT_duration = v.findViewById(R.id.history_TXT_duration);
            history_LYT_image = v.findViewById(R.id.history_LYT_image);
            context = v.getContext();
            v.setOnClickListener(this);

        }

        @Override
        public void onClick(View v) {
            startActivityDetailsActivity();
        }

        // Method that starts the activity details activity.
        private void startActivityDetailsActivity(){
            Intent intent = new Intent(context, ActivityDetailsActivity.class);
            intent.putExtra("type", activityUtil.getType());
            intent.putExtra("startTime", activityUtil.getTimestampStart());
            intent.putExtra("endTime", activityUtil.getTimestampEnd());
            intent.putExtra("duration", activityUtil.getDuration());
            //intent.putExtra("route", activityUtil.getRoute());
            Gson gson = new Gson();
            String routeString = gson.toJson(activityUtil.getRoute());
            intent.putExtra("route", routeString);
            context.startActivity(intent);
        }
    }

    // Constructor.
    public HistoryRecyclerViewAdapter(ArrayList<ActivityUtil> myDataset) {
        mDataset = myDataset;
        converter = Converter.getInstance();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public HistoryRecyclerViewAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_row, parent, false);
        MyViewHolder vh = new MyViewHolder(view);
        return vh;
    }

    // Replace the contents of a view
    @Override
    public void onBindViewHolder(HistoryRecyclerViewAdapter.MyViewHolder holder, int position) {
        // - get element from the dataset at this position
        // - replace the contents of the view with that element
        holder.history_TXT_number.setText(String.valueOf(position + 1));
        holder.history_TXT_activity.setText(converter.convertActivityType(mDataset.get(position).getType()));
        holder.history_TXT_duration.setText(mDataset.get(position).getDuration());
        changeImage(holder, position);
        holder.activityUtil = mDataset.get(position);
    }

    // Method that change the image in each view by the activity type.
    private void changeImage(MyViewHolder holder, int position) {
        switch(mDataset.get(position).getType()){
            case "STILL":
                holder.history_LYT_image.setBackgroundResource(R.drawable.ic_stand);
                break;

            case "WALKING":
                holder.history_LYT_image.setBackgroundResource(R.drawable.ic_walking);
                break;

            case "RUNNING":
                holder.history_LYT_image.setBackgroundResource(R.drawable.ic_running);
                break;

            case "IN_VEHICLE":
                holder.history_LYT_image.setBackgroundResource(R.drawable.ic_driving);
                break;
        }
    }

    // Method that returns the size of the dataset.
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

}
