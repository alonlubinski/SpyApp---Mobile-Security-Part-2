package com.alon.spyapp.utils.models;

import java.util.ArrayList;

public class ActivityUtil {

    private String type, duration, timestampStart, timestampEnd;
    private ArrayList<Location> route;

    // Constructor.
    public ActivityUtil() {
        route = new ArrayList<>();
    }


    // Getters and setters.
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTimestampStart() {
        return timestampStart;
    }

    public void setTimestampStart(String timestampStart) {
        this.timestampStart = timestampStart;
    }

    public String getTimestampEnd() {
        return timestampEnd;
    }

    public void setTimestampEnd(String timestampEnd) {
        this.timestampEnd = timestampEnd;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public ArrayList<Location> getRoute() {
        return route;
    }

    public void setRoute(ArrayList<Location> route) {
        this.route = route;
    }

    public void addPointToRoute(Location point){
        this.route.add(point);
    }
}
