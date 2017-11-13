package smartenergy.energyapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

// This gets called by the ActivityRecognitionClient style service.
// It stores the confidences, but also checks the current position and speed.
// This service writes to the database.
public class SamplerService extends Service {

    // TODO Nicole: This: http://en.proft.me/2017/07/25/how-recognize-user-activity-activity-recognition-a/

    //this is for the GPS location
    private LocationManager locationManager = null;
    private static final int LOCATION_INTERVAL = 10000; //every min we try to get the location
    private static final float LOCATION_DISTANCE = 0;
    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };


    private DBRawHelper dbRawHelper;

    public SamplerService() {
        dbRawHelper = new DBRawHelper(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate(){
        initializeLocationManager();

        //DEBUG: onCreate gets called

        //now it's getting truly ugly
        try {
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[1]);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, locationListeners[0]);
        }catch (java.lang.SecurityException ex){
            throw new UnsupportedOperationException("Not implemented");
        }catch (IllegalArgumentException e){
            throw new UnsupportedOperationException("Not implemented");

        }

    }

    private void initializeLocationManager(){
        if (locationManager == null){
            locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }



    // Insert the gathered data into the position database, indexed using the current timestamp
    private void samplePosition(double longitude, double latitude, float speed){
        dbRawHelper.insertPosition(
                (int)(System.currentTimeMillis()/1000),
                longitude,
                latitude,
                speed
                );
    }


    private class LocationListener implements android.location.LocationListener{

        Location lastLocation;

        public LocationListener(String provider){
            lastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location){
            lastLocation.set(location);
            Log.d("New location", " "+ lastLocation.getLatitude());

            //sample the output into the db
            samplePosition(lastLocation.getLongitude(), lastLocation.getLatitude(), lastLocation.getSpeed());

        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras){
            //do nothing here not important for us
        }

        @Override
        public void onProviderEnabled(String s) {
            //as above
        }

        @Override
        public void onProviderDisabled(String s) {
            //as above this is an abstract method that needs to be implemented for this interface
        }


    }
}
