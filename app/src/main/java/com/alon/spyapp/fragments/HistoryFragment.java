package com.alon.spyapp.fragments;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alon.spyapp.utils.models.ActivityUtil;
import com.alon.spyapp.utils.adapters.HistoryRecyclerViewAdapter;
import com.alon.spyapp.utils.models.Location;
import com.alon.spyapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;


public class HistoryFragment extends Fragment implements View.OnClickListener {

    private EditText history_EDT_phone, history_EDT_date;
    private Button history_BTN_search;
    private TextView history_TXT_error;
    private DatePickerDialog datePickerDialog;
    private String phone, date, type, startTime, endTime;
    private FirebaseFirestore db;
    private RecyclerView history_RCV_activities;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private ArrayList<ActivityUtil> activitiesArrayList = new ArrayList<>();

    public HistoryFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_history, container, false);
        findAll(view);
        history_EDT_date.setInputType(InputType.TYPE_NULL);
        setClickListeners();
        return view;
    }

    // Method that sets all the click listeners.
    private void setClickListeners() {
        history_EDT_date.setOnClickListener(this);
        history_BTN_search.setOnClickListener(this);
    }

    // Method that finds all the views by id.
    private void findAll(View view) {
        history_EDT_phone = view.findViewById(R.id.history_EDT_phone);
        history_EDT_date = view.findViewById(R.id.history_EDT_date);
        history_BTN_search = view.findViewById(R.id.history_BTN_search);
        history_TXT_error = view.findViewById(R.id.history_TXT_error);
        history_RCV_activities = view.findViewById(R.id.history_RCV_activities);
    }

    // Method that init the recycler view.
    private void initRecyclerView(){
        history_RCV_activities.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this.getContext());
        history_RCV_activities.setLayoutManager(layoutManager);
        mAdapter = new HistoryRecyclerViewAdapter(activitiesArrayList);
        history_RCV_activities.setAdapter(mAdapter);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.history_EDT_date:
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                // date picker dialog
                datePickerDialog = new DatePickerDialog(getContext(), R.style.DatePickerTheme,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                history_EDT_date.setText(convertOneDigitToDoubleDigit(dayOfMonth) + "-" + convertOneDigitToDoubleDigit(monthOfYear + 1) + "-" + year);
                            }
                        } ,year, month, day);
                datePickerDialog.show();
                break;

            case R.id.history_BTN_search:
                history_BTN_search.setClickable(false);
                if(checkInputValidation()) {
                    phone = history_EDT_phone.getText().toString();
                    date = history_EDT_date.getText().toString();
                    getHistoryFromDB(phone, date);
                } else {
                    history_BTN_search.setClickable(true);
                }
                break;
        }
    }

    // Method that gets all data from db by phone and date.
    private void getHistoryFromDB(String phone, final String date) {
        activitiesArrayList.clear();
        Log.d("pttt", "Doing search with phone: " + phone + " on date: " + date);
        db.collection("locations").document(phone).collection("history").document(date).collection("activities").orderBy("timestampStart").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        type = documentSnapshot.getString("type");
                        if(!type.equals("STILL")) {
                            ActivityUtil newActivity = documentSnapshot.toObject(ActivityUtil.class);
                            if(newActivity.getTimestampEnd() != null){
                                newActivity.setDuration(calculateDuration(newActivity.getTimestampStart(), newActivity.getTimestampEnd()));
                            } else {
                                newActivity.setDuration("In Progress");
                            }
                            activitiesArrayList.add(newActivity);
                        } else {
                            ActivityUtil newActivity = new ActivityUtil();
                            startTime = documentSnapshot.getString("timestampStart");
                            endTime = documentSnapshot.getString("timestampEnd");
                            HashMap<String, Object> route = (HashMap)documentSnapshot.get("route");
                            Location newLocation = new Location();
                            if(route.get("lat") != null && route.get("lng") != null){
                                newLocation.setLat(Double.parseDouble(route.get("lat").toString()));
                                newLocation.setLng(Double.parseDouble(route.get("lng").toString()));
                                newActivity.addPointToRoute(newLocation);
                            }
                            newActivity.setType(type);
                            newActivity.setTimestampStart(startTime);
                            newActivity.setTimestampEnd(endTime);
                            if(endTime != null){
                                newActivity.setDuration(calculateDuration(startTime, endTime));
                            } else {
                                newActivity.setDuration("In Progress");
                            }

                            activitiesArrayList.add(newActivity);
                        }
                    }
                    // Remove rows from recycler view from past search.
                    if(history_RCV_activities != null){
                        history_RCV_activities.removeAllViewsInLayout();
                    }
                    // If search find results then init recycler view with them.
                    if(activitiesArrayList.size() != 0){
                        history_TXT_error.setVisibility(View.GONE);
                        initRecyclerView();
                    } else {
                        history_TXT_error.setVisibility(View.VISIBLE);
                    }
                } else {
                    Log.d("pttt", "ERROR");
                }
                history_BTN_search.setClickable(true);

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                history_BTN_search.setClickable(true);
            }
        });
    }

    // Method that converts one digit number to two digits number.
    private String convertOneDigitToDoubleDigit(int num){
        switch(num){
            case 0:
                return "00";

            case 1:
                return "01";

            case 2:
                return "02";

            case 3:
                return "03";

            case 4:
                return "04";

            case 5:
                return "05";

            case 6:
                return "06";

            case 7:
                return "07";

            case 8:
                return "08";

            case 9:
                return "09";

            default:
                return String.valueOf(num);
        }
    }

    // Method that calculates the gap between start time to end time.
    private String calculateDuration(String start, String end){
        String hours, minutes, seconds;
        long startSeconds, endSeconds, durationSeconds;
        String[] startSplit = start.split(":");
        String[] endSplit = end.split(":");
        startSeconds = Long.valueOf(startSplit[2]) + Long.valueOf(startSplit[1]) * 60 + Long.valueOf(startSplit[0]) * 3600;
        endSeconds = Long.valueOf(endSplit[2]) + Long.valueOf(endSplit[1]) * 60 + Long.valueOf(endSplit[0]) * 3600;
        durationSeconds = endSeconds - startSeconds;
        hours = String.valueOf(durationSeconds / 3600);
        durationSeconds -= Integer.valueOf(hours) * 3600;
        minutes = String.valueOf(durationSeconds / 60);
        durationSeconds -= Integer.valueOf(minutes) * 60;
        seconds = String.valueOf(durationSeconds);
        return convertOneDigitToDoubleDigit(Integer.parseInt(hours))
                + ":"
                + convertOneDigitToDoubleDigit(Integer.parseInt(minutes))
                + ":"
                + convertOneDigitToDoubleDigit(Integer.parseInt(seconds));
    }

    // Method that checks input validation.
    private boolean checkInputValidation(){
        if(history_EDT_phone.getText().toString().trim().isEmpty() || history_EDT_date.getText().toString().trim().isEmpty()){
            Toast.makeText(getContext(), "Please fill the form", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}