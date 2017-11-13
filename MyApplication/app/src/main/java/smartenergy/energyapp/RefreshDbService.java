package smartenergy.energyapp;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import java.util.Timer;
import java.util.TimerTask;

public class RefreshDbService extends Service {

    public static final long NOTIFY_INTERVAL = 120 * 1000; // 2 min
    Analyzer analyzer;
    private DBRawHelper dbRawHelper;
    private DBProcessedHelper dbProcessedHelper;
    private DBAggregatedHelper dbAggregatedHelper;

    private static String TAG = "RefreshDbService";

    // run on another Thread to avoid crash
    private Handler mHandler = new Handler();
    // timer handling
    private Timer mTimer = null;

    public RefreshDbService() {
        dbRawHelper = new DBRawHelper(this);
        dbProcessedHelper = new DBProcessedHelper(this);
        dbAggregatedHelper = new DBAggregatedHelper(this);
        analyzer = new Analyzer(dbRawHelper, dbProcessedHelper, dbAggregatedHelper);
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        // cancel if already existed
        if(mTimer != null) {
            mTimer.cancel();
        } else {
            // recreate new
            mTimer = new Timer();
        }
        // schedule task
        mTimer.scheduleAtFixedRate(new TimeDisplayTimerTask(), 0, NOTIFY_INTERVAL);
    }


    class TimeDisplayTimerTask extends TimerTask {

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {

                @Override
                public void run() {
                    Log.d(TAG, "run: refreshing the db");
                    analyzer.refreshCaches();
                }

            });
        }

    }
}
