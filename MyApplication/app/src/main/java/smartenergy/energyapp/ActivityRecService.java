package smartenergy.energyapp;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.location.ActivityRecognitionResult;
import com.google.android.gms.location.DetectedActivity;

import java.util.List;


public class ActivityRecService extends Service {

    private DBRawHelper DBRawHelper;

    private static String TAG = "ActivityRecService";

    private SharedPreferences spf;
    private String currentVehicleKey = "CurrentVehicle";

    public ActivityRecService() {
        DBRawHelper = new DBRawHelper(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        spf = this.getSharedPreferences("energyapp", Context.MODE_PRIVATE);

        if (ActivityRecognitionResult.hasResult(intent)) {
            ActivityRecognitionResult result = ActivityRecognitionResult.extractResult(intent);

            String vehicle = spf.getString(currentVehicleKey, new String());
            Log.d(TAG, "onStartCommand: "+vehicle);

            sampleActivity(getVehicle(vehicle), result.getProbableActivities());

            Log.d(TAG, result.toString());
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: is this ever called?");

    }


    // Insert the gathered data into the activity database, indexed using the current timestamp
    private void sampleActivity(Vehicle userDefinedVehicle, List<DetectedActivity> detectedActivities) {
        int vehicleConfidence = 0;
        int bicycleConfidence = 0;
        int footConfidence = 0;
        int runningConfidence = 0;
        int stillConfidence = 0;
        int tiltingConfidence = 0;
        int walkingConfidence = 0;
        int unknownConfidence = 0;

        for (DetectedActivity activity : detectedActivities) {
            switch (activity.getType()) {
                case DetectedActivity.IN_VEHICLE:
                    vehicleConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.ON_BICYCLE:
                    bicycleConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.ON_FOOT:
                    footConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.RUNNING:
                    runningConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.STILL:
                    stillConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.TILTING:
                    tiltingConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.WALKING:
                    walkingConfidence = activity.getConfidence();
                    break;
                case DetectedActivity.UNKNOWN:
                    unknownConfidence = activity.getConfidence();
                    break;
            }
        }
        Log.d(TAG, "Still proba is " + stillConfidence);
        DBRawHelper.insertActivity(
                (int) (System.currentTimeMillis() / 1000),
                userDefinedVehicle.toString(),
                vehicleConfidence,
                bicycleConfidence,
                footConfidence,
                runningConfidence,
                stillConfidence,
                tiltingConfidence,
                walkingConfidence,
                unknownConfidence
        );
    }

    private Vehicle getVehicle(String vehicle){
        Log.d(TAG, "getVehicle: rcv "+ vehicle);
        switch (vehicle){
            case "Car":
                Log.d(TAG, "getVehicle: car");
                return Vehicle.CAR;
            case "Boat":
                Log.d(TAG, "getVehicle: boat");
                return Vehicle.BOAT;
            case "Bus":
                Log.d(TAG, "getVehicle: bus");
                return Vehicle.BUS;
            case "plane":
                Log.d(TAG, "getVehicle: airplane");
                return Vehicle.AIRPLANE;
            case "Tram":
                Log.d(TAG, "getVehicle: tram");
                return Vehicle.TRAMWAY;
            case "Train":
                Log.d(TAG, "getVehicle: train");
                return Vehicle.TRAIN;
            default:
                Log.d(TAG, "getVehicle: default");
                return Vehicle.BUS; //just some value so that we do not have nothing there and are having unexpected behaviour in the db
        }
    }

}
