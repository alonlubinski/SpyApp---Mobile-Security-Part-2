package com.alon.spyapp.fragments;

import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.alon.spyapp.utils.Converter;
import com.alon.spyapp.utils.models.Person;
import com.alon.spyapp.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;

import java.io.IOException;
import java.util.List;
import java.util.Locale;


public class LiveLocationFragment extends Fragment implements OnMapReadyCallback, View.OnClickListener {

    GoogleMap map;
    private EditText live_EDT_phone;
    private Button live_BTN_spy;
    private TextView live_TXT_name, live_TXT_phone, live_TXT_location, live_TXT_activity;
    private FirebaseFirestore db;
    private ListenerRegistration registration;
    private Person newPerson;
    private Double lat, lng;
    private String address, activity;
    private Converter converter;

    public LiveLocationFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        db = FirebaseFirestore.getInstance();
        newPerson = new Person();
        converter = Converter.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live_location, container, false);
        findAll(view);
        setClickListeners();
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.live_FRG_map);
        mapFragment.getMapAsync(this);
        return view;
    }

    // Method that sets click listeners.
    private void setClickListeners() {
        live_BTN_spy.setOnClickListener(this);
    }

    // Method that finds all the view by id.
    private void findAll(View view) {
        live_EDT_phone = view.findViewById(R.id.live_EDT_phone);
        live_BTN_spy = view.findViewById(R.id.live_BTN_spy);
        live_TXT_name = view.findViewById(R.id.live_TXT_name);
        live_TXT_phone = view.findViewById(R.id.live_TXT_phone);
        live_TXT_location = view.findViewById(R.id.live_TXT_location);
        live_TXT_activity = view.findViewById(R.id.live_TXT_activity);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.live_BTN_spy:
                String phone = live_EDT_phone.getText().toString();
                getDetailsFromDB(phone);
                break;
        }
    }

    // Method that gets all the details from db by phone number.
    private void getDetailsFromDB(final String phone){
        if(registration != null){
            registration.remove();
        }
        db.collection("locations").document(phone).collection("data").document("details").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getData() != null){
                        newPerson.setName((String)task.getResult().getData().get("name"));
                        newPerson.setPhone((String)task.getResult().getData().get("phone"));
                        live_TXT_name.setText(newPerson.getName());
                        live_TXT_phone.setText(newPerson.getPhone());
                        getLiveDataFromDB(phone);
                    } else {
                        Toast.makeText(getContext(), "Phone doesn't exist.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error! Please try again.", Toast.LENGTH_LONG).show();
            }
        });
    }

    // Method that gets the live data from db and register snapshot listener.
    private void getLiveDataFromDB(String phone){
        DocumentReference documentReference = db.collection("locations").document(phone).collection("data").document("live");
        documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    if(task.getResult().getData() != null){
                        lat = (Double)task.getResult().getData().get("lat");
                        lng = (Double)task.getResult().getData().get("lng");
                        activity = converter.convertActivityType(task.getResult().getData().get("activity").toString());
                        newPerson.setLat(lat);
                        newPerson.setLng(lng);
                        initPersonToTheMap(newPerson);
                        convertToAddress(lat, lng);
                        live_TXT_activity.setText(activity);
                    } else {
                        Toast.makeText(getContext(), "Phone doesn't exist.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getContext(), "Error! Please try again.", Toast.LENGTH_LONG).show();
            }
        });

        registration = documentReference.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                Log.d("pttt", "Coord changed!");
                lat = (Double)documentSnapshot.getData().get("lat");
                lng = (Double)documentSnapshot.getData().get("lng");
                activity = converter.convertActivityType(documentSnapshot.getData().get("activity").toString());
                newPerson.setLat(lat);
                newPerson.setLng(lng);
                live_TXT_activity.setText(activity);
                initPersonToTheMap(newPerson);
                convertToAddress(lat, lng);
            }
        });
    }

    // Method that init marker to user's location.
    private void initPersonToTheMap(Person person){
        map.clear();
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(person.getLat(), person.getLng()), 15));
        map.addMarker(new MarkerOptions().position(new LatLng(person.getLat(), person.getLng())).snippet(person.getName()).title(person.getPhone()));

    }

    // Method that converts location to physical address.
    private void convertToAddress(double lat, double lng){
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        try{
            List<Address> addressList = geocoder.getFromLocation(lat, lng, 1);
            if(!addressList.isEmpty()){
                address = addressList.get(0).getAddressLine(0);
            } else {
                address = "Address Not Available";
            }
            live_TXT_location.setText(address);
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        // Remove snapshot listener.
        registration.remove();
    }

}