package smartenergy.energyapp;

import android.util.Pair;

import java.sql.Timestamp;


public class Sample {
    Timestamp timestamp;
    MeanOfTransport mostLikelyMeanOfTransport;
    Pair<Double, Double> position;
    double speed;

    public Sample(Timestamp ts, MeanOfTransport transport, double latitude, double longitude, double speed) {
        timestamp = ts;
        mostLikelyMeanOfTransport = transport;
        position = new Pair(latitude, longitude);
        this.speed = speed;
    }

    public double getLatitude(){
        return position.first;
    }

    public double getLongitude(){
        return position.second;
    }
}
