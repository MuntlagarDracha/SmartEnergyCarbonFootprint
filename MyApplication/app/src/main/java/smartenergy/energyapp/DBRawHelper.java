package smartenergy.energyapp;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class DBRawHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "samples_raw.db";

    public DBRawHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
        db.execSQL("INSERT INTO " + RawEntry.TABLE_NAME + "(" + // Create dummy entry
                RawEntry.COLUMN_NAME_TIMESTAMP + "," +
                RawEntry.COLUMN_NAME_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_STILL + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + "," +
                RawEntry.COLUMN_NAME_LONGITUDE + "," +
                RawEntry.COLUMN_NAME_LATITUDE + "," +
                RawEntry.COLUMN_NAME_SPEED + ") " +
                "VALUES (0,\"FOOT\",0,0,0,0,0,0,0,0,0,0,0)");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public void insertActivity(int tsSeconds,
                               String vehicle,
                               int vehicleConfidence,
                               int bicycleConfidence,
                               int footConfidence,
                               int runningConfidence,
                               int stillConfidence,
                               int tiltingConfidence,
                               int walkingConfidence,
                               int unknownCondfidence) {
        SQLiteDatabase db = getWritableDatabase();

        db.execSQL("INSERT INTO " + RawEntry.TABLE_NAME + "(" +
                RawEntry.COLUMN_NAME_TIMESTAMP + "," +
                RawEntry.COLUMN_NAME_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_STILL + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + "," +
                RawEntry.COLUMN_NAME_LONGITUDE + "," +
                RawEntry.COLUMN_NAME_LATITUDE + "," +
                RawEntry.COLUMN_NAME_SPEED + ") " +
                "select " +
                tsSeconds + ", " +
                "\"" + vehicle + "\"" + ", " +
                vehicleConfidence + ", " +
                bicycleConfidence + ", " +
                footConfidence + ", " +
                runningConfidence + ", " +
                stillConfidence + ", " +
                tiltingConfidence + ", " +
                walkingConfidence + ", " +
                unknownCondfidence + ", " +
                RawEntry.COLUMN_NAME_LONGITUDE + "," +
                RawEntry.COLUMN_NAME_LATITUDE + "," +
                RawEntry.COLUMN_NAME_SPEED + "" +
                " FROM " + RawEntry.TABLE_NAME + " ORDER BY " + RawEntry.COLUMN_NAME_TIMESTAMP + " DESC LIMIT 1;");
    }

    public void deleteAllTables(DBProcessedHelper dbProcessedHelper, DBAggregatedHelper dbAggregatedHelper){
        getWritableDatabase().execSQL("DELETE FROM "+RawEntry.TABLE_NAME);
        dbProcessedHelper.deleteProcessedAndAggregatedEntries(dbAggregatedHelper);
        getWritableDatabase().execSQL("INSERT INTO " + RawEntry.TABLE_NAME + "(" + // Create dummy entry
                RawEntry.COLUMN_NAME_TIMESTAMP + "," +
                RawEntry.COLUMN_NAME_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_STILL + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + "," +
                RawEntry.COLUMN_NAME_LONGITUDE + "," +
                RawEntry.COLUMN_NAME_LATITUDE + "," +
                RawEntry.COLUMN_NAME_SPEED + ") " +
                "VALUES (0,\"FOOT\",0,0,0,0,0,0,0,0,0,0,0)");
    }

    public void insertPosition(int tsSeconds,
                               double longitude,
                               double latitude,
                               float speed) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO " + RawEntry.TABLE_NAME + "(" +
                RawEntry.COLUMN_NAME_TIMESTAMP + "," +
                RawEntry.COLUMN_NAME_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_STILL + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + "," +
                RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + "," +
                RawEntry.COLUMN_NAME_LONGITUDE + "," +
                RawEntry.COLUMN_NAME_LATITUDE + "," +
                RawEntry.COLUMN_NAME_SPEED + ") " +
                "select " +
                tsSeconds + ", " +
                RawEntry.COLUMN_NAME_VEHICLE + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_STILL + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + ", " +
                RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + ", " +
                longitude + "," +
                latitude + "," +
                speed +
                " FROM " + RawEntry.TABLE_NAME + " ORDER BY " + RawEntry.COLUMN_NAME_TIMESTAMP + " DESC LIMIT 1;");
    }

    // Query the database for relevant information in a given time period
    public Sample[] queryDB(int sinceSecond, String sortOrder) {
        Cursor cursor = getReadableDatabase().rawQuery(
                String.format("SELECT * FROM %s WHERE timestamp > ? ORDER BY %s %s", RawEntry.TABLE_NAME, RawEntry.COLUMN_NAME_TIMESTAMP, sortOrder),
                new String[]{String.valueOf(sinceSecond)}
        );
        Sample[] samples = new Sample[cursor.getCount()];
        int i = 0;
        if (cursor.moveToFirst()) {
            do {
                // Get raw data from DB
                // Map probabilities to means of transport
                Map<Integer, MeanOfTransport> probaToTrans = new HashMap<>();
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_STILL)), MeanOfTransport.STILL);
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_TILTING)), MeanOfTransport.STILL);
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_FOOT)), MeanOfTransport.FOOT); // = walk + run => ignoring those
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE)), MeanOfTransport.BICYCLE);
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE)), null);
                probaToTrans.put(cursor.getInt(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN)), MeanOfTransport.STILL);
                String userDefVehicle = cursor.getString(cursor.getColumnIndexOrThrow(RawEntry.COLUMN_NAME_VEHICLE));
                // Find max value
                Map.Entry<Integer, MeanOfTransport> maxEntry = null;
                for (Map.Entry<Integer, MeanOfTransport> entry : probaToTrans.entrySet()) {
                    if (maxEntry == null || entry.getKey().compareTo(maxEntry.getKey()) > 0)
                        maxEntry = entry;
                }
                MeanOfTransport transport = maxEntry.getValue();
                // Bring user defined Vehicle into account
                if (transport == null) {
                    for (MeanOfTransport v : MeanOfTransport.values()) {
                        if (Objects.equals(v.toString(), userDefVehicle)) {
                            transport = v;
                            break;
                        }
                    }
                }
                if (transport == null) {
                    Log.e("DBRawHelper", "Unknown transport: " + userDefVehicle);
                    transport = MeanOfTransport.STILL;
                }
                // Create Sample object and insert it
                samples[i] = new Sample(
                        new Timestamp(1000L * cursor.getInt(cursor.getColumnIndexOrThrow(DBRawHelper.RawEntry.COLUMN_NAME_TIMESTAMP))),
                        transport,
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DBRawHelper.RawEntry.COLUMN_NAME_LONGITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DBRawHelper.RawEntry.COLUMN_NAME_LATITUDE)),
                        cursor.getDouble(cursor.getColumnIndexOrThrow(DBRawHelper.RawEntry.COLUMN_NAME_SPEED))
                );
                i++;
            } while (cursor.moveToNext());
        }
        cursor.close();
        return samples;
    }

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + RawEntry.TABLE_NAME + " (" +
                    RawEntry._ID + "INTEGER PRIMARY KEY," +
                    RawEntry.COLUMN_NAME_TIMESTAMP + " INTEGER," +
                    RawEntry.COLUMN_NAME_VEHICLE + " TEXT," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_VEHICLE + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_BICYCLE + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_FOOT + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_RUNNING + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_STILL + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_TILTING + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_WALKING + " INTEGER," +
                    RawEntry.COLUMN_NAME_CONFIDENCE_UNKNOWN + " INTEGER," +
                    RawEntry.COLUMN_NAME_LONGITUDE + " REAL," +
                    RawEntry.COLUMN_NAME_LATITUDE + " REAL," +
                    RawEntry.COLUMN_NAME_SPEED + " REAL)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + RawEntry.TABLE_NAME;

    /* Inner class that defines the table contents */
    private static class RawEntry implements BaseColumns {
        public static final String TABLE_NAME = "activityEntry";
        public static final String COLUMN_NAME_TIMESTAMP = "timestamp";
        public static final String COLUMN_NAME_VEHICLE = "vehicle";
        public static final String COLUMN_NAME_CONFIDENCE_VEHICLE = "confidence_vehicle";
        public static final String COLUMN_NAME_CONFIDENCE_BICYCLE = "confidence_bicycle";
        public static final String COLUMN_NAME_CONFIDENCE_FOOT = "confidence_foot";
        public static final String COLUMN_NAME_CONFIDENCE_RUNNING = "confidence_running";
        public static final String COLUMN_NAME_CONFIDENCE_STILL = "confidence_still";
        public static final String COLUMN_NAME_CONFIDENCE_TILTING = "confidence_tilting";
        public static final String COLUMN_NAME_CONFIDENCE_WALKING = "confidence_walking";
        public static final String COLUMN_NAME_CONFIDENCE_UNKNOWN = "confidence_unknown";
        public static final String COLUMN_NAME_LONGITUDE = "longitude";
        public static final String COLUMN_NAME_LATITUDE = "latitude";
        public static final String COLUMN_NAME_SPEED = "speed";
    }

}
