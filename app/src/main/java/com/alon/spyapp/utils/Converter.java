package com.alon.spyapp.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class Converter {

    private static Converter instance;


    public static Converter getInstance(){
        return instance;
    }

    // Constructor.
    private Converter(){
    }

    public static Converter initHelper(){
        if(instance == null){
            instance = new Converter();
        }
        return instance;
    }

    // Method that converts the activity type string to fixed string.
    public static String convertActivityType(String type){
        switch(type){
            case "STILL":
                return "Standing";

            case "WALKING":
                return "Walking";

            case "RUNNING":
                return "Running";

            case "IN_VEHICLE":
                return "In Vehicle";

            default:
                return "Unknown";
        }
    }
}
