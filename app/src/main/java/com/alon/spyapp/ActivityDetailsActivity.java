package com.alon.spyapp;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.alon.spyapp.utils.Converter;
import com.alon.spyapp.utils.models.Location;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

public class ActivityDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;
    private TextView details_TXT_activity, details_TXT_duration, details_TXT_start, details_TXT_end;
    private String type, duration, startTime, endTime;
    private ArrayList<Location> route;
    private Converter converter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        findAll();
        converter = Converter.getInstance();
        if(getIntent().getExtras() != null){
            type = getIntent().getStringExtra("type");
            duration = getIntent().getStringExtra("duration");
            startTime = getIntent().getStringExtra("startTime");
            endTime = getIntent().getStringExtra("endTime");
            initDetails();
            String routeAsString = getIntent().getStringExtra("route");
            convertRouteToArrayList(routeAsString);
        }
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.details_FRG_map);
        supportMapFragment.getMapAsync(this);
    }

    private void convertRouteToArrayList(String routeAsString) {
        Gson gson = new Gson();
        Type type = new TypeToken<ArrayList<Location>>(){}.getType();
        route = gson.fromJson(routeAsString, type);
    }

    // Method that init the details to the ui.
    private void initDetails() {
        details_TXT_activity.setText(converter.convertActivityType(type));
        details_TXT_duration.setText(duration);
        details_TXT_start.setText(startTime);
        details_TXT_end.setText(endTime);
    }

    // Method that finds all the views by id.
    private void findAll() {
        details_TXT_activity = findViewById(R.id.details_TXT_activity);
        details_TXT_duration = findViewById(R.id.detail_TXT_duration);
        details_TXT_start = findViewById(R.id.details_TXT_start);
        details_TXT_end = findViewById(R.id.details_TXT_end);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if(route.size() > 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(route.get(0).getLat(), route.get(0).getLng()), 15));
        }
        for(int i = 0; i < route.size(); i++){
            if(i == 0 && route.size() > 1){
                // If there are more then 1 location points in route.
                map.addMarker(new MarkerOptions().position(new LatLng(route.get(i).getLat(), route.get(i).getLng())).snippet(startTime).title("Start Location"));
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(route.get(i).getLat(), route.get(i).getLng()), new LatLng(route.get(i+1).getLat(), route.get(i+1).getLng()))
                        .width(5)
                        .color(Color.RED));
            } else if(i == 0 && i == route.size() - 1) {
                // If there is only one location point in route.
                map.addMarker(new MarkerOptions().position(new LatLng(route.get(i).getLat(), route.get(i).getLng())));
            } else if(i == route.size() - 1){
                // If this is the last location point in route.
                map.addMarker(new MarkerOptions().position(new LatLng(route.get(i).getLat(), route.get(i).getLng())).snippet(endTime).title("End Location"));
            } else {
                // If this is a middle point somewhere in route.
                map.addPolyline(new PolylineOptions()
                        .add(new LatLng(route.get(i).getLat(), route.get(i).getLng()), new LatLng(route.get(i+1).getLat(), route.get(i+1).getLng()))
                        .width(5)
                        .color(Color.RED));
            }

        }
    }
}