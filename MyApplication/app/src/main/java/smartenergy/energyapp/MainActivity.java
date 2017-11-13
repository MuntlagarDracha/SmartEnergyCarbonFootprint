package smartenergy.energyapp;

import android.app.ActivityManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.ActivityRecognition;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static String TAG = "MainActivity";
    private String currentVehicleKey = "CurrentVehicle";

    Button btnMain, btnGlobe, btnCalender;
    ToggleButton tBtnAir, tBtnBoat, tBtnCar, tBtnBus, tBtnTram, tBtnTrain;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;

    ProgressBar progressBar;
    TextView tvPercent, tvPercentInfo;

    //i'm utterly sorry for this
    TextView tvDisCar, tvDisWalk, tvDisCycle, tvDisTram, tvDisTrain, tvDisbus, tvDisBoat, tvDisAir,
            tvEnergyWalk, tvEnergyCycle, tvEnergyTram, tvEnergyTrain, tvEnergybus, tvEnergyCar, tvEnergyBoat, tvEnergyAir,
            tvCO2Walk, tvCO2Cycle, tvCO2Tram, tvCO2Train, tvCO2bus, tvCO2Car, tvCO2Boat, tvCO2Air;
    TextView tvDis[] = new TextView[8];
    TextView tvCO2[] = new TextView[8];
    TextView tvEnergy[] = new TextView[8];

    private DBRawHelper dbRawHelper;
    private DBProcessedHelper dbProcessedHelper;
    private DBAggregatedHelper dbAggregatedHelper;
    private Analyzer analyzer;

    public GoogleApiClient apiClient;
    SharedPreferences spf; //this is for storing the next vehicle, and the first run if we will implement that

    int TIMER = 60000; // 1min


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spf = this.getSharedPreferences("energyapp", Context.MODE_PRIVATE);
        String isThereAnything = spf.getString(currentVehicleKey, new String());
        if (isThereAnything == ""){
            Log.d(TAG, "onCreate: no vehicle in shared preferences");
            //we started the app for the first time and now vehicle is set so we simply set it to car just so it is not empty when the service tries to read it
            spf.edit().putString(currentVehicleKey, "car").commit(); //this thing is synchronous and is called so this is not empty when the service tries to read from it
        }

        if (spf.getBoolean("firstrun", true)) {
            // ToDo first run stuff here then set 'firstrun' as false

            spf.edit().putBoolean("firstrun", false).apply();
        }

        //DB stuff so we get the infos to display
        dbRawHelper = new DBRawHelper(this);
        dbProcessedHelper = new DBProcessedHelper(this);
        dbAggregatedHelper = new DBAggregatedHelper(this);
        analyzer = new Analyzer(dbRawHelper, dbProcessedHelper, dbAggregatedHelper);

        //Bottom three buttons
        btnMain = (Button) findViewById(R.id.btnMiddle);
        btnCalender = (Button) findViewById(R.id.btnLeft);
        btnGlobe = (Button) findViewById(R.id.btnRight);
        initBtnButtom(); //this sets the onClickListeners for the buttons on the bottom

        //next mode of transport part
        tBtnAir = (ToggleButton) findViewById(R.id.tBtnAir);
        tBtnBoat = (ToggleButton) findViewById(R.id.tBtnBoat);
        tBtnCar = (ToggleButton) findViewById(R.id.tBtnCar);
        tBtnBus = (ToggleButton) findViewById(R.id.tBtnBus);
        tBtnTram = (ToggleButton) findViewById(R.id.tBtnTram);
        tBtnTrain = (ToggleButton) findViewById(R.id.tBtnTrain);
        initBtnVehicle(); //sets the onClickListeners and the logic involved with those buttons

        //the progressbar part
        progressBar = (ProgressBar) findViewById(R.id.circle_progress_bar);
        tvPercent = (TextView) findViewById(R.id.tvPercent);
        tvPercentInfo = (TextView) findViewById(R.id.tvPercentInfo);
        initProgessBar(); //get value for the progressbar and the percentage textview

        //the table part
        tvCO2Air = (TextView) findViewById(R.id.tvCO2Air);
        tvCO2Boat = (TextView) findViewById(R.id.tvCO2Boat);
        tvCO2Car = (TextView) findViewById(R.id.tvCO2Car);
        tvCO2Cycle = (TextView) findViewById(R.id.tvCO2Cycle);
        tvCO2Train = (TextView) findViewById(R.id.tvCO2Train);
        tvCO2Tram = (TextView) findViewById(R.id.tvCO2Tram);
        tvCO2bus = (TextView) findViewById(R.id.tvCO2Bus);
        tvCO2Walk = (TextView) findViewById(R.id.tvCO2Walk);

        tvEnergyAir = (TextView) findViewById(R.id.tvEnergyAir);
        tvEnergyBoat = (TextView) findViewById(R.id.tvEnergyBoat);
        tvEnergybus = (TextView) findViewById(R.id.tvEnergyBus);
        tvEnergyCar = (TextView) findViewById(R.id.tvEnergyCar);
        tvEnergyCycle = (TextView) findViewById(R.id.tvEnergyCycle);
        tvEnergyTrain = (TextView) findViewById(R.id.tvEnergyTrain);
        tvEnergyTram = (TextView) findViewById(R.id.tvEnergyTram);
        tvEnergyWalk = (TextView) findViewById(R.id.tvEnergyWalk);

        tvDisAir = (TextView) findViewById(R.id.tvDisAir);
        tvDisBoat = (TextView) findViewById(R.id.tvDisBoat);
        tvDisbus = (TextView) findViewById(R.id.tvDisBus);
        tvDisCar = (TextView) findViewById(R.id.tvDisCar);
        tvDisCycle = (TextView) findViewById(R.id.tvDisCycle);
        tvDisTrain = (TextView) findViewById(R.id.tvDisTrain);
        tvDisTram = (TextView) findViewById(R.id.tvDisTram);
        tvDisWalk = (TextView) findViewById(R.id.tvDisWalk);

        tvDis[0] = tvDisCar;
        tvDis[1] = tvDisTram;
        tvDis[2] = tvDisTrain;
        tvDis[3] = tvDisbus;
        tvDis[4] = tvDisBoat;
        tvDis[5] = tvDisAir;
        tvDis[6] = tvDisWalk;
        tvDis[7] = tvDisCycle;

        tvEnergy[0] = tvEnergyCar;
        tvEnergy[1] = tvEnergyTram;
        tvEnergy[2] = tvEnergyTrain;
        tvEnergy[3] = tvEnergybus;
        tvEnergy[4] = tvEnergyBoat;
        tvEnergy[5] = tvEnergyAir;
        tvEnergy[6] = tvEnergyWalk;
        tvEnergy[7] = tvEnergyCycle;

        tvCO2[0] = tvCO2Car;
        tvCO2[1] = tvCO2Tram;
        tvCO2[2] = tvCO2Train;
        tvCO2[3] = tvCO2bus;
        tvCO2[4] = tvCO2Boat;
        tvCO2[5] = tvCO2Air;
        tvCO2[6] = tvCO2Walk;
        tvCO2[7] = tvCO2Cycle;
        initTable(); //fill the table


        //start other non GUI stuff that we need for location and ActivityRecognition
        apiClient = new GoogleApiClient.Builder(this)
                .addApi(ActivityRecognition.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        apiClient.connect();

        //check Play Services is active
        checkGooglePlayServicesAvailable(this);

        //ask for permissions for the location
        askForPermission();

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //in case we do not have the permission
            Log.d(TAG, "onCreate: we do not have permission");
            return;
        }

        //let's start the service
        Intent intentLocation = new Intent(this, SamplerService.class);
        Intent intentActivityRec = new Intent(this, ActivityRecService.class);
        Intent intentRefresh = new Intent(this, RefreshDbService.class);
        this.startService(intentActivityRec);
        Log.d(TAG, "onCreate: started ActivityRecService");
        this.startService(intentLocation);
        Log.d(TAG, "onCreate: started location service");
        this.startService(intentRefresh);
        Log.d(TAG, "onCreate: started DB Refresh service");


    }

    @Override
    protected void onResume() {

        //update progress bar
        initProgessBar();

        //update the table
        initTable();

        //check the buttons
        resumeBtnVehicle();

        //check if the services are still running and restart them if needed
        if (!isMyServiceRunning(SamplerService.class)){
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //in case we do not have the permission
                Log.d(TAG, "onCreate: we do not have permission");
            }else {
                //start it again
                Intent intentLocation = new Intent(this, SamplerService.class);
                this.startService(intentLocation);
            }

        }
        if (!isMyServiceRunning(ActivityRecService.class)){
            //start the service again
            Intent intentActivityRec = new Intent(this, ActivityRecService.class);
            this.startService(intentActivityRec);

        }
        if (!isMyServiceRunning(RefreshDbService.class)){
            //star the service again
            Intent intentRefresh = new Intent(this, RefreshDbService.class);
            this.startService(intentRefresh);

        }

        super.onResume();
    }

    private boolean checkGooglePlayServicesAvailable(MainActivity mainActivity) {
        int PLAY_SERVICE_RESOLUTION_REQUEST = 9000;
        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int result = googleAPI.isGooglePlayServicesAvailable(mainActivity);
        if (result != ConnectionResult.SUCCESS) {
            if (googleAPI.isUserResolvableError(result)) {
                googleAPI.getErrorDialog(mainActivity, result, PLAY_SERVICE_RESOLUTION_REQUEST).show();
            }
            return false;
        }
        return true;
    }

    void askForPermission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
            //we do not already have permission
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted

                } else {
                    // permission denied
                    // well the user is an idiot we will just ask him AGAIN
                    askForPermission();
                }
                return;
            }

        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        Intent intent = new Intent(this, ActivityRecService.class);
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        ActivityRecognition.ActivityRecognitionApi.requestActivityUpdates(apiClient, TIMER, pendingIntent);
        Log.d(TAG, "onConnected: send pending intent");
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended: oups");
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        //let's just hope this never happens as i have no idea what to do in this case
        Log.d(TAG, "onConnectionFailed: this should not happen");
    }

    //test if a service is running
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    private void initBtnButtom() {
        btnMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Do nothing
            }
        });

        btnGlobe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), GlobeActivity.class);
                startActivity(intent);
            }
        });

        btnCalender.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //transit to relevant view
                Intent intent = new Intent(view.getContext(), CalenderActivity.class);
                startActivity(intent);
            }
        });
    }

    //make the actually selected vehicle button again look selected
    private void resumeBtnVehicle(){
        String vehicle = spf.getString(currentVehicleKey, new String());
        Log.d(TAG, "resumeBtnVehicle: "+ vehicle + tBtnCar.isChecked());
        getVehicleBtn(vehicle).setChecked(true);
    }

    ToggleButton getVehicleBtn(String vehicle){
        switch (vehicle){
            case "Car":
                Log.d(TAG, "getVehicle: car");
                return tBtnCar;
            case "Boat":
                Log.d(TAG, "getVehicle: boat");
                return tBtnBoat;
            case "Bus":
                Log.d(TAG, "getVehicle: bus");
                return tBtnBus;
            case "plane":
                Log.d(TAG, "getVehicle: airplane");
                return tBtnAir;
            case "Tram":
                Log.d(TAG, "getVehicle: tram");
                return tBtnTram;
            case "Train":
                Log.d(TAG, "getVehicle: train");
                return tBtnTrain;
            default:
                Log.d(TAG, "getVehicle: default");
                return tBtnCar; //car is never a bad idea ... there is no reason behind this other than just simply have a fallback
        }

    }


    //does all the clicked, checked magic for the vehicle buttons.
    private void initBtnVehicle() {

        tBtnTram.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on tram");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnTram.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable tramNPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_tramwaycircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, tramNPressed, null, null);
                } else {
                    //this has been selected
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable tramPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_tramwaycirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, tramPressed, null, null);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnTrain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on train");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTram.setChecked(false);
                }
            }
        });

        tBtnTrain.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    Log.d(TAG, "onCheckedChanged: button already pressed");
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable trainNpressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_traincircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, trainNpressed, null, null);
                } else {
                    //this has been selected
                    Log.d(TAG, "onCheckedChanged: button not already pressed");
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable trainPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_traincirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, trainPressed, null, null);

                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTram.setChecked(false);
                }
            }
        });

        tBtnAir.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on airplane");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnTram.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnAir.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    Log.d(TAG, "onCheckedChanged: button already pressed");
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable planeNpressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_airplanecircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, planeNpressed, null, null);
                } else {
                    //this has been selected
                    Log.d(TAG, "onCheckedChanged: button not already pressed");
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable planePressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_airplanecirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, planePressed, null, null);

                    tBtnTram.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnBoat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on boat");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnBoat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    Log.d(TAG, "onCheckedChanged: button already pressed");
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable boatNpressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_boatcircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, boatNpressed, null, null);
                } else {
                    //this has been selected
                    Log.d(TAG, "onCheckedChanged: button not already pressed");
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable boatPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_boatcirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, boatPressed, null, null);

                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnCar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on car");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnCar.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    Log.d(TAG, "onCheckedChanged: button already pressed");
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable carNpressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_carcircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, carNpressed, null, null);
                } else {
                    //this has been selected
                    Log.d(TAG, "onCheckedChanged: button not already pressed");
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable carPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_carcirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, carPressed, null, null);

                    tBtnAir.setChecked(false);
                    tBtnBus.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnBus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked on bus");
                if (((ToggleButton) view).isChecked()) {
                    //the button is already pressed we will ignore it
                } else {
                    //this button has been selected
                    ToggleButton tbtn = (ToggleButton) view;
                    tbtn.setChecked(true);

                    //uncheck all other buttons
                    tBtnAir.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });

        tBtnBus.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Log.d(TAG, "onCheckedChanged: " + b);
                if (!b) {
                    //the thing is already selected  we will now unselect it
                    Log.d(TAG, "onCheckedChanged: button already pressed");
                    //colorBtn((ToggleButton) compoundButton, false);
                    Drawable busNpressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_buscircle);
                    ((ToggleButton)compoundButton).setCompoundDrawablesWithIntrinsicBounds(null, busNpressed, null, null);
                } else {
                    //this has been selected
                    Log.d(TAG, "onCheckedChanged: button not already pressed");
                    selectVehicle(compoundButton.getText().toString());
                    ToggleButton tbtn = (ToggleButton) compoundButton;
                    //colorBtn(tbtn, true);
                    Drawable busPressed = compoundButton.getContext().getResources().getDrawable(R.mipmap.ic_buscirclegrey);
                    tbtn.setCompoundDrawablesWithIntrinsicBounds(null, busPressed, null, null);

                    tBtnAir.setChecked(false);
                    tBtnTram.setChecked(false);
                    tBtnCar.setChecked(false);
                    tBtnBoat.setChecked(false);
                    tBtnTrain.setChecked(false);
                }
            }
        });


    }

    //write the vehicle that the user selected into the shared preferences for the service to read it
    public void selectVehicle(String vehicle) {
        Log.d(TAG, "selectVehicle: pressed on a button " + vehicle);
        spf.edit().putString(currentVehicleKey, vehicle).apply(); //apply is synchronous

    }

    private void initProgessBar() {
        int percent = analyzer.percentageCO2comparedToAverg(); // worker.work -> analyzer.get..... -> worker gives back true
        if (percent >= 100) { //so the progress bar does not get overset
            progressBar.setProgress(100);
            ColorStateList csl = new ColorStateList(new int[][]{{}}, new int[]{Color.RED});
            progressBar.setProgressBackgroundTintList(csl);
            progressBar.setProgressTintList(csl); //this makes the progress bar red

            tvPercent.setTextColor(Color.RED); //set the text red

        } else {
            progressBar.setProgress(percent);
        }

        tvPercent.setText(percent + "%");
    }

    private void initTable() {
        int i = 0;
        for (MeanOfTransport mt : MeanOfTransport.values()) {
            if (mt.equals(MeanOfTransport.STILL)) continue;
            tvDis[i].setText("" + roundTo2Decimals(analyzer.distTraveled(mt, TimePeriod.DAILY)));
            i++;
        }
        i = 0;
        for (MeanOfTransport mt : MeanOfTransport.values()) {
            if (mt.equals(MeanOfTransport.STILL)) continue;
            tvEnergy[i].setText("" + roundTo2Decimals(analyzer.energyUsed(mt, TimePeriod.DAILY)));
            i++;
        }
        i = 0;
        for (MeanOfTransport mt : MeanOfTransport.values()) {
            if (mt.equals(MeanOfTransport.STILL)) continue;
            tvCO2[i].setText("" + roundTo2Decimals(analyzer.co2Emmited(mt, TimePeriod.DAILY)));
            i++;
        }
    }


    double roundTo2Decimals(double val) {
        return Math.round(val * 100.0) / 100.0;
    }
}
