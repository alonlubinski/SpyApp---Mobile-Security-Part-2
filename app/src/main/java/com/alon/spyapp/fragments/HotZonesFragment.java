package com.alon.spyapp.fragments;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.alon.spyapp.utils.models.ActivityUtil;
import com.alon.spyapp.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import java.util.ArrayList;
import java.util.List;

public class HotZonesFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    GoogleMap map;
    private ImageButton hot_IMB_walking, hot_IMB_running, hot_IMB_driving;
    private FirebaseFirestore db;
    private ArrayList<ActivityUtil> activityUtilArrayList;
    private ArrayList<LatLng> locationsList;

    public HotZonesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        activityUtilArrayList = new ArrayList<>();
        locationsList = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hot_zones, container, false);
        findAll(view);
        setClickListeners();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.hot_FRG_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    // Method that sets all click listeners.
    private void setClickListeners() {
        hot_IMB_walking.setOnClickListener(this);
        hot_IMB_running.setOnClickListener(this);
        hot_IMB_driving.setOnClickListener(this);
    }

    // Method that finds all the views by id.
    private void findAll(View view) {
        hot_IMB_walking = view.findViewById(R.id.hot_IMB_walking);
        hot_IMB_running = view.findViewById(R.id.hot_IMB_running);
        hot_IMB_driving = view.findViewById(R.id.hot_IMB_driving);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    // Method that adds the heat map to the map.
    private void addHeatMap(GoogleMap googleMap, List<LatLng> list){
        googleMap.clear();
        int[] colors = {
                Color.rgb(102, 225, 0), // green
                Color.rgb(255, 0, 0)    // red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);
        TileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient).radius(40)
                .build();
        googleMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    // Method that get the data from the db.
    private void getDataFromDB(final String activityType, final GoogleMap googleMap){
        activityUtilArrayList.clear();
        locationsList.clear();
        db.collection(activityType).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for(QueryDocumentSnapshot documentSnapshot : task.getResult()){
                        ActivityUtil activityUtil = documentSnapshot.toObject(ActivityUtil.class);
                        activityUtilArrayList.add(activityUtil);
                    }
                    initValuesToLocationsList(activityUtilArrayList, locationsList, googleMap);

                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

            }
        });
    }



    // Method that init values to locations list.
    private void initValuesToLocationsList(ArrayList<ActivityUtil> list, ArrayList<LatLng> locationsList, GoogleMap googleMap){
        for(int i = 0; i < list.size(); i++){
            for(int j = 0; j < list.get(i).getRoute().size(); j++){
                locationsList.add(new LatLng(list.get(i).getRoute().get(j).getLat(), list.get(i).getRoute().get(j).getLng()));
            }
        }
        if(!locationsList.isEmpty()) {
            addHeatMap(googleMap, locationsList);
        } else {
            googleMap.clear();
        }
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.hot_IMB_walking:
                hot_IMB_walking.setImageResource(R.drawable.ic_walking_red);
                hot_IMB_running.setImageResource(R.drawable.ic_running);
                hot_IMB_driving.setImageResource(R.drawable.ic_driving);
                getDataFromDB("WALKING", map);
                break;

            case R.id.hot_IMB_running:
                hot_IMB_walking.setImageResource(R.drawable.ic_walking);
                hot_IMB_running.setImageResource(R.drawable.ic_running_red);
                hot_IMB_driving.setImageResource(R.drawable.ic_driving);
                getDataFromDB("RUNNING", map);
                break;

            case R.id.hot_IMB_driving:
                hot_IMB_walking.setImageResource(R.drawable.ic_walking);
                hot_IMB_running.setImageResource(R.drawable.ic_running);
                hot_IMB_driving.setImageResource(R.drawable.ic_driving_red);
                getDataFromDB("IN_VEHICLE", map);
                break;
        }
    }
}